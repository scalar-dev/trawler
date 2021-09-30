package dev.scalar.trawler.server.graphql

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.generator.toSchema
import graphql.schema.GraphQLType
import java.time.Instant
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType

val config = SchemaGeneratorConfig(
    supportedPackages = listOf("dev.scalar.trawler.server.graphql"),
    hooks = object : SchemaGeneratorHooks {
        override fun willGenerateGraphQLType(type: KType): GraphQLType? = when (type.classifier as? KClass<*>) {
            UUID::class -> Scalars.uuid
            Instant::class -> Scalars.dateTime
//            ByteArray::class -> Scalars.byteArray
            Any::class -> Scalars.json
//            Map::class -> Scalars.json
            else -> super.willGenerateGraphQLType(type)
        }
    },

//    dataFetcherFactoryProvider = SimpleKotlinDataFetcherFactoryProvider(objectMapper),
)

val query = EntityQuery()
val mutation = Mutations()

val schema = toSchema(
    config = config,
    queries = listOf(TopLevelObject(query)),
    mutations = listOf(TopLevelObject(mutation))
)
