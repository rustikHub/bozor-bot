package uz.rustik.bozorbot.handler.message

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender
import uz.rustik.bozorbot.*
import uz.ugnis.tgbotlib.*
import java.util.*

@Service
class MainMenuMessageHandler(
    private val userServiceImpl: UserServiceImpl,
    private val myChatService: MyChatService,
    private val storeService: StoreService,
    private val messageSourceService: MessageSourceService
) : MessageHandler {
    override fun messageHandle(message: Message, sender: AbsSender) {
        val chatId = message.chatId.toString()
        val chat = myChatService.findByChatId(message.chatId)
        val responseMessage = SendMessage()
        responseMessage.chatId = message.chatId.toString()
        responseMessage.enableHtml(true)

        val lang = Locale(chat.language)

        val user = chat.user ?: throw UsernameNotFound(sender, message, "User not found")

        if (message.hasText()) {
            val text = message.text
            when (text) {
                _START -> {
                    sender.deleteMessage(message.messageId, chatId)
                    chat.botMessageId?.run { sender.deleteMessage(this, chatId) }
                    responseMessage.text = "Salom"
                    responseMessage.replyMarkup =
                        mainMenuInlineMarkup(user.roles, messageSourceService, lang)
                    chat.user = user
                    chat.note = ""
                    chat.chatStep = CallbackTypes.MAIN_MENU.name
                    chat.botMessageId = sender.sendMessage(responseMessage)
                    myChatService.save(chat)
                }
                else -> {
                    when (chat.chatStep) {
                        Steps.ADD_BOSS_USERNAME.name -> {
                            sender.deleteMessage(message.messageId, chatId)
                            chat.botMessageId?.run { sender.deleteMessage(this, chatId) }

                            if (userServiceImpl.containsUserName(text)) {
                                throw UserNameExistsException(
                                    sender, message, messageSourceService.getMessage(
                                        LocaleMessageSourceKey.USER_EXISTS_EXCEPTION,
                                        arrayOf(text), lang
                                    )
                                )
                            }

                            chat.note = text
                            responseMessage.text = messageSourceService
                                .getMessage(LocaleMessageSourceKey.NEW_BOSS_PASSWORD, lang)
                            chat.chatStep = Steps.ADD_BOSS_PASSWORD.name
                            chat.botMessageId = sender.sendMessage(responseMessage)
                            myChatService.save(chat)
                        }
                        Steps.ADD_BOSS_PASSWORD.name -> {
                            sender.deleteMessage(message.messageId, chatId)
                            chat.botMessageId?.run { sender.deleteMessage(this, chatId) }

                            chat.note += "#$text^"
                            responseMessage.text = "Role tanlang"
                            responseMessage.replyMarkup = newBossChooseRoleInlineMarkup(listOf())
                            chat.botMessageId = sender.sendMessage(responseMessage)
                            myChatService.save(chat)
                        }

                        Steps.EDIT_BOSS_PASSWORD.name,
                        Steps.EDIT_BOSS_USERNAME.name -> {
                            sender.deleteMessage(message.messageId, chatId)
                            chat.botMessageId?.run { sender.deleteMessage(this, chatId) }

                            val id = chat.note.toLong()
                            val bossUser = userServiceImpl.findByUserId(id)

                            if (chat.chatStep == Steps.EDIT_BOSS_PASSWORD.name) {
                                bossUser.password = text
                                responseMessage.text = "tesfdfa"
                                responseMessage.replyMarkup = editBossInlineMarkup(bossUser, messageSourceService, lang)
                                chat.chatStep = CallbackTypes.MAIN_MENU.name
                            } else {
                                if (userServiceImpl.containsUserName(text)) {
                                    throw UserNameExistsException(
                                        sender, message, messageSourceService.getMessage(
                                            LocaleMessageSourceKey.USER_EXISTS_EXCEPTION,
                                            arrayOf(text), lang
                                        )
                                    )
                                }
                                bossUser.userName = text
                                responseMessage.text = messageSourceService
                                    .getMessage(LocaleMessageSourceKey.NEW_BOSS_PASSWORD, lang)
                                chat.chatStep = Steps.EDIT_BOSS_PASSWORD.name
                            }
                            userServiceImpl.save(bossUser)
                            chat.botMessageId = sender.sendMessage(responseMessage)
                            myChatService.save(chat)
                        }
                        Steps.INPUT_SHOP_NAME.name -> {
                            sender.deleteMessage(message.messageId, chatId)
                            chat.botMessageId?.run { sender.deleteMessage(this, chatId) }

                            if (storeService.existsByStoreName(text)) {
                                //TODO exception berish kere
                                return
                            }
                            val shopId = chat.note.toLongOrNull()
                            if (shopId != null) {
                                val shop = storeService.findById(shopId).apply { name = text }
                                storeService.save(shop)
                                chat.note = ""
                                chat.chatStep = Steps.MAIN_MENU.name
                                responseMessage.text =
                                    messageSourceService.getMessage(LocaleMessageSourceKey.SHOP_INFO_TEXT, lang)
                                responseMessage.replyMarkup =
                                    editShopInlineMarkup(user, shop, messageSourceService, lang)
                                chat.botMessageId = sender.sendMessage(responseMessage)
                            } else {
                                val newStore = Store(text, StoreType.valueOf(chat.note), user)
                                storeService.save(newStore)
                                val allUsersShopList = storeService.getAllUsersStoreList(user)
                                chat.chatStep = Steps.MAIN_MENU.name
                                responseMessage.text =
                                    messageSourceService.getMessage(LocaleMessageSourceKey.SHOPS_MENU, lang)
                                responseMessage.replyMarkup =
                                    shopsInlineMarkup(
                                        allUsersShopList,
                                        messageSourceService,
                                        lang
                                    )
                                chat.botMessageId = sender.sendMessage(responseMessage)
                            }
                            myChatService.save(chat)
                        }
                        Steps.INPUT_USERNAME.name -> {
                            sender.deleteMessage(message.messageId, chatId)
                            chat.botMessageId?.run { sender.deleteMessage(this, chatId) }

                            if (userServiceImpl.containsUserName(text)) {
                                throw UserNameExistsException(
                                    sender, message, messageSourceService.getMessage(
                                        LocaleMessageSourceKey.USER_EXISTS_EXCEPTION,
                                        arrayOf(text), lang
                                    )
                                )
                            }

                            chat.note = text
                            responseMessage.text = messageSourceService
                                .getMessage(LocaleMessageSourceKey.INPUT_USER_PASSWORD_TEXT, lang)
                            chat.chatStep = Steps.INPUT_USER_PASSWORD.name
                            chat.botMessageId = sender.sendMessage(responseMessage)
                            myChatService.save(chat)
                        }
                        Steps.INPUT_USER_PASSWORD.name -> {
                            sender.deleteMessage(message.messageId, chatId)
                            chat.botMessageId?.run { sender.deleteMessage(this, chatId) }

                            chat.note += "#$text^"
                            responseMessage.text = "Role tanlang"
                            responseMessage.replyMarkup = newUserChooseRoleInlineMarkup(listOf())
                            chat.botMessageId = sender.sendMessage(responseMessage)
                            myChatService.save(chat)
                        }
                        else -> {
                            sender.deleteMessage(message.messageId, chatId)
                        }
                    }
                }
            }
        }
    }

    override fun steps(): List<String> {
        return listOf(
            CallbackTypes.MAIN_MENU.name,
            CallbackTypes.MY_WORKERS.name,
            CallbackTypes.MY_SHOPS.name,
            CallbackTypes.ADD_ORDER.name,
            CallbackTypes.LOG_OUT.name,
            CallbackTypes.ADD_BOSS.name,
            CallbackTypes.ADD_BOSS_CHOOSE_ROLE.name,
            CallbackTypes.BOSS_PAGE_BUTTON.name,
            CallbackTypes.ADD_BOSS_DONE.name,
            CallbackTypes.EDIT_BOSS.name,
            CallbackTypes.EDIT_USER_INFO.name,
            CallbackTypes.BACK_FROM_EDIT_BOSS.name,
            CallbackTypes.DO_NOTHING.name,
            CallbackTypes.BACK_TO_MAIN_MENU.name,
            CallbackTypes.EDIT_USER_ROLE.name,
            CallbackTypes.DELETE_BOSS.name,
            CallbackTypes.NO_BUTTON.name,
            CallbackTypes.YES_BUTTON.name,

            Steps.ADD_BOSS_USERNAME.name,
            Steps.ADD_BOSS_PASSWORD.name,
            Steps.EDIT_BOSS_PASSWORD.name,
            Steps.EDIT_BOSS_USERNAME.name,
            Steps.INPUT_SHOP_NAME.name,
            Steps.INPUT_USERNAME.name,
            Steps.INPUT_USER_PASSWORD.name
        )
    }
}