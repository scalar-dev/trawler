package dev.scalar.trawler.server

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.toSchema

class WidgetQuery {
    fun whoAmiI(context: QueryContext): String {
        return context.user.principal().getString("sub")
    }
}

class WidgetMutation {
    fun saveWidget(value: String): String? {
        return null
    }
}

val config = SchemaGeneratorConfig(
    supportedPackages = listOf("dev.scalar.trawler.server"),
//    hooks = object : SchemaGeneratorHooks {
//        override fun willGenerateGraphQLType(type: KType): GraphQLType? = when (type.classifier as? KClass<*>) {
//            UUID::class -> Scalars.uuid
//            Instant::class -> Scalars.dateTime
//            ByteArray::class -> Scalars.byteArray
//            Any::class -> Scalars.json
//            Map::class -> Scalars.json
//            else -> super.willGenerateGraphQLType(type)
//        }
//    },
//    dataFetcherFactoryProvider = SimpleKotlinDataFetcherFactoryProvider(objectMapper),
)

val widgetQuery = WidgetQuery()
val widgetMutation = WidgetMutation()
val schema = toSchema(
    config = config,
    queries = listOf(TopLevelObject(widgetQuery)),
    mutations = listOf(TopLevelObject(widgetMutation))
)