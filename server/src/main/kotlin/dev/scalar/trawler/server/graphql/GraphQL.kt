package dev.scalar.trawler.server.graphql

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.execution.FunctionDataFetcher
import com.expediagroup.graphql.generator.execution.KotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.execution.PropertyDataFetcher
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.generator.toSchema
import dev.scalar.trawler.server.graphql.query.EntityQuery
import dev.scalar.trawler.server.graphql.query.OntologyQuery
import dev.scalar.trawler.server.graphql.query.ProjectQuery
import dev.scalar.trawler.server.graphql.query.UserQuery
import graphql.schema.DataFetcherFactory
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLType
import java.time.Instant
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType

fun makeSchema(): GraphQLSchema? {
    val config = SchemaGeneratorConfig(
        supportedPackages = listOf("dev.scalar.trawler.server.graphql"),
        dataFetcherFactoryProvider = object : KotlinDataFetcherFactoryProvider {
            override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>): DataFetcherFactory<Any?> {
                return DataFetcherFactory {
                    if (it.fieldDefinition.directivesByName.containsKey("unauthenticated")) {
                        FunctionDataFetcher(target, kFunction)
                    } else {
                        AuthenticatingFunctionDataFetcher(target, kFunction)
                    }
                }
            }

            override fun propertyDataFetcherFactory(
                kClass: KClass<*>,
                kProperty: KProperty<*>
            ): DataFetcherFactory<Any?> {
                return DataFetcherFactory {
                    PropertyDataFetcher(kProperty.getter)
                }
            }
        },
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
            ),
            TopLevelObject(
                OntologyQuery()
            )
        ),
        mutations = listOf(
            TopLevelObject(
                UserMutation()
            )
        )
    )
}
