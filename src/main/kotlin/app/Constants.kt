package app

import io.javalin.core.security.Role

const val LOGIN_COOKIE_NAME = "login_token"
const val SESSION_USER = "session_user"

enum class USER_ROLES(val id: Int, val power: Int) : Role {
    GUEST(0, 0),
    MEMBER(2, 100),
    MODERATOR(3, 500),
    ADMINISTRATOR(1, 1000);

    companion object {
        fun fromId(id: Int) = values().first { it.id == id }
    }
}

enum class MODERATION_STATUS(val id: Int) {
    PENDING(0),
    APPROVED(1),
    REJECTED(2);

    companion object {
        fun fromId(id: Int) = values().first { it.id == id }
    }
}

enum class ACCESS_STATUS(val id: Int) {
    PRIVATE(0),
    PUBLIC(1);

    companion object {
        fun fromId(id: Int) = values().first { it.id == id }
    }
}
