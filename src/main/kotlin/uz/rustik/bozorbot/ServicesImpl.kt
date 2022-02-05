package uz.rustik.bozorbot

import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.stereotype.Service
import uz.ugnis.tgbotlib.Chat
import uz.ugnis.tgbotlib.ChatService
import java.util.*
import javax.persistence.EntityManager
import javax.transaction.Transactional

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>, entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {
    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    @Transactional
    override fun trash(id: Long) = save(getOne(id).apply { deleted = true })
    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
}

@Service
class UserServiceImpl(private val userRepository: UserRepository) : UserService {

    override fun findByUsernameAndPassword(username: String, password: String): User? {
        return userRepository.findByUserNameAndPasswordAndDeletedFalse(
            username,
            password
        )
    }

    override fun containsUserName(userName: String): Boolean {
        return userRepository.existsByUserNameAndDeletedFalse(userName)
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

