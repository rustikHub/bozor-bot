package uz.rustik.bozorbot

import org.springframework.context.annotation.Bean
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender
import uz.ugnis.tgbotlib.MassageHandleException
import uz.ugnis.tgbotlib.sendMessage

class UsernameNotFound(absSender: AbsSender, message: Message, exception: String) :
    MassageHandleException(absSender, message, exception) {

    override fun handle() {
        this.absSender.sendMessage(SendMessage(telegramMessage.chatId.toString(), message!!))
    }

}