package uz.rustik.bozorbot

import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.ugnis.tgbotlib.Chat
import uz.ugnis.tgbotlib.ChatService
import java.util.*
import javax.persistence.EntityManager

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>, entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {
    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    @Transactional
    override fun trash(id: Long) = save(getOne(id).apply { deleted = true })
    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
}

@Service
class UserServiceImpl(
    private val jdbcTemplate: JdbcTemplate,
    private val userRepository: UserRepository,
    private val entityManager: EntityManager
) : UserService {

    override fun findByUsernameAndPassword(username: String, password: String): User? {
        return userRepository.findByUserNameAndPasswordAndDeletedFalse(
            username,
            password
        )
    }

    override fun findAllBossUsers(): List<User> {
        val result = jdbcTemplate.query(
            "select * from users u left join users_roles r on u.id = r.users_id where r.roles like 'BOSS' and u.id not in (select id from users left join users_roles on users.id = users_roles.users_id where users_roles.roles like 'ROOT') order by id asc"
        ) { rs, _ ->
            User.toEntity(rs)
        }
        return result
    }

    @Transactional
    override fun save(user: User): User {
        return userRepository.save(user)
    }

    @Transactional
    override fun saveWorker(user: User, shopId: Long): User {
        return userRepository.save(
            user.apply {
                this.store = entityManager
                    .getReference(Store::class.java, shopId)
            }
        )
    }

    override fun findByUserId(id: Long): User {
        return userRepository.findById(id).get()
    }

    override fun deleteById(id: Long) {
        userRepository.trash(id)
    }

    override fun setDeleteById(id: Long, delete: Boolean) {
        findByUserId(id).apply {
            this.deleted = delete
            save(this)
        }
    }

    override fun blockById(id: Long) {
        findByUserId(id).apply {
            this.blocked = true
            save(this)
        }
    }

    override fun setBlockById(id: Long, block: Boolean) {
        findByUserId(id).apply {
            this.blocked = block
            save(this)
        }
    }

    override fun findByUsername(username: String): User? {
        return userRepository.findByUserName(username)
    }

    override fun containsUserName(userName: String): Boolean {
        return userRepository.existsByUserNameAndDeletedFalse(userName)
    }

}

@Service
class StoreServiceImpl(
    private val jdbcTemplate: JdbcTemplate,
    private val repository: StoreRepository,
    private val entityManager: EntityManager
) : StoreService {
    override fun deleteById(id: Long) {
        repository.deleteById(id)
    }

    override fun findById(id: Long): Store {
        return repository.findById(id).get()
    }

    override fun existsByStoreName(shopName: String): Boolean {
        return repository.existsByNameAndDeletedFalse(shopName)
    }

    //Just for learning
    override fun getAllUsersStoreList(user: User): List<Store> {
        val result = jdbcTemplate
            .query(
                "select * from store sh where " +
                        "case when ${user.id} in (select r.users_id from users_roles r where r.roles = 'ROOT') then true " +
                        "else sh.boss_id = ${user.id} end " +
                        "order by created_date;"
            ) { rs, _ ->
                Store.toEntity(rs, entityManager)
            }
        return result
    }

    override fun save(store: Store): Store {
        return repository.save(store)
    }

}

@Service
class ChatServiceImpl(private val chatRepository: ChatRepository) : ChatService {
    override fun findByChatId(chatId: Long): Chat? {
        val user = chatRepository.findByChatId(chatId) ?: return null
        return Chat(user.chatId, user.chatStep)
    }

    override fun save(chat: Chat): Chat {
        chatRepository.save(MyChat(chat.chatId, chat.step))
        return chat
    }
}

@Service
class MyChatServiceImpl(private val chatRepository: ChatRepository) : MyChatService {
    override fun findByChatId(chatId: Long): MyChat {
        return chatRepository.findByChatId(chatId)!!
    }

    override fun setChatStep(step: String, chatId: Long) {
        findByChatId(chatId).apply {
            chatStep = step
            chatRepository.save(this)
        }
    }

    override fun save(chat: MyChat): MyChat {
        return chatRepository.save(chat)
    }
}

@Service
class MessageSourceService(val messageResourceBundleMessageSource: ResourceBundleMessageSource) {

    fun getMessage(sourceKey: LocaleMessageSourceKey, lang: Locale): String {
        return messageResourceBundleMessageSource.getMessage(
            sourceKey.name, null, lang
        )
    }

    fun getMessage(sourceKey: LocaleMessageSourceKey, any: Array<String>, lang: Locale): String {
        return messageResourceBundleMessageSource.getMessage(
            sourceKey.name, any, lang
        )
    }
}

