package uz.rustik.bozorbot.handler.message

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender
import uz.rustik.bozorbot.*
import uz.ugnis.tgbotlib.MessageHandler
import uz.ugnis.tgbotlib.sendMessage
import uz.rustik.bozorbot.Steps.*
import uz.rustik.bozorbot.MyChatService
import uz.rustik.bozorbot.MessageSourceService
import uz.rustik.bozorbot.UserServiceImpl
import uz.ugnis.tgbotlib.deleteMessage
import java.util.*

@Service
class StartMessageHandler(
    private val userServiceImpl: UserServiceImpl,
    private val myChatService: MyChatService,
    private val messageSourceService: MessageSourceService
) : MessageHandler {
    override fun messageHandle(message: Message, sender: AbsSender) {
        val chatId = message.chatId.toString()
        val chat = myChatService.findByChatId(message.chatId)
        val responseMessage = SendMessage()
        responseMessage.chatId = message.chatId.toString()
        responseMessage.enableHtml(true)

        val lang = Locale(chat.language)
        if (message.hasText()) {
            when (val text = message.text) {
                _START -> {
                    sender.deleteMessage(message.messageId, chatId)
                    when {
                        chat.isNew -> {
                            chat.chatStep = CHOOSE_LANGUAGE.name
                            responseMessage.apply {
                                this.replyMarkup = languagesInlineMarkup
                                this.text = chooseLanguageMessage
                            }
                        }
                        chat.user != null -> {
                            responseMessage.text = "Salom logout"
                        }
                        else -> {
                            chat.note = ""
                            responseMessage.text = messageSourceService
                                .getMessage(LocaleMessageSourceKey.ENTER_USERNAME, lang)
                            chat.chatStep = ENTER_USERNAME.name
                        }
                    }
                    chat.botMessageId = sender.sendMessage(responseMessage)
                    myChatService.save(chat)
                }
                else -> {
                    when (Steps.valueOf(chat.chatStep)) {
                        ENTER_USERNAME -> {
                            sender.deleteMessage(message.messageId, chatId)
                            chat.botMessageId?.run { sender.deleteMessage(this, chatId) }
                            chat.note = text
                            chat.chatStep = ENTER_PASSWORD.name
                            responseMessage.text = messageSourceService
                                .getMessage(LocaleMessageSourceKey.ENTER_PASSWORD, lang)
                            chat.botMessageId = sender.sendMessage(responseMessage)

                            myChatService.save(chat)
                        }
                        ENTER_PASSWORD -> {
                            sender.deleteMessage(message.messageId, chatId)
                            chat.botMessageId?.run { sender.deleteMessage(this, chatId) }

                            val username = chat.note
                            val user = userServiceImpl.findByUsernameAndPassword(username, text)
                            if (user != null) {
                                responseMessage.text = "Salom"
                                responseMessage.replyMarkup =
                                    mainMenuInlineMarkup(user.roles, messageSourceService, lang)
                                chat.user = user
                                chat.chatStep = CallbackTypes.MAIN_MENU.name
                                chat.botMessageId = sender.sendMessage(responseMessage)
                            } else {
                                responseMessage.text = "‼ Username or password is incorrect"
                                sender.sendMessage(responseMessage)
                                responseMessage.text = messageSourceService.getMessage(
                                    LocaleMessageSourceKey.ENTER_USERNAME,
                                    Locale(chat.language)
                                )
                                chat.chatStep = ENTER_USERNAME.name
                                chat.botMessageId = sender.sendMessage(responseMessage)
                            }
                            myChatService.save(chat)
                        }
                        LOG_IN -> {
                            sender.deleteMessage(message.messageId, chatId)
                            responseMessage.text = messageSourceService.getMessage(
                                LocaleMessageSourceKey.ENTER_USERNAME,
                                Locale(chat.language)
                            )
                            chat.chatStep = ENTER_USERNAME.name
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
            "/start",
            CHOOSE_LANGUAGE.name,
            ENTER_PASSWORD.name,
            ENTER_USERNAME.name,
            LOG_IN.name
        )
    }
}