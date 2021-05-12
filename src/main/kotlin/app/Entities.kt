package app

import org.jetbrains.exposed.sql.Table

object GuestBookTable : Table() {
    val id = uuid("id").primaryKey()
    val author = text("author")
    val comment = text("comment")
}
