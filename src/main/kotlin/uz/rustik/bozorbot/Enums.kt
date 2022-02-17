package uz.rustik.bozorbot

enum class Steps {
    START,
    CHOOSE_LANGUAGE,
    ENTER_USERNAME,
    ENTER_PASSWORD,
    LOG_IN,
    ADD_BOSS_USERNAME,
    ADD_BOSS_PASSWORD,
    EDIT_BOSS_USERNAME,
    EDIT_BOSS_PASSWORD,
    MAIN_MENU,
    ADD_SHOP,
    INPUT_SHOP_NAME,
    INPUT_USERNAME,
    INPUT_USER_PASSWORD,
}

enum class Role {
    ROOT,
    BOSS,
    MODERATOR,
    SELLER
}

enum class CallbackTypes {
    MAIN_MENU,
    CHOOSE_LANGUAGE,
    MY_SHOPS,
    MY_WORKERS,
    ADD_ORDER,
    LOG_OUT,
    BOSS_PAGE_BUTTON,
    ADD_BOSS,
    ADD_BOSS_DONE,
    ADD_BOSS_CHOOSE_ROLE,
    EDIT_BOSS,
    DO_NOTHING,
    BLOCK_BOSS,
    DELETE_BOSS,
    EDIT_USER_INFO,
    EDIT_USER_ROLE,
    BACK_FROM_EDIT_BOSS,
    BACK_TO_MAIN_MENU,
    YES_BUTTON,
    NO_BUTTON,
    BACK_TO_EDIT_BOSS,
    RESTORE_USER,
    SHOP_PAGE_BUTTON,
    CHOOSE_SHOP,
    ADD_SHOP,
    EDIT_SHOP_NAME,
    DELETE_SHOP,
    SHOP_WORKERS,
    ADD_WORKER,
    CHOOSE_WORKER,
    WORKERS_PAGE_BUTTON,
    CHOOSE_SORE_TYPE,
    ADD_USER_DONE,
}

enum class LocaleMessageSourceKey {
    //    ---------Buttons(START)--------------
    LOG_OUT_BUTTON_TEXT,
    SHOP_BUTTON_TEXT,
    WORKERS_BUTTON_TEXT,
    ADD_ORDER_BUTTON_TEXT,
    BOSS_BUTTON_TEXT,
    ADD_BOSS_BUTTON_TEXT,
    BLOCK_USER_BUTTON_TEXT,
    EDIT_USER_INFO_BUTTON_TEXT,
    EDIT_USER_ROLE_BUTTON_TEXT,
    DELETE_USER_INFO_BUTTON_TEXT,
    BACK_BUTTON_TEXT,
    RESTORE_BUTTON_TEXT,
    ADD_SHOP_BUTTON_TEXT,
    ADD_WORKER_BUTTON_TEXT,
    EDIT_SHOP_NAME_BUTTON_TEXT,
    DELETE_SHOP_BUTTON_TEXT,
    DELETE_WORKER_BUTTON_TEXT,
    INVENTORY_STORE_TYPE_BUTTON,
    SHOP_STORE_TYPE_BUTTON,
    //    ---------Buttons(END)--------------
    /**-------------------------------------------------------------**/
    //    ---------Messages(START)--------------
    LANGUAGE_CHANGED,
    MAIN_MENU,
    ENTER_USERNAME,
    ENTER_PASSWORD,
    NEW_BOSS_USERNAME,
    NEW_BOSS_PASSWORD,
    LOGED_OUT,
    WARNING_DELETE_BOSS,
    NO_BUTTON_TEXT,
    YES_BUTTON_TEXT,
    WARNING_BLOCK_BOSS,
    BOSSES_MENU,
    BOSS_MENU,
    SHOPS_MENU,
    ENTER_SHOP_NAME,
    SHOP_INFO_TEXT,
    SHOP_WORKERS_TEXT,
    CHOOSE_STORE_TYPE_TEXT,
    INPUT_USER_PASSWORD_TEXT,
    INPUT_USERNAME_TEXT,
    //    ---------Messages(END)--------------
    /**-------------------------------------------------------------**/
    //   ---------Error Messages(START)--------------
    USER_EXISTS_EXCEPTION,
    //   ---------Error Messages(END)--------------

}

enum class Category {
    WALLPAPER,
    VITRINA,
    ANY
}

enum class StoreType {
    SHOP,
    INVENTORY
}