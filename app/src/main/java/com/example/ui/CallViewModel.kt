package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class ActiveTab {
    DIALER, CONTACTS, HISTORY
}

enum class CallState {
    IDLE, DIALING, RINGING, CONNECTED, DISCONNECTED, FAILED
}

data class CallTurn(
    val speakerName: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class CallViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CallDatabase.getDatabase(application)
    private val repository = CallRepository(database.callDao())

    val callLogs = repository.allCallLogs
    val contacts = repository.allContacts

    // UI States
    private val _selectedTab = MutableStateFlow(ActiveTab.DIALER)
    val selectedTab = _selectedTab.asStateFlow()

    private val _dialerInput = MutableStateFlow("")
    val dialerInput = _dialerInput.asStateFlow()

    // Call state variables
    private val _activeCallContact = MutableStateFlow<ContactEntity?>(null)
    val activeCallContact = _activeCallContact.asStateFlow()

    private val _activeCallState = MutableStateFlow(CallState.IDLE)
    val activeCallState = _activeCallState.asStateFlow()

    private val _activeCallDuration = MutableStateFlow(0)
    val activeCallDuration = _activeCallDuration.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn = _isSpeakerOn.asStateFlow()

    private val _isCallHold = MutableStateFlow(false)
    val isCallHold = _isCallHold.asStateFlow()

    private val _callTranscript = MutableStateFlow<List<CallTurn>>(emptyList())
    val callTranscript = _callTranscript.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking = _isAiThinking.asStateFlow()

    private val _userInputText = MutableStateFlow("")
    val userInputText = _userInputText.asStateFlow()

    private var callTimerJob: Job? = null
    private var callFlowJob: Job? = null

    init {
        // Prepopulate contacts on start
        viewModelScope.launch {
            repository.checkAndPrepopulateContacts()
        }
    }

    fun selectTab(tab: ActiveTab) {
        _selectedTab.value = tab
    }

    fun setDialerInput(input: String) {
        _dialerInput.value = input
    }

    fun appendToDialer(char: Char) {
        if (_dialerInput.value.length < 15) {
            _dialerInput.value += char
        }
    }

    fun deleteFromDialer() {
        if (_dialerInput.value.isNotEmpty()) {
            _dialerInput.value = _dialerInput.value.dropLast(1)
        }
    }

    fun setUserInputText(text: String) {
        _userInputText.value = text
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
    }

    fun toggleHold() {
        _isCallHold.value = !_isCallHold.value
    }

    // Initiate Call from Dialer or Contacts List
    fun initiateCall(contact: ContactEntity) {
        if (_activeCallState.value != CallState.IDLE) return

        _activeCallContact.value = contact
        _activeCallState.value = CallState.DIALING
        _callTranscript.value = emptyList()
        _activeCallDuration.value = 0
        _isMuted.value = false
        _isSpeakerOn.value = false
        _isCallHold.value = false

        callFlowJob = viewModelScope.launch {
            // Dialing state for 1.5 seconds
            delay(1500)
            if (_activeCallState.value == CallState.DIALING) {
                _activeCallState.value = CallState.RINGING
            }

            // Ringing state for 2 seconds
            delay(2000)
            if (_activeCallState.value == CallState.RINGING) {
                _activeCallState.value = CallState.CONNECTED

                // Insert into Call Logs as OUTGOING
                repository.insertCallLog(
                    CallLogEntity(
                        contactName = contact.name,
                        phoneNumber = contact.phoneNumber,
                        callType = "OUTGOING",
                        timestamp = System.currentTimeMillis(),
                        durationSeconds = 0
                    )
                )

                // Start active duration timer
                startCallTimer()

                // Trigger AI character to speak first!
                getAiGreeting(contact)
            }
        }
    }

    // Dial raw number entered in the dialer keypad
    fun initiateCallFromDialer() {
        val number = _dialerInput.value.trim()
        if (number.isEmpty()) return

        // Look for matching contact
        viewModelScope.launch {
            _activeCallContact.value = null // reset
            
            val contactToCall = ContactEntity(
                name = "Direct Dial ($number)",
                phoneNumber = number,
                status = "Online",
                avatarColorSeed = number.hashCode(),
                personaSystemInstruction = "You are a friendly customer service agent answering an online Internet phone call. Be warm, professional, helpful, and polite. Introduce yourself briefly, and ask what the caller needs help with. Keep responses short (1-2 sentences) and conversational. Avoid emojis.",
                isCustom = true
            )

            initiateCall(contactToCall)
            _dialerInput.value = "" // clear dialer
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_activeCallState.value == CallState.CONNECTED) {
                delay(1000)
                if (!_isCallHold.value) {
                    _activeCallDuration.value += 1
                }
            }
        }
    }

    private fun getAiGreeting(contact: ContactEntity) {
        viewModelScope.launch {
            _isAiThinking.value = true
            val greetingPrompt = "The user has just called you over an online connection. Answer with a brief 1-sentence friendly greeting welcoming them, in character. Keep it brief. Do not use emojis."
            val response = generateGeminiResponse(contact, greetingPrompt, emptyList())
            _isAiThinking.value = false

            _callTranscript.value = _callTranscript.value + CallTurn(
                speakerName = contact.name,
                text = response,
                isUser = false
            )
        }
    }

    fun speakIntoCall() {
        val userInput = _userInputText.value.trim()
        val contact = _activeCallContact.value
        if (userInput.isEmpty() || contact == null || _activeCallState.value != CallState.CONNECTED || _isCallHold.value) return

        // Add user turn
        val newUserTurn = CallTurn(
            speakerName = "You",
            text = userInput,
            isUser = true
        )
        _callTranscript.value = _callTranscript.value + newUserTurn
        _userInputText.value = ""

        viewModelScope.launch {
            _isAiThinking.value = true

            // Gather context history (recent 6 turns to stay safe on context tokens)
            val history = _callTranscript.value.takeLast(6)
            
            // Generate answer from Gemini
            val response = generateGeminiResponse(contact, userInput, history)
            
            _callTranscript.value = _callTranscript.value + CallTurn(
                speakerName = contact.name,
                text = response,
                isUser = false
            )
            _isAiThinking.value = false
        }
    }

    // Call Gemini API with system instructions and chat history
    private suspend fun generateGeminiResponse(
        contact: ContactEntity,
        latestPrompt: String,
        history: List<CallTurn>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Connection secure. (Note: Please set your GEMINI_API_KEY in the Secrets panel of AI Studio to enable interactive voice dialogue!)"
        }

        val systemContent = Content(
            parts = listOf(Part(text = contact.personaSystemInstruction))
        )

        // Build conversational contents array
        val contentsList = mutableListOf<Content>()
        
        // Convert history turns to Gemini format
        for (turn in history) {
            val role = if (turn.isUser) "user" else "model"
            contentsList.add(
                Content(
                    parts = listOf(Part(text = turn.text)),
                    role = role
                )
            )
        }

        val request = GenerateContentRequest(
            contents = contentsList,
            systemInstruction = systemContent,
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 150
            )
        )

        return try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Hello! I can hear you over the line but I'm experiencing some network static. What was that?"
        } catch (e: Exception) {
            "Hmm, static on the line. Let's try again!"
        }
    }

    fun endCall() {
        val contact = _activeCallContact.value
        val duration = _activeCallDuration.value

        callFlowJob?.cancel()
        callTimerJob?.cancel()

        if (_activeCallState.value == CallState.CONNECTED && contact != null) {
            // Update last log with correct duration
            viewModelScope.launch {
                repository.insertCallLog(
                    CallLogEntity(
                        contactName = contact.name,
                        phoneNumber = contact.phoneNumber,
                        callType = "OUTGOING",
                        timestamp = System.currentTimeMillis(),
                        durationSeconds = duration
                    )
                )
            }
        }

        _activeCallState.value = CallState.DISCONNECTED
        viewModelScope.launch {
            delay(1000)
            _activeCallState.value = CallState.IDLE
            _activeCallContact.value = null
            _callTranscript.value = emptyList()
            _activeCallDuration.value = 0
        }
    }

    fun addCustomContact(name: String, number: String, persona: String) {
        viewModelScope.launch {
            val newContact = ContactEntity(
                name = name,
                phoneNumber = number,
                status = "Online",
                avatarColorSeed = name.hashCode(),
                personaSystemInstruction = if (persona.trim().isEmpty()) {
                    "You are $name, a contact in the user's phone. Speak briefly (1-3 sentences) like a real person. Keep a conversational, friendly style suitable for a phone call. Avoid emojis."
                } else {
                    persona
                },
                isCustom = true
            )
            repository.insertContact(newContact)
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    fun deleteCallLog(id: Int) {
        viewModelScope.launch {
            repository.deleteCallLog(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearCallLogs()
        }
    }
}
