package app

object ENV {
    val DATABASE_URL = System.getenv("DB_URL")
    val DATABASE_USER = System.getenv("DATABASE_USER")
    val DATABASE_PASSWORD = System.getenv("DATABASE_PASSWORD")
    val HOST = System.getenv("HOST")
    val PORT = System.getenv("PORT")?.toInt() ?: 8080
}
