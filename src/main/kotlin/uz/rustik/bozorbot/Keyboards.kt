package uz.rustik.bozorbot

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import uz.ugnis.tgbotlib.InlineKeyboardMarkupBuilder
import uz.rustik.bozorbot.CallbackTypes.*
import uz.rustik.bozorbot.LocaleMessageSourceKey.*
import uz.ugnis.tgbotlib.InlineKeyboardMarkupBuilder.Companion.button
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

val languagesInlineMarkup = InlineKeyboardMarkupBuilder()
    .row(button("🇺🇿 O'zbekcha 🇺🇿", "${CHOOSE_LANGUAGE}#uz"))
    .row(button("🇺🇸 English 🇺🇸", "${CHOOSE_LANGUAGE}#en"))
    .row(button("🇷🇺 Русский 🇷🇺", "${CHOOSE_LANGUAGE}#ru"))
    .build()

fun mainMenuInlineMarkup(
    roles: List<String>,
    messageSourceService: MessageSourceService,
    lang: Locale
): InlineKeyboardMarkup {
    val builder = InlineKeyboardMarkupBuilder()

    if (roles.contains(Role.ROOT)) {
        builder.addRowButton(messageSourceService.getMessage(BOSS_BUTTON_TEXT, lang), BOSS_PAGE_BUTTON.name)
    }

    if (roles.contains(Role.BOSS) || roles.contains(Role.MODERATOR)) {
        builder.row(
            button(
                messageSourceService.getMessage(SHOP_BUTTON_TEXT, lang), MY_SHOPS.name
            ),
            button(
                messageSourceService.getMessage(WORKERS_BUTTON_TEXT, lang), "${MY_WORKERS.name}#"
            )
        )
    }

    if (roles.contains(Role.SELLER)) {
        builder.addRowButton(messageSourceService.getMessage(ADD_ORDER_BUTTON_TEXT, lang), ADD_ORDER.name)
    }
    builder.addRowButton(messageSourceService.getMessage(LOG_OUT_BUTTON_TEXT, lang), LOG_OUT.name)

    return builder.build()
}

fun bossInlineMarkup(
    users: List<User>,
    messageSourceService: MessageSourceService,
    lang: Locale,
    page: Int = 0
): InlineKeyboardMarkup {
    val builder = InlineKeyboardMarkupBuilder()
    builder.addRowButton(messageSourceService.getMessage(ADD_BOSS_BUTTON_TEXT, lang), ADD_BOSS.name)

    users.safeSubList(page * 6, (page + 1) * 6)
        .chunked(2) {
            val buttons = mutableListOf<InlineKeyboardButton>()
            buttons.add(button("👮 ${it[0].userName}", "${EDIT_BOSS}#${it[0].id}"))
            if (it.size == 2) {
                buttons.add(button("👮 ${it[1].userName}", "${EDIT_BOSS}#${it[1].id}"))
            }
            builder.row(*buttons.toTypedArray())
        }

    val size = users.size//7
    val maxPageSize = ceil(size / 6.0).roundToInt()
    if (users.isNotEmpty() && maxPageSize > 1) {
        val buttons = mutableListOf<InlineKeyboardButton>()
        if (page != 0) {
            buttons.add(button("⬅", "${BOSS_PAGE_BUTTON}#${page - 1}"))
        }

        buttons.add(button("${page + 1} / $maxPageSize", DO_NOTHING.name))

        if ((page + 1) < maxPageSize) {
            buttons.add(button("➡", "${BOSS_PAGE_BUTTON}#${page + 1}"))
        }
        builder.row(*buttons.toTypedArray())
    }

    builder.addRowButton(
        messageSourceService.getMessage(BACK_BUTTON_TEXT, lang),
        BACK_TO_MAIN_MENU.name
    )

    return builder.build()
}

fun newBossChooseRoleInlineMarkup(list: List<Role>): InlineKeyboardMarkup {
    val builder = InlineKeyboardMarkupBuilder()

//    val root = if (list.contains(Role.ROOT)) "✅ ROOT" else "ROOT"
    val boss = if (list.contains(Role.BOSS)) "✅ BOSS" else "BOSS"
    val moderator = if (list.contains(Role.MODERATOR)) "✅ MODERATOR" else "MODERATOR"
    val sailor = if (list.contains(Role.SELLER)) "✅ SELLER" else "SELLER"

//    builder.addRowButton(root, "${ADD_BOSS_CHOOSE_ROLE}#ROOT")
    builder.addRowButton(boss, "${ADD_BOSS_CHOOSE_ROLE}#BOSS")
    builder.addRowButton(moderator, "${ADD_BOSS_CHOOSE_ROLE}#MODERATOR")
    builder.addRowButton(sailor, "${ADD_BOSS_CHOOSE_ROLE}#SELLER")

    if (list.isNotEmpty()) {
        builder.addRowButton("📥 Save", ADD_BOSS_DONE.name)
    }

    return builder.build()
}

fun newUserChooseRoleInlineMarkup(list: List<Role>): InlineKeyboardMarkup {
    val builder = InlineKeyboardMarkupBuilder()

    val moderator = if (list.contains(Role.MODERATOR)) "✅ MODERATOR" else "MODERATOR"
    val sailor = if (list.contains(Role.SELLER)) "✅ SELLER" else "SELLER"

    builder.addRowButton(moderator, "${ADD_BOSS_CHOOSE_ROLE}#MODERATOR")
    builder.addRowButton(sailor, "${ADD_BOSS_CHOOSE_ROLE}#SELLER")

    if (list.isNotEmpty()) {
        builder.addRowButton("📥 Save", ADD_USER_DONE.name)
    }

    return builder.build()
}

fun editBossInlineMarkup(
    user: User,
    messageSourceService: MessageSourceService,
    lang: Locale
): InlineKeyboardMarkup {
    val builder = InlineKeyboardMarkupBuilder()

    if (user.deleted || user.blocked) {
        builder.addRowButton(
            messageSourceService.getMessage(RESTORE_BUTTON_TEXT, lang),
            "${RESTORE_USER.name}#${user.id}"
        )
    } else {
        builder
            .row(
                button(
                    messageSourceService.getMessage(EDIT_USER_ROLE_BUTTON_TEXT, lang),
                    "${EDIT_USER_ROLE}#${user.id}"
                ),
                button(
                    messageSourceService.getMessage(EDIT_USER_INFO_BUTTON_TEXT, lang),
                    "${EDIT_USER_INFO}#${user.id}"
                ),
            ).row(
                button(
                    messageSourceService.getMessage(BLOCK_USER_BUTTON_TEXT, lang),
                    "${BLOCK_BOSS}#${user.id}"
                ),
                button(
                    messageSourceService.getMessage(DELETE_USER_INFO_BUTTON_TEXT, lang),
                    "${DELETE_BOSS}#${user.id}"
                )
            )
    }

    builder.addRowButton(
        messageSourceService.getMessage(BACK_BUTTON_TEXT, lang),
        BACK_FROM_EDIT_BOSS.name
    )

    return builder.build()
}

fun warningDeleteBossInlineMarkup(
    messageSourceService: MessageSourceService, lang: Locale
) = InlineKeyboardMarkupBuilder()
    .row(
        button(messageSourceService.getMessage(NO_BUTTON_TEXT, lang), NO_BUTTON.name),
        button(messageSourceService.getMessage(YES_BUTTON_TEXT, lang), YES_BUTTON.name)
    )
    .addRowButton(
        messageSourceService.getMessage(BACK_BUTTON_TEXT, lang),
        BACK_FROM_EDIT_BOSS.name
    )
    .build()

fun shopsInlineMarkup(
    stores: List<Store>,
    messageSourceService: MessageSourceService,
    lang: Locale,
    page: Int = 0,
    isRoot: Boolean = false
): InlineKeyboardMarkup {
    val builder = InlineKeyboardMarkupBuilder()

    if (!isRoot) {
        builder.addRowButton(
            messageSourceService.getMessage(ADD_SHOP_BUTTON_TEXT, lang),
            "$ADD_SHOP"
        )
    }

    stores.safeSubList(page * 6, (page + 1) * 6)
        .chunked(2) {
            val buttons = mutableListOf<InlineKeyboardButton>()
            buttons.add(button("${it[0].getStoreTypeEmoji()} ${it[0].name}", "${CHOOSE_SHOP}#${it[0].id}"))
            if (it.size == 2) {
                buttons.add(button("${it[1].getStoreTypeEmoji()} ${it[1].name}", "${CHOOSE_SHOP}#${it[1].id}"))
            }
            builder.row(*buttons.toTypedArray())
        }

    val size = stores.size//7
    val maxPageSize = ceil(size / 6.0).roundToInt()
    if (stores.isNotEmpty() && maxPageSize > 1) {
        val buttons = mutableListOf<InlineKeyboardButton>()
        if (page != 0) {
            buttons.add(button("⬅", "${SHOP_PAGE_BUTTON}#${page - 1}"))
        }

        buttons.add(button("${page + 1} / $maxPageSize", DO_NOTHING.name))

        if ((page + 1) < maxPageSize) {
            buttons.add(button("➡", "${SHOP_PAGE_BUTTON}#${page + 1}"))
        }
        builder.row(*buttons.toTypedArray())
    }

    builder.addRowButton(
        messageSourceService.getMessage(BACK_BUTTON_TEXT, lang),
        BACK_TO_MAIN_MENU.name
    )
    return builder.build()
}

fun editShopInlineMarkup(
    user: User,
    store: Store,
    messageSourceService: MessageSourceService, lang: Locale
) = InlineKeyboardMarkupBuilder()
    .row(
        *listOf(
            button(
                messageSourceService.getMessage(EDIT_SHOP_NAME_BUTTON_TEXT, lang),
                "${EDIT_SHOP_NAME.name}#${store.id}"
            ),
            button(
                messageSourceService.getMessage(DELETE_SHOP_BUTTON_TEXT, lang),
                "${DELETE_SHOP.name}#${store.id}"
            )
        ).toTypedArray()
    )
    .row(
        button(
            messageSourceService.getMessage(WORKERS_BUTTON_TEXT, lang),
            "${SHOP_WORKERS.name}#${store.id}"
        )
    )
    .addRowButton(
        messageSourceService.getMessage(BACK_BUTTON_TEXT, lang),
        "${SHOP_PAGE_BUTTON.name}#0"
    ).build()

fun yesOrNoButtonsInlineMarkup(
    messageSourceService: MessageSourceService, lang: Locale,
    backCallbackData: String
) = InlineKeyboardMarkupBuilder()
    .row(
        button(messageSourceService.getMessage(NO_BUTTON_TEXT, lang), NO_BUTTON.name),
        button(messageSourceService.getMessage(YES_BUTTON_TEXT, lang), YES_BUTTON.name)
    )
    .addRowButton(
        messageSourceService.getMessage(BACK_BUTTON_TEXT, lang),
        backCallbackData
    )
    .build()

fun workersInlineMarkup(
    workers: List<User>,
    messageSourceService: MessageSourceService, lang: Locale,
    store: Store? = null,
    page: Int = 0
): InlineKeyboardMarkup {
    val builder = InlineKeyboardMarkupBuilder()
    if (store != null) {
        builder.addRowButton(
            messageSourceService.getMessage(ADD_WORKER_BUTTON_TEXT, lang),
            "${ADD_WORKER.name}#${store.id}"
        )
    }

    workers.safeSubList(page * 6, (page + 1) * 6)
        .chunked(2) {
            val buttons = mutableListOf<InlineKeyboardButton>()
            buttons.add(button("🏬 ${it[0].userName}", "${CHOOSE_WORKER}#${it[0].id}"))
            if (it.size == 2) {
                buttons.add(button("🏬 ${it[1].userName}", "${CHOOSE_WORKER}#${it[1].id}"))
            }
            builder.row(*buttons.toTypedArray())
        }

    val size = workers.size//7
    val maxPageSize = ceil(size / 6.0).roundToInt()
    if (workers.isNotEmpty() && maxPageSize > 1) {
        val buttons = mutableListOf<InlineKeyboardButton>()
        if (page != 0) {
            buttons.add(button("⬅", "${WORKERS_PAGE_BUTTON}#${page - 1}"))
        }

        buttons.add(button("${page + 1} / $maxPageSize", DO_NOTHING.name))

        if ((page + 1) < maxPageSize) {
            buttons.add(button("➡", "${WORKERS_PAGE_BUTTON}#${page + 1}"))
        }
        builder.row(*buttons.toTypedArray())
    }

    if (store != null) {
        builder.addRowButton(
            messageSourceService.getMessage(BACK_BUTTON_TEXT, lang),
            "${CHOOSE_SHOP.name}#${store.id}"
        )
    } else {
        builder.addRowButton(
            messageSourceService.getMessage(BACK_BUTTON_TEXT, lang),
            BACK_TO_MAIN_MENU.name
        )
    }

    return builder.build()
}

fun chooseShopTypeInlineMarkup(messageSourceService: MessageSourceService, lang: Locale) =
    InlineKeyboardMarkupBuilder()
        .addRowButton(
            messageSourceService.getMessage(INVENTORY_STORE_TYPE_BUTTON, lang),
            "${CHOOSE_SORE_TYPE}#${StoreType.SHOP}"
        )
        .addRowButton(
            messageSourceService.getMessage(SHOP_STORE_TYPE_BUTTON, lang),
            "${CHOOSE_SORE_TYPE}#${StoreType.INVENTORY}"
        )
        .build()