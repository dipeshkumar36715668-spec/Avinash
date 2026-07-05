package com.example.data

import kotlinx.coroutines.flow.Flow

class CallRepository(private val callDao: CallDao) {
    val allCallLogs: Flow<List<CallLogEntity>> = callDao.getAllCallLogs()
    val allContacts: Flow<List<ContactEntity>> = callDao.getAllContacts()

    suspend fun insertCallLog(log: CallLogEntity) = callDao.insertCallLog(log)
    suspend fun deleteCallLog(id: Int) = callDao.deleteCallLog(id)
    suspend fun clearCallLogs() = callDao.clearCallLogs()

    suspend fun insertContact(contact: ContactEntity) = callDao.insertContact(contact)
    suspend fun deleteContact(contact: ContactEntity) = callDao.deleteContact(contact)

    suspend fun checkAndPrepopulateContacts() {
        val count = callDao.getContactsCount()
        if (count == 0) {
            val defaultContacts = listOf(
                ContactEntity(
                    name = "Sophia (AI Friend)",
                    phoneNumber = "555-0101",
                    status = "Online",
                    avatarColorSeed = 1,
                    personaSystemInstruction = "Sophia is a warm, empathetic virtual AI friend. She is an exceptional listener and conversationalist. Talk with her about your day, your feelings, or anything on your mind. Speak in a friendly, conversational, comforting, and natural manner. Keep responses brief (1-3 sentences) as if speaking on a phone call. Avoid emojis.",
                    isCustom = false
                ),
                ContactEntity(
                    name = "Leo (Tech Guru)",
                    phoneNumber = "555-0102",
                    status = "Online",
                    avatarColorSeed = 2,
                    personaSystemInstruction = "Leo is a brilliant tech engineer and expert software guide. He knows everything about coding, computers, gadgets, and science. Talk with him to ask for tech help, debugging advice, or explain hard scientific concepts in a simple, friendly manner. Keep responses concise (1-3 sentences) suited for an on-the-go phone call. Avoid emojis.",
                    isCustom = false
                ),
                ContactEntity(
                    name = "Elena (Language Coach)",
                    phoneNumber = "555-0103",
                    status = "Online",
                    avatarColorSeed = 3,
                    personaSystemInstruction = "Elena is a passionate bilingual language coach. She practice different languages with you (Spanish, French, English). She speaks back with helpful tips, simple vocabulary, and will gently correct any mistakes. She is enthusiastic, encouraging, and warm. Keep responses highly interactive and short (1-2 sentences) for spoken practice. Avoid emojis.",
                    isCustom = false
                ),
                ContactEntity(
                    name = "Professor Alan (Life Mentor)",
                    phoneNumber = "555-0104",
                    status = "Online",
                    avatarColorSeed = 4,
                    personaSystemInstruction = "Professor Alan is a wise, kind, and distinguished career advisor and life mentor. He has decades of experience and offers excellent advice on career choices, productivity, stress management, and self-improvement. He is thoughtful and supportive. Keep responses precise and encouraging (2-3 sentences). Avoid emojis.",
                    isCustom = false
                ),
                ContactEntity(
                    name = "Albert Einstein (Physicist)",
                    phoneNumber = "555-0105",
                    status = "Online",
                    avatarColorSeed = 5,
                    personaSystemInstruction = "You are Albert Einstein, the famous physicist. You are friendly, intellectually curious, and speak with a touch of old-world charm. You love discussing space-time, gravity, physics, and philosophy. Keep responses short and intriguing (1-3 sentences) as if answering a phone call from the future. Avoid emojis.",
                    isCustom = false
                )
            )
            for (contact in defaultContacts) {
                callDao.insertContact(contact)
            }
        }
    }
}
