package dev.scalar.trawler.server.db.util

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.BooleanColumnType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.FieldSet
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.appendTo
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.TransactionManager

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
    functionName: String,
    postfix: String = "",
    vararg params: Expression<*>
): CustomFunction<Boolean?> =
    object : CustomFunction<Boolean?>(functionName, BooleanColumnType(), *params) {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) {
            super.toQueryBuilder(queryBuilder)
            if (postfix.isNotEmpty()) {
                queryBuilder.append(postfix)
            }
        }
    }

/**
* Insert or update a-ka upsert implementation
*
* Sample usage:
*
* class SampleTable: IntIdTable("whatever"){
 *     val identifier = varchar("identifier", 32").uniqueIndex()
 *     val value = varchar("value", 32)
 * }
*
* transaction {
 *     SampleTable.insertOrUpdate({
 *         it[SampleTable.identifier] = "some identifier"
 *         it[SampleTable.value] = "inserted"
 *     }){
 *         it[SampleTable.value] = "updated"
 *     }
 * }
*
* Which is equivalent of:
*
* INSERT INTO whatever(identifier, value) VALUES('some identifier', 'inserted')
* ON DUPLICATE KEY UPDATE value = 'updated'
*/
fun <T : Table> T.insertOrUpdate(vararg keys: Column<*>, insert: T.(InsertStatement<Number>) -> Unit, update: T.(UpdateBuilder<Int>) -> Unit) {
    val updateStatement = UpsertUpdateBuilder(this).apply { update(this) }
    InsertOrUpdate<Number>(keys, updateStatement, this).apply {
        insert(this)
        execute(TransactionManager.current())
    }
}

private class UpsertUpdateBuilder(table: Table) : UpdateBuilder<Int>(StatementType.OTHER, listOf(table)) {

    val firstDataSet: List<Pair<Column<*>, Any?>> get() = values.toList()

    override fun arguments(): List<List<Pair<IColumnType, Any?>>> = QueryBuilder(true).run {
        values.forEach {
            registerArgument(it.key, it.value)
        }
        if (args.isNotEmpty()) listOf(args) else emptyList()
    }

    override fun prepareSQL(transaction: Transaction): String {
        throw IllegalStateException("prepareSQL in UpsertUpdateBuilder is not supposed to be used")
    }

    override fun PreparedStatementApi.executeInternal(transaction: Transaction): Int {
        throw IllegalStateException("executeInternal in UpsertUpdateBuilder is not supposed to be used")
    }
}

private class InsertOrUpdate<Key : Any>(
    val keys: Array<out Column<*>>,
    val update: UpsertUpdateBuilder,
    table: Table,
    isIgnore: Boolean = false
) : InsertStatement<Key>(table, isIgnore) {

    override fun arguments(): List<Iterable<Pair<IColumnType, Any?>>> {
        val updateArgs = update.arguments()
        return super.arguments().mapIndexed { index, list -> list + (updateArgs.getOrNull(index) ?: return@mapIndexed list) }
    }

    override fun prepareSQL(transaction: Transaction): String {
        val values = update.firstDataSet
        if (values.isEmpty())
            return super.prepareSQL(transaction)

        val originalStatement = super.prepareSQL(transaction)

        val updateStm = with(QueryBuilder(true)) {
            values.appendTo(this) { (col, value) ->
                append("${transaction.identity(col)}=")
                registerArgument(col, value)
            }
            toString()
        }

        return "$originalStatement ON CONFLICT (${keys.joinToString(",") { transaction.identity(it) }}) DO UPDATE SET $updateStm"
    }
}

// https://stackoverflow.com/a/52977812
class ILikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "ILIKE")
infix fun <T : String?> ExpressionWithColumnType<T>.ilike(pattern: String): Op<Boolean> = ILikeOp(this, QueryParameter(pattern, columnType))

@Suppress("UNCHECKED_CAST")
operator fun <T, S : Comparable<S>, ID : EntityID<S>?, E : S?> UpdateBuilder<T>.set(column: Column<ID>, value: E) =
    // see https://github.com/JetBrains/Exposed/issues/1275
    set(column as Column<EntityID<S>>, value)
