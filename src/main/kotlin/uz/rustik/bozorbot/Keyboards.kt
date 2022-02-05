package uz.rustik.bozorbot

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import uz.ugnis.tgbotlib.InlineKeyboardMarkupBuilder
import uz.rustik.bozorbot.CallbackTypes.*
import uz.rustik.bozorbot.LocaleMessageSourceKey.*
import uz.ugnis.tgbotlib.InlineKeyboardMarkupBuilder.Companion.button
import java.util.*

val languagesInlineMarkup = InlineKeyboardMarkupBuilder()
    .row(button("🇺🇿 O'zbekcha 🇺🇿", "${CHOOSE_LANGUAGE}#uz"))
    .row(button("🇺🇸 English 🇺🇸", "${CHOOSE_LANGUAGE}#en"))
    .row(button("🇷🇺 Русский 🇷🇺", "${CHOOSE_LANGUAGE}#ru"))
    .build()

fun mainMenuInlineMarkup(
    roles: List<Role>,
    messageSourceService: MessageSourceService,
    lang: Locale
): InlineKeyboardMarkup {
    val builder = InlineKeyboardMarkupBuilder()

    if (roles.contains(Role.ROOT)) {
        builder.addRowButton("", "")
    }
    if (roles.contains(Role.BOSS)) {
        builder.addRowButton(messageSourceService.getMessage(SHOP_BUTTON_TEXT, lang), MY_SHOPS.name)
        builder.addRowButton(messageSourceService.getMessage(WORKERS_BUTTON_TEXT, lang), MY_SHOPS.name)
    }
    if (roles.contains(Role.MODERATOR)) {

    }
    if (roles.contains(Role.SELLER)) {

    }
    builder.addRowButton(messageSourceService.getMessage(LOG_OUT_BUTTON_TEXT, lang), LOG_OUT.name)



    return builder.build()
}