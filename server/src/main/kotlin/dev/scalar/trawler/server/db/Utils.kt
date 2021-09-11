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

fun customDistinctOn(vararg expressions: Expression<*>): CustomFunction<Boolean?> = CustomBooleanFunction(
    functionName = "DISTINCT ON",
    postfix = " TRUE",
    params = *expressions
)

fun CustomBooleanFunction(
    functionName: String, postfix: String = "", vararg params: Expression<*>
): CustomFunction<Boolean?> =
    object : CustomFunction<Boolean?>(functionName, BooleanColumnType(), *params) {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) {
            super.toQueryBuilder(queryBuilder)
            if (postfix.isNotEmpty()) {
                queryBuilder.append(postfix)
            }
        }
    }
