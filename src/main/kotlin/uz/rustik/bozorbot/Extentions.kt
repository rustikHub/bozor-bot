package uz.rustik.bozorbot

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender

fun List<String>.contains(role: Role): Boolean {
    this.forEach {
        if (it == role.name) {
            return true
        }
    }
    return false
}

fun List<String>.isRoot(): Boolean {
    return this.contains(Role.ROOT.name)
}

fun List<String>.isBoss(): Boolean {
    return this.contains(Role.BOSS.name)
}

fun List<String>.isModerator(): Boolean {
    return this.contains(Role.BOSS.name)
}

fun <T> List<T>.safeSubList(fromIndex: Int, toIndex: Int): List<T> =
    this.subList(fromIndex, toIndex.coerceAtMost(this.size))

fun Message.delete(absSender: AbsSender) {
    DeleteMessage(chatId.toString(), messageId)
        .run { absSender.execute(this) }
}