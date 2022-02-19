package uz.rustik.bozorbot.handler.callback

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.bots.AbsSender
import uz.rustik.bozorbot.*
import uz.rustik.bozorbot.LocaleMessageSourceKey.*
import uz.ugnis.tgbotlib.*
import uz.ugnis.tgbotlib.InlineKeyboardMarkupBuilder.Companion.emptyInlineMarkup
import java.util.*

@Service
class MainMenuCallbackHandler(
    private val messageSourceService: MessageSourceService,
    private val userService: UserServiceImpl,
    private val storeService: StoreService,
    private val myChatService: MyChatService,
) : CallbackHandler {
    @Transactional
    override fun callbackHandle(callback: CallbackQuery, sender: AbsSender) {
        val message = callback.message
        val chatId = message.chatId.toString()
        val chat = myChatService.findByChatId(message.chatId)
        val user = chat.user!!
        val lang = Locale(chat.language)
        val answerCallbackQuery = AnswerCallbackQuery()
        answerCallbackQuery.callbackQueryId = callback.id

        val keys = callback.data.split("#")

        val sendMessage = SendMessage()
        sendMessage.chatId = chatId
        sendMessage.enableHtml(true)

        val editMessage = EditMessageText()
        editMessage.chatId = chatId
        editMessage.messageId = message.messageId
        editMessage.enableHtml(true)

        val editButton = EditMessageReplyMarkup()
        editButton.chatId = chatId
        editButton.messageId = message.messageId

        when (keys[0]) {
            CallbackTypes.BACK_FROM_EDIT_BOSS.name,
            CallbackTypes.BOSS_PAGE_BUTTON.name -> {
                if (!user.roles.isRoot()) {
                    answerCallbackQuery.showAlert = true
                    answerCallbackQuery.text = "Not Allowed!!"
                    sender.answerCallBackQuery(answerCallbackQuery)
                    return
                }
                val page = if (keys.size >= 2) keys[1].toInt() else 0

                editMessage.text = messageSourceService.getMessage(BOSSES_MENU, lang)
                editMessage.replyMarkup = bossInlineMarkup(
                    userService.findAllBossUsers(),
                    messageSourceService, lang, page
                )
                sender.editMessage(editMessage)
            }

            CallbackTypes.ADD_BOSS.name -> {
                if (!user.roles.isRoot()) {
                    answerCallbackQuery.showAlert = true
                    answerCallbackQuery.text = "Not Allowed!!"
                    sender.answerCallBackQuery(answerCallbackQuery)
                    return
                }
                chat.botMessageId?.run { sender.deleteMessage(this, chatId) }
                chat.chatStep = Steps.ADD_BOSS_USERNAME.name
                sendMessage.text = messageSourceService
                    .getMessage(LocaleMessageSourceKey.NEW_BOSS_USERNAME, lang)
                chat.botMessageId = sender.sendMessage(sendMessage)
                myChatService.save(chat)
            }
            CallbackTypes.MY_SHOPS.name -> {
                val shopList = storeService.getAllUsersStoreList(user)
                editMessage.text = messageSourceService.getMessage(SHOPS_MENU, lang)
                editMessage.replyMarkup = shopsInlineMarkup(
                    shopList,
                    messageSourceService, lang,
                    isRoot = user.roles.isRoot()
                )
                sender.editMessage(editMessage)
            }
            CallbackTypes.ADD_ORDER.name -> {

            }
            CallbackTypes.LOG_OUT.name -> {
                chat.user = null
                chat.chatStep = Steps.LOG_IN.name
                answerCallbackQuery.text = messageSourceService.getMessage(LOGED_OUT, lang)
                sender.deleteMessage(message.messageId, chatId)
                chat.botMessageId?.run { sender.deleteMessage(this, chatId) }
                chat.botMessageId = null
                myChatService.save(chat)
            }
            CallbackTypes.DO_NOTHING.name -> {

            }
            CallbackTypes.USER_CHOOSE_ROLE.name,
            CallbackTypes.ADD_BOSS_CHOOSE_ROLE.name -> {
                val role = Role.valueOf(keys[1])
                val split = chat.note.split("^")
                val roles = split[1].split("!")
                    .filter { it.isNotEmpty() }
                    .map { Role.valueOf(it) }.toMutableList()

                if (roles.contains(role)) {
                    roles.remove(role)
                } else {
                    roles.add(role)
                }
                editButton.replyMarkup = if (keys[0] == CallbackTypes.USER_CHOOSE_ROLE.name) {
                    newUserChooseRoleInlineMarkup(roles)
                } else {
                    newBossChooseRoleInlineMarkup(roles)
                }
                chat.note = "${split[0]}^${roles.joinToString("!")}"
                myChatService.save(chat)
                sender.editMessageReplyMarkup(editButton)
            }
            CallbackTypes.ADD_BOSS_DONE.name -> {
                val split = chat.note.split("^")
                val roles = split[1].split("!")
                    .filter { it.isNotEmpty() }.toMutableList()

                if (chat.chatStep == CallbackTypes.EDIT_USER_ROLE.name) {
                    val id = split[0].toLong()
                    val bossUser = userService.findByUserId(id)
                    bossUser.roles = roles
                    userService.save(bossUser)
                    chat.botMessageId = message.messageId
                    editMessage.text = messageSourceService.getMessage(BOSS_MENU, lang)
                    editMessage.replyMarkup = editBossInlineMarkup(bossUser, messageSourceService, lang)
                    sender.editMessage(editMessage)
                } else {
                    sender.deleteMessage(message.messageId, chatId)
                    chat.botMessageId?.run { sender.deleteMessage(this, chatId) }

//                    .map { Role.valueOf(it) }.toMutableList()
                    val splitInfo = split[0].split("#")
                    var newUser = User(
                        splitInfo[0],
                        splitInfo[1],
                        roles = roles,
                        boss = user
                    )
                    answerCallbackQuery.text = "✅ Saved."
                    newUser = userService.save(newUser)

                    sendMessage.text = messageSourceService.getMessage(MAIN_MENU, lang)
                    sendMessage.replyMarkup =
                        mainMenuInlineMarkup(user.roles, messageSourceService, lang)
                    chat.botMessageId = sender.sendMessage(sendMessage)
                }
                chat.chatStep = CallbackTypes.MAIN_MENU.name

                myChatService.save(chat)
            }
            CallbackTypes.EDIT_BOSS.name -> {
                if (!user.roles.isRoot()) {
                    answerCallbackQuery.showAlert = true
                    answerCallbackQuery.text = "Not Allowed!!"
                    sender.answerCallBackQuery(answerCallbackQuery)
                    return
                }
                val userId = keys[1].toLong()
                val userBoss = userService.findByUserId(userId)
                editMessage.text = messageSourceService.getMessage(BOSS_MENU, lang)
                editMessage.replyMarkup = editBossInlineMarkup(userBoss, messageSourceService, lang)
                sender.editMessage(editMessage)
            }
            CallbackTypes.EDIT_USER_INFO.name -> {
                editMessage.text = messageSourceService
                    .getMessage(LocaleMessageSourceKey.NEW_BOSS_USERNAME, lang)
                editMessage.replyMarkup = emptyInlineMarkup()
                sender.editMessage(editMessage)
                chat.chatStep = Steps.EDIT_BOSS_USERNAME.name
                chat.note = keys[1]
                chat.botMessageId = message.messageId
                myChatService.save(chat)
            }
            CallbackTypes.BACK_TO_MAIN_MENU.name -> {
                chat.chatStep = CallbackTypes.MAIN_MENU.name
                chat.botMessageId = message.messageId
                editMessage.text = messageSourceService.getMessage(LocaleMessageSourceKey.MAIN_MENU, lang)
                editMessage.replyMarkup = mainMenuInlineMarkup(user.roles, messageSourceService, lang)
                sender.editMessage(editMessage)
                myChatService.save(chat)
            }
            CallbackTypes.EDIT_USER_ROLE.name -> {
                val id = keys[1].toLong()
                val bossUser = userService.findByUserId(id)
                editMessage.text = "roles"
                editMessage.replyMarkup = newBossChooseRoleInlineMarkup(bossUser.roles.map { Role.valueOf(it) })
                sender.editMessage(editMessage)
                chat.chatStep = CallbackTypes.EDIT_USER_ROLE.name
                chat.botMessageId = message.messageId
                chat.note = "$id^${bossUser.roles.joinToString("!")}"
                myChatService.save(chat)
            }
            CallbackTypes.DELETE_BOSS.name -> {
                editMessage.text = messageSourceService.getMessage(WARNING_DELETE_BOSS, lang)
                editMessage.replyMarkup = warningDeleteBossInlineMarkup(messageSourceService, lang)
                chat.chatStep = CallbackTypes.DELETE_BOSS.name
                chat.botMessageId = message.messageId
                chat.note = keys[1]
                myChatService.save(chat)
                sender.editMessage(editMessage)
            }
            CallbackTypes.BLOCK_BOSS.name -> {
                editMessage.text = messageSourceService.getMessage(WARNING_BLOCK_BOSS, lang)
                editMessage.replyMarkup = warningDeleteBossInlineMarkup(messageSourceService, lang)
                chat.chatStep = CallbackTypes.DELETE_BOSS.name
                chat.botMessageId = message.messageId
                chat.note = keys[1]
                myChatService.save(chat)
                sender.editMessage(editMessage)
            }
            CallbackTypes.NO_BUTTON.name -> {
                if (chat.chatStep == CallbackTypes.DELETE_SHOP.name) {
                    val id = chat.note.toLong()
                    val shop = storeService.findById(id)
                    editMessage.replyMarkup = editShopInlineMarkup(
                        user, shop, messageSourceService, lang
                    )
                    editMessage.text = messageSourceService.getMessage(SHOP_INFO_TEXT, lang)
                    chat.chatStep = Steps.MAIN_MENU.name
                    chat.note = ""
                    myChatService.save(chat)
                    sender.editMessage(editMessage)
                } else {
                    val id = chat.note.toLong()
                    val bossUser = userService.findByUserId(id)
                    editMessage.text = "fadsfds"
                    editMessage.replyMarkup = editBossInlineMarkup(bossUser, messageSourceService, lang)
                    chat.chatStep = CallbackTypes.MAIN_MENU.name
                    sender.editMessage(editMessage)
                }
            }
            CallbackTypes.YES_BUTTON.name -> {
                if (chat.chatStep == CallbackTypes.DELETE_SHOP.name) {
                    val id = chat.note.toLong()
                    val shop = storeService.findById(id)
                    shop.deleted = true
                    storeService.save(shop)
                    chat.chatStep = Steps.MAIN_MENU.name
                    chat.note = ""
                    myChatService.save(chat)
                    answerCallbackQuery.text = "Deleted ${shop.name}"
                    val allShops = storeService.getAllUsersStoreList(user)
                    editMessage.replyMarkup = shopsInlineMarkup(
                        allShops, messageSourceService, lang, isRoot = user.roles.isRoot()
                    )
                    editMessage.text = messageSourceService.getMessage(SHOPS_MENU, lang)
                    sender.editMessage(editMessage)
                }
                if (chat.chatStep == CallbackTypes.DELETE_BOSS.name) {
                    val id = chat.note.toLong()
                    val bossUser = userService.findByUserId(id)
                    val workers = bossUser.workers
                    if (!workers.isNullOrEmpty()) {
                        workers.forEach {
                            it.id?.run { userService.deleteById(this) }
                        }
                    }
                    userService.deleteById(id)
                    editMessage.text = messageSourceService.getMessage(BOSSES_MENU, lang)
                    editMessage.replyMarkup =
                        bossInlineMarkup(userService.findAllBossUsers(), messageSourceService, lang)
                    sender.editMessage(editMessage)
                }
                if (chat.chatStep == CallbackTypes.BLOCK_BOSS.name) {
                    val id = chat.note.toLong()
                    val bossUser = userService.findByUserId(id)
                    val workers = bossUser.workers
                    if (!workers.isNullOrEmpty()) {
                        workers.forEach {
                            it.id?.run { userService.blockById(this) }
                        }
                    }
                    userService.blockById(id)
                    editMessage.text = messageSourceService.getMessage(BOSSES_MENU, lang)
                    editMessage.replyMarkup =
                        bossInlineMarkup(userService.findAllBossUsers(), messageSourceService, lang)
                    sender.editMessage(editMessage)
                }
            }
            CallbackTypes.BACK_TO_EDIT_BOSS.name -> {
                val id = chat.note.toLong()
                val bossUser = userService.findByUserId(id)
                chat.chatStep = CallbackTypes.MAIN_MENU.name
                chat.botMessageId = message.messageId
                myChatService.save(chat)
                editMessage.text = messageSourceService.getMessage(BOSS_MENU, lang)
                editMessage.replyMarkup = editBossInlineMarkup(bossUser, messageSourceService, lang)
                sender.editMessage(editMessage)
            }
            CallbackTypes.RESTORE_USER.name -> {
                if (!user.roles.isRoot()) {
                    answerCallbackQuery.showAlert = true
                    answerCallbackQuery.text = "Not Allowed!!"
                    sender.answerCallBackQuery(answerCallbackQuery)
                    return
                }
                val id = keys[1].toLong()
                var bossUser = userService.findByUserId(id)
                when {
                    bossUser.blocked -> {
                        bossUser.workers?.forEach {
                            it.id?.run { userService.setBlockById(this, false) }
                        }
                        bossUser.blocked = false
                    }
                    bossUser.deleted -> {
                        bossUser.workers?.forEach {
                            it.id?.run { userService.setDeleteById(this, false) }
                        }
                        bossUser.deleted = false
                    }
                }
                bossUser = userService.save(bossUser)
                editButton.replyMarkup = editBossInlineMarkup(bossUser, messageSourceService, lang)
                sender.editMessageReplyMarkup(editButton)
            }
            CallbackTypes.ADD_SHOP.name -> {
                editMessage.replyMarkup = chooseShopTypeInlineMarkup(messageSourceService, lang)
                editMessage.text = messageSourceService.getMessage(CHOOSE_STORE_TYPE_TEXT, lang)
                sender.editMessage(editMessage)
            }
            CallbackTypes.SHOP_PAGE_BUTTON.name -> {
                val page = if (keys.size >= 2) keys[1].toInt() else 0
                editMessage.text = messageSourceService.getMessage(BOSSES_MENU, lang)
                editMessage.replyMarkup = shopsInlineMarkup(
                    storeService.getAllUsersStoreList(user),
                    messageSourceService, lang, page,
                    user.roles.isRoot()
                )
                sender.editMessage(editMessage)
            }
            CallbackTypes.CHOOSE_SHOP.name -> {
                val id = keys[1].toLong()
                val bossShop = storeService.findById(id)
                editMessage.replyMarkup = editShopInlineMarkup(user, bossShop, messageSourceService, lang)
                editMessage.text = messageSourceService.getMessage(SHOP_INFO_TEXT, lang)
                sender.editMessage(editMessage)
            }
            CallbackTypes.EDIT_SHOP_NAME.name -> {
                chat.note = keys[1]
                chat.botMessageId = message.messageId
                chat.chatStep = Steps.INPUT_SHOP_NAME.name
                myChatService.save(chat)
                editMessage.text = messageSourceService.getMessage(ENTER_SHOP_NAME, lang)
                editMessage.replyMarkup = emptyInlineMarkup()
                sender.editMessage(editMessage)
            }
            CallbackTypes.DELETE_SHOP.name -> {
                chat.note = keys[1]
                chat.chatStep = CallbackTypes.DELETE_SHOP.name

                editButton.replyMarkup = yesOrNoButtonsInlineMarkup(
                    messageSourceService, lang,
                    "${CallbackTypes.CHOOSE_SHOP.name}#${keys[1]}"
                )
                myChatService.save(chat)
                sender.editMessageReplyMarkup(editButton)
            }
            CallbackTypes.MY_WORKERS.name,
            CallbackTypes.SHOP_WORKERS.name -> {
                val id = keys[1].toLongOrNull()
                val workers = (id?.run {
                    storeService.findById(this).workers
                } ?: user.workers) ?: listOf()
                chat.note = keys[1]
                myChatService.save(chat)
                editMessage.replyMarkup = workersInlineMarkup(
                    workers, messageSourceService, lang, id?.run { storeService.findById(this) })
                editMessage.text = messageSourceService.getMessage(SHOP_WORKERS_TEXT, lang)
                sender.editMessage(editMessage)
            }
            CallbackTypes.WORKERS_PAGE_BUTTON.name -> {
                val id = chat.note.toLongOrNull()
                val workers = (id?.run {
                    storeService.findById(this).workers
                } ?: user.workers) ?: listOf()
                myChatService.save(chat)
                val page = if (keys.size >= 2) keys[1].toInt() else 0
                editButton.replyMarkup = workersInlineMarkup(
                    workers, messageSourceService, lang, id?.run { storeService.findById(this) })
                sender.editMessageReplyMarkup(editButton)
            }
            CallbackTypes.ADD_WORKER.name -> {
                val id = keys[1].toLong()
                val store = storeService.findById(id)
                editMessage.replyMarkup = emptyInlineMarkup()
                editMessage.text = messageSourceService.getMessage(INPUT_USERNAME_TEXT, lang)
                sender.editMessage(editMessage)

                chat.chatStep = Steps.INPUT_USERNAME.name
                chat.note = "${store.id}"
                myChatService.save(chat)
            }
            CallbackTypes.CHOOSE_SORE_TYPE.name -> {
                println(keys[1])
                chat.note = keys[1]
                chat.chatStep = Steps.INPUT_SHOP_NAME.name
                editMessage.text = messageSourceService.getMessage(ENTER_SHOP_NAME, lang)
                editMessage.replyMarkup = emptyInlineMarkup()
                myChatService.save(chat)
                sender.editMessage(editMessage)
            }
            CallbackTypes.ADD_USER_DONE.name -> {
                val split = chat.note.split("^")
                val roles = split[1].split("!")
                    .filter { it.isNotEmpty() }.toMutableList()

                if (chat.chatStep == CallbackTypes.EDIT_USER_ROLE.name) {
                    val id = split[0].toLong()
                    val bossUser = userService.findByUserId(id)
                    bossUser.roles = roles
                    userService.save(bossUser)
                    chat.botMessageId = message.messageId
                    editMessage.text = messageSourceService.getMessage(BOSS_MENU, lang)
                    editMessage.replyMarkup = editBossInlineMarkup(bossUser, messageSourceService, lang)
                    sender.editMessage(editMessage)
                } else {
                    sender.deleteMessage(message.messageId, chatId)
                    chat.botMessageId?.run { sender.deleteMessage(this, chatId) }

//                    .map { Role.valueOf(it) }.toMutableList()
                    val splitInfo = split[0].split("#")

                    var newUser = User(
                        splitInfo[1],
                        splitInfo[2],
                        roles = roles,
                        boss = user
                    )
                    answerCallbackQuery.text = "✅ Saved."
                    newUser = userService.saveWorker(newUser, splitInfo[0].toLong())

                    sendMessage.text = messageSourceService.getMessage(SHOP_INFO_TEXT, lang)
                    sendMessage.replyMarkup =
                        editShopInlineMarkup(user, newUser.store!!, messageSourceService, lang)
                    chat.botMessageId = sender.sendMessage(sendMessage)
                }
                chat.chatStep = CallbackTypes.MAIN_MENU.name

                myChatService.save(chat)
            }
            else -> {
                answerCallbackQuery.text = "Error mapping"
            }
        }

        sender.answerCallBackQuery(answerCallbackQuery)
    }

    override fun callbackTypes(): List<String> {
        return listOf(
            CallbackTypes.MAIN_MENU.name,
            CallbackTypes.MY_SHOPS.name,
            CallbackTypes.MY_WORKERS.name,
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
            CallbackTypes.BACK_TO_EDIT_BOSS.name,
            CallbackTypes.RESTORE_USER.name,
            CallbackTypes.ADD_SHOP.name,
            CallbackTypes.SHOP_PAGE_BUTTON.name,
            CallbackTypes.CHOOSE_SHOP.name,
            CallbackTypes.EDIT_SHOP_NAME.name,
            CallbackTypes.DELETE_SHOP.name,
            CallbackTypes.SHOP_WORKERS.name,
            CallbackTypes.WORKERS_PAGE_BUTTON.name,
            CallbackTypes.ADD_WORKER.name,
            CallbackTypes.CHOOSE_SORE_TYPE.name,
            CallbackTypes.ADD_USER_DONE.name,
            CallbackTypes.USER_CHOOSE_ROLE.name
        )
    }
}