package uz.rustik.bozorbot

interface MyChatService {
    fun findByChatId(chatId: Long): MyChat
    fun setChatStep(step: String, chatId: Long)
    fun save(chat: MyChat): MyChat
}

interface UserService {
    fun containsUserName(userName: String): Boolean
    fun findByUsernameAndPassword(username: String, password: String): User?
}