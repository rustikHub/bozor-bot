package uz.rustik.bozorbot

interface MyChatService {
    fun findByChatId(chatId: Long): MyChat
    fun setChatStep(step: String, chatId: Long)
    fun save(chat: MyChat): MyChat
}

interface UserService {
    fun containsUserName(userName: String): Boolean
    fun findByUsernameAndPassword(username: String, password: String): User?
    fun findAllBossUsers(): List<User>
    fun save(user: User): User
    fun findByUserId(id: Long): User
    fun deleteById(id: Long)
    fun setDeleteById(id: Long, delete: Boolean)
    fun blockById(id: Long)
    fun setBlockById(id: Long, black: Boolean)
    fun findByUsername(username: String): User?
}

interface StoreService {
    fun deleteById(id: Long)
    fun findById(id: Long): Store
    fun existsByStoreName(shopName: String): Boolean
    fun getAllUsersStoreList(user: User): List<Store>
    fun save(store: Store): Store
}