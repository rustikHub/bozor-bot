package uz.rustik.bozorbot

import org.springframework.stereotype.Service
import uz.rustik.testlib.model.Chat
import uz.rustik.testlib.service.ChatService

@Service
class ChatServiceImpl : ChatService {
    override fun findByChatId(chatId: Long?): Chat {
        return Chat(123, "")
    }
}