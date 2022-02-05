package uz.rustik.bozorbot

enum class Steps {
    START,
    CHOOSE_LANGUAGE,
    ENTER_USERNAME,
    ENTER_PASSWORD
}

enum class Role {
    ROOT,
    BOSS,
    MODERATOR,
    SELLER
}

enum class CallbackTypes {
    CHOOSE_LANGUAGE,
    MY_SHOPS,
    LOG_OUT
}

enum class LocaleMessageSourceKey {
    //    ---------Buttons(START)--------------
    LOG_OUT_BUTTON_TEXT,
    SHOP_BUTTON_TEXT,
    WORKERS_BUTTON_TEXT,
    //    ---------Buttons(END)--------------
    /**-------------------------------------------------------------**/
    //    ---------Messages(START)--------------
    LANGUAGE_CHANGED,
    MAIN_MENU,
    ENTER_USERNAME,
    ENTER_PASSWORD,
    //    ---------Messages(END)--------------
}

enum class Category {
    WALLPAPER,
    VITRINA,
    ANY
}