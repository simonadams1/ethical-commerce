package app

import io.javalin.core.security.Role

object ENV {
    val DATABASE_URL = System.getenv("DB_URL")
    val DATABASE_USER = System.getenv("DATABASE_USER")
    val DATABASE_PASSWORD = System.getenv("DATABASE_PASSWORD")
    val HOST = System.getenv("HOST")
    val PORT = System.getenv("PORT")?.toInt() ?: 8080
    val DEMO_MODE = System.getenv("DEMO_MODE") != null
}

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
    PUBLIC(1),
    PUBLIC_DELISTED(2);

    companion object {
        fun fromId(id: Int) = values().first { it.id == id }
    }
}
