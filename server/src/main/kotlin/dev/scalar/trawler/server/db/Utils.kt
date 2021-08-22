package dev.scalar.trawler.server.db

import org.jetbrains.exposed.sql.*

inline fun FieldSet.selectForUpdate(where: SqlExpressionBuilder.() -> Op<Boolean>): Query =
    selectForUpdate(SqlExpressionBuilder.where())

fun FieldSet.selectForUpdate(where: Op<Boolean>): Query = SelectForUpdateQuery(this, where)

open class SelectForUpdateQuery(set: FieldSet, where: Op<Boolean>?) : Query(set, where) {
    override fun prepareSQL(transaction: Transaction): String {
        val sql = super.prepareSQL(transaction)
        return "$sql for update"
    }
}