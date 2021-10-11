package dev.scalar.trawler.server.graphql

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.generator.toSchema
import dev.scalar.trawler.server.graphql.query.EntityQuery
import dev.scalar.trawler.server.graphql.query.ProjectQuery
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLType
import java.time.Instant
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KType

fun makeSchema(): GraphQLSchema? {
    val config = SchemaGeneratorConfig(
        supportedPackages = listOf("dev.scalar.trawler.server.graphql"),
        hooks = object : SchemaGeneratorHooks {
            override fun willGenerateGraphQLType(type: KType): GraphQLType? = when (type.classifier as? KClass<*>) {
                UUID::class -> Scalars.uuid
                Instant::class -> Scalars.dateTime
                Any::class -> Scalars.json
                else -> super.willGenerateGraphQLType(type)
            }
        },
    )

    return toSchema(
        config = config,
        queries = listOf(
            TopLevelObject(
                EntityQuery()
            ),
            TopLevelObject(
                UserQuery()
            ),
            TopLevelObject(
                ProjectQuery()
            )
        ),
        mutations = listOf(
            TopLevelObject(
                UserMutation()
            )
        )
    )
}
