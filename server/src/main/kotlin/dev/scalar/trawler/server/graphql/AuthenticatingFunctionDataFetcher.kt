package dev.scalar.trawler.server.graphql

import com.expediagroup.graphql.generator.execution.FunctionDataFetcher
import graphql.schema.DataFetchingEnvironment
import kotlin.reflect.KFunction

class AuthenticatingFunctionDataFetcher(target: Any?, fn: KFunction<*>) : FunctionDataFetcher(target, fn) {
    override fun get(environment: DataFetchingEnvironment): Any? {
        val context = environment.getContext<QueryContext>()
        if (!context.isAuthenticated()) {
            throw Exception("Field ${environment.field.name} requires authentication")
        } else {
            return super.get(environment)
        }
    }
}
