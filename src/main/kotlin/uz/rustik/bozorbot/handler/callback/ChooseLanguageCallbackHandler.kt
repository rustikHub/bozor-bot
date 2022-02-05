package uz.rustik.bozorbot.handler.callback

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.bots.AbsSender
import uz.rustik.bozorbot.CallbackTypes
import uz.rustik.bozorbot.LocaleMessageSourceKey.*
import uz.rustik.bozorbot.Steps
import uz.rustik.bozorbot.MyChatService
import uz.rustik.bozorbot.MessageSourceService
import uz.rustik.bozorbot.UserServiceImpl
import uz.ugnis.tgbotlib.CallbackHandler
import uz.ugnis.tgbotlib.answerCallBackQuery
import uz.ugnis.tgbotlib.sendMessage
import java.util.*

@Service
class ChooseLanguageCallbackHandler(
    private val messageSourceService: MessageSourceService,
    private val userServiceImpl: UserServiceImpl,
    private val myChatService: MyChatService
) : CallbackHandler {
    override fun callbackHandle(callback: CallbackQuery, sender: AbsSender) {
        val message = callback.message
        val chatId = message.chatId.toString()
        val chat = myChatService.findByChatId(message.chatId)
        val lang: Locale
        val answerCallbackQuery = AnswerCallbackQuery()
        answerCallbackQuery.callbackQueryId = callback.id

        val keys = callback.data.split("#")

        val sendMessage = SendMessage()
        sendMessage.chatId = chatId
        sendMessage.enableHtml(true)

        val editMessage = EditMessageText()
        editMessage.chatId = chatId
        editMessage.enableHtml(true)

        when (keys[0]) {
            CallbackTypes.CHOOSE_LANGUAGE.name -> {
                val keyLang = keys[1]
                chat.language = "en"
                lang = Locale(keyLang)
                answerCallbackQuery.text = messageSourceService.getMessage(LANGUAGE_CHANGED, Locale(chat.language))
                sendMessage.text = messageSourceService.getMessage(ENTER_USERNAME,  Locale(chat.language))
                chat.chatStep = Steps.ENTER_USERNAME.name
                chat.isNew = false
                myChatService.save(chat)
                sender.sendMessage(sendMessage)
            }
        }

        sender.answerCallBackQuery(answerCallbackQuery)
    }

    override fun callbackTypes(): List<String> {
        return listOf(CallbackTypes.CHOOSE_LANGUAGE.name)
    }
}