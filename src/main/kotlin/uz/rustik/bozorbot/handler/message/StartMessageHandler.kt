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
            val text = message.text
            when {
                text == _START -> {
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
                    myChatService.save(chat)
                    sender.sendMessage(responseMessage)
                }
                else -> {
                    when (Steps.valueOf(chat.chatStep)) {
                        ENTER_USERNAME -> {
                            if (!userServiceImpl.containsUserName(text))
                                throw UsernameNotFound(
                                    sender,
                                    message,
                                    "User not found!!"
                                )
                            chat.note = text
                            chat.chatStep = ENTER_PASSWORD.name
                            myChatService.save(chat)

                            responseMessage.text = messageSourceService
                                .getMessage(LocaleMessageSourceKey.ENTER_PASSWORD, lang)
                            sender.sendMessage(responseMessage)
                        }
                        ENTER_PASSWORD -> {
                            val username = chat.note
                            val user = userServiceImpl.findByUsernameAndPassword(username, text)
                            if (user != null) {
                                responseMessage.text = "Salom"

                                chat.user = user
                                myChatService.save(chat)

                                sender.sendMessage(responseMessage)
                            }
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
            ENTER_USERNAME.name
        )
    }
}