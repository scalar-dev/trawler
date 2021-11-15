import { TypedDocumentNode as DocumentNode } from '@graphql-typed-document-node/core';
export type Maybe<T> = T | null;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
  /** DataTime scalar */
  DateTime: any;
  /** A JSON blob */
  Json: any;
  /** Long type */
  Long: any;
  /** UUID */
  UUID: any;
};

export type ApiKey = {
  __typename?: 'ApiKey';
  accountId: Scalars['UUID'];
  createdAt: Scalars['DateTime'];
  description?: Maybe<Scalars['String']>;
  id: Scalars['UUID'];
  secret?: Maybe<Scalars['String']>;
};

export type AuthenticatedUser = {
  __typename?: 'AuthenticatedUser';
  jwt: Scalars['String'];
};

export type Entity = {
  __typename?: 'Entity';
  facetLog: Array<FacetLog>;
  facets: Array<Facet>;
  id: Scalars['UUID'];
  projectId: Scalars['UUID'];
  timeSeries?: Maybe<FacetTimeSeries>;
  type: Scalars['String'];
  typeName: Scalars['String'];
  urn: Scalars['String'];
};


export type EntityFacetLogArgs = {
  facets: Array<Scalars['String']>;
};


export type EntityTimeSeriesArgs = {
  facet: Scalars['String'];
};

export type EntityType = {
  __typename?: 'EntityType';
  id: Scalars['UUID'];
  isRootType: Scalars['Boolean'];
  name: Scalars['String'];
  uri: Scalars['String'];
};

export type Facet = {
  __typename?: 'Facet';
  metaType: Scalars['String'];
  name: Scalars['String'];
  uri: Scalars['String'];
  value?: Maybe<Scalars['Json']>;
  version: Scalars['Long'];
};

export type FacetLog = {
  __typename?: 'FacetLog';
  createdAt: Scalars['DateTime'];
  entities: Array<Entity>;
  id: Scalars['UUID'];
  name: Scalars['String'];
  urn: Scalars['String'];
  version: Scalars['Long'];
};

export type FacetTimeSeries = {
  __typename?: 'FacetTimeSeries';
  name: Scalars['String'];
  points: Array<FacetTimeSeriesPoint>;
  urn: Scalars['String'];
};

export type FacetTimeSeriesPoint = {
  __typename?: 'FacetTimeSeriesPoint';
  timestamp: Scalars['DateTime'];
  value: Scalars['Float'];
};

export type FacetType = {
  __typename?: 'FacetType';
  id: Scalars['UUID'];
  indexTimeSeries: Scalars['Boolean'];
  isRootType: Scalars['Boolean'];
  jsonSchema?: Maybe<Scalars['Json']>;
  metaType: Scalars['String'];
  name: Scalars['String'];
  uri: Scalars['String'];
};

export type FilterInput = {
  uri: Scalars['String'];
  value: Array<Scalars['String']>;
};

export type Mutation = {
  __typename?: 'Mutation';
  createApiKey: ApiKey;
  createUser: Scalars['UUID'];
  login: AuthenticatedUser;
};


export type MutationCreateUserArgs = {
  email: Scalars['String'];
  password: Scalars['String'];
};


export type MutationLoginArgs = {
  email: Scalars['String'];
  password: Scalars['String'];
};

export type Project = {
  __typename?: 'Project';
  id: Scalars['UUID'];
  name: Scalars['String'];
  slug: Scalars['String'];
};

export type Query = {
  __typename?: 'Query';
  apiKeys: Array<ApiKey>;
  entity?: Maybe<Entity>;
  entityGraph: Array<Entity>;
  entityTypes: Array<EntityType>;
  facetTypes: Array<FacetType>;
  me?: Maybe<User>;
  projects: Array<Project>;
  search: Array<Entity>;
};


export type QueryEntityArgs = {
  id: Scalars['UUID'];
};


export type QueryEntityGraphArgs = {
  d: Scalars['Int'];
  id: Scalars['UUID'];
};


export type QueryEntityTypesArgs = {
  project: Scalars['String'];
};


export type QueryFacetTypesArgs = {
  project: Scalars['String'];
};


export type QuerySearchArgs = {
  filters: Array<FilterInput>;
  project: Scalars['String'];
};

export type User = {
  __typename?: 'User';
  email: Scalars['String'];
  firstName?: Maybe<Scalars['String']>;
  lastName?: Maybe<Scalars['String']>;
};

export type MeQueryVariables = Exact<{ [key: string]: never; }>;


export type MeQuery = { __typename?: 'Query', me?: { __typename?: 'User', email: string, firstName?: string | null | undefined, lastName?: string | null | undefined } | null | undefined, projects: Array<{ __typename?: 'Project', id: any, name: string, slug: string }> };

export type SearchByNameQueryVariables = Exact<{
  search: Array<Scalars['String']> | Scalars['String'];
  project: Scalars['String'];
}>;


export type SearchByNameQuery = { __typename?: 'Query', search: Array<{ __typename?: 'Entity', id: any, type: string, typeName: string, facets: Array<{ __typename?: 'Facet', name: string, uri: string, value?: any | null | undefined }> }> };

export type SearchByTypeQueryVariables = Exact<{
  type: Array<Scalars['String']> | Scalars['String'];
  project: Scalars['String'];
}>;


export type SearchByTypeQuery = { __typename?: 'Query', search: Array<{ __typename?: 'Entity', id: any, urn: string, type: string, typeName: string, facets: Array<{ __typename?: 'Facet', name: string, uri: string, value?: any | null | undefined }> }> };

export type FacetTimeSeriesQueryVariables = Exact<{
  id: Scalars['UUID'];
  facet: Scalars['String'];
}>;


export type FacetTimeSeriesQuery = { __typename?: 'Query', entity?: { __typename?: 'Entity', timeSeries?: { __typename?: 'FacetTimeSeries', name: string, urn: string, points: Array<{ __typename?: 'FacetTimeSeriesPoint', timestamp: any, value: number }> } | null | undefined } | null | undefined };

export type FacetLogQueryVariables = Exact<{
  id: Scalars['UUID'];
  facets: Array<Scalars['String']> | Scalars['String'];
}>;


export type FacetLogQuery = { __typename?: 'Query', entity?: { __typename?: 'Entity', facetLog: Array<{ __typename?: 'FacetLog', id: any, createdAt: any, name: string, urn: string, version: any, entities: Array<{ __typename?: 'Entity', id: any, facets: Array<{ __typename?: 'Facet', name: string, uri: string, value?: any | null | undefined }> }> }> } | null | undefined };

export type EntityQueryVariables = Exact<{
  id: Scalars['UUID'];
  depth: Scalars['Int'];
}>;


export type EntityQuery = { __typename?: 'Query', entityGraph: Array<{ __typename?: 'Entity', id: any, urn: string, type: string, typeName: string, facets: Array<{ __typename?: 'Facet', name: string, uri: string, value?: any | null | undefined, metaType: string }> }> };

export type OntologyQueryVariables = Exact<{
  project: Scalars['String'];
}>;


export type OntologyQuery = { __typename?: 'Query', entityTypes: Array<{ __typename?: 'EntityType', id: any, name: string, uri: string, isRootType: boolean }>, facetTypes: Array<{ __typename?: 'FacetType', id: any, name: string, uri: string, metaType: string, jsonSchema?: any | null | undefined, indexTimeSeries: boolean, isRootType: boolean }> };

export type CreateApiKeyMutationVariables = Exact<{ [key: string]: never; }>;


export type CreateApiKeyMutation = { __typename?: 'Mutation', createApiKey: { __typename?: 'ApiKey', id: any, description?: string | null | undefined, createdAt: any, secret?: string | null | undefined } };

export type GetApiKeysQueryVariables = Exact<{ [key: string]: never; }>;


export type GetApiKeysQuery = { __typename?: 'Query', apiKeys: Array<{ __typename?: 'ApiKey', id: any, description?: string | null | undefined, createdAt: any }> };

export type LoginMutationVariables = Exact<{
  email: Scalars['String'];
  password: Scalars['String'];
}>;


export type LoginMutation = { __typename?: 'Mutation', login: { __typename?: 'AuthenticatedUser', jwt: string } };


export const MeDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"Me"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"me"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"email"}},{"kind":"Field","name":{"kind":"Name","value":"firstName"}},{"kind":"Field","name":{"kind":"Name","value":"lastName"}}]}},{"kind":"Field","name":{"kind":"Name","value":"projects"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"slug"}}]}}]}}]} as unknown as DocumentNode<MeQuery, MeQueryVariables>;
export const SearchByNameDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"SearchByName"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"search"}},"type":{"kind":"NonNullType","type":{"kind":"ListType","type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"project"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"search"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"filters"},"value":{"kind":"ListValue","values":[{"kind":"ObjectValue","fields":[{"kind":"ObjectField","name":{"kind":"Name","value":"uri"},"value":{"kind":"StringValue","value":"http://schema.org/name","block":false}},{"kind":"ObjectField","name":{"kind":"Name","value":"value"},"value":{"kind":"Variable","name":{"kind":"Name","value":"search"}}}]}]}},{"kind":"Argument","name":{"kind":"Name","value":"project"},"value":{"kind":"Variable","name":{"kind":"Name","value":"project"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"facets"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"uri"}},{"kind":"Field","name":{"kind":"Name","value":"value"}}]}},{"kind":"Field","name":{"kind":"Name","value":"type"}},{"kind":"Field","name":{"kind":"Name","value":"typeName"}}]}}]}}]} as unknown as DocumentNode<SearchByNameQuery, SearchByNameQueryVariables>;
export const SearchByTypeDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"SearchByType"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"type"}},"type":{"kind":"NonNullType","type":{"kind":"ListType","type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"project"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"search"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"filters"},"value":{"kind":"ListValue","values":[{"kind":"ObjectValue","fields":[{"kind":"ObjectField","name":{"kind":"Name","value":"uri"},"value":{"kind":"StringValue","value":"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","block":false}},{"kind":"ObjectField","name":{"kind":"Name","value":"value"},"value":{"kind":"Variable","name":{"kind":"Name","value":"type"}}}]}]}},{"kind":"Argument","name":{"kind":"Name","value":"project"},"value":{"kind":"Variable","name":{"kind":"Name","value":"project"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"urn"}},{"kind":"Field","name":{"kind":"Name","value":"facets"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"uri"}},{"kind":"Field","name":{"kind":"Name","value":"value"}}]}},{"kind":"Field","name":{"kind":"Name","value":"type"}},{"kind":"Field","name":{"kind":"Name","value":"typeName"}}]}}]}}]} as unknown as DocumentNode<SearchByTypeQuery, SearchByTypeQueryVariables>;
export const FacetTimeSeriesDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"FacetTimeSeries"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"id"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"UUID"}}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"facet"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"entity"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"id"},"value":{"kind":"Variable","name":{"kind":"Name","value":"id"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"timeSeries"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"facet"},"value":{"kind":"Variable","name":{"kind":"Name","value":"facet"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"urn"}},{"kind":"Field","name":{"kind":"Name","value":"points"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"timestamp"}},{"kind":"Field","name":{"kind":"Name","value":"value"}}]}}]}}]}}]}}]} as unknown as DocumentNode<FacetTimeSeriesQuery, FacetTimeSeriesQueryVariables>;
export const FacetLogDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"FacetLog"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"id"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"UUID"}}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"facets"}},"type":{"kind":"NonNullType","type":{"kind":"ListType","type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"entity"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"id"},"value":{"kind":"Variable","name":{"kind":"Name","value":"id"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"facetLog"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"facets"},"value":{"kind":"Variable","name":{"kind":"Name","value":"facets"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"createdAt"}},{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"urn"}},{"kind":"Field","name":{"kind":"Name","value":"version"}},{"kind":"Field","name":{"kind":"Name","value":"entities"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"facets"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"uri"}},{"kind":"Field","name":{"kind":"Name","value":"value"}}]}}]}}]}}]}}]}}]} as unknown as DocumentNode<FacetLogQuery, FacetLogQueryVariables>;
export const EntityDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"Entity"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"id"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"UUID"}}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"depth"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"Int"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"entityGraph"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"id"},"value":{"kind":"Variable","name":{"kind":"Name","value":"id"}}},{"kind":"Argument","name":{"kind":"Name","value":"d"},"value":{"kind":"Variable","name":{"kind":"Name","value":"depth"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"urn"}},{"kind":"Field","name":{"kind":"Name","value":"facets"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"uri"}},{"kind":"Field","name":{"kind":"Name","value":"value"}},{"kind":"Field","name":{"kind":"Name","value":"metaType"}}]}},{"kind":"Field","name":{"kind":"Name","value":"type"}},{"kind":"Field","name":{"kind":"Name","value":"typeName"}}]}}]}}]} as unknown as DocumentNode<EntityQuery, EntityQueryVariables>;
export const OntologyDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"Ontology"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"project"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"entityTypes"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"project"},"value":{"kind":"Variable","name":{"kind":"Name","value":"project"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"uri"}},{"kind":"Field","name":{"kind":"Name","value":"isRootType"}}]}},{"kind":"Field","name":{"kind":"Name","value":"facetTypes"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"project"},"value":{"kind":"Variable","name":{"kind":"Name","value":"project"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"uri"}},{"kind":"Field","name":{"kind":"Name","value":"metaType"}},{"kind":"Field","name":{"kind":"Name","value":"jsonSchema"}},{"kind":"Field","name":{"kind":"Name","value":"indexTimeSeries"}},{"kind":"Field","name":{"kind":"Name","value":"isRootType"}}]}}]}}]} as unknown as DocumentNode<OntologyQuery, OntologyQueryVariables>;
export const CreateApiKeyDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"mutation","name":{"kind":"Name","value":"CreateApiKey"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"createApiKey"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"description"}},{"kind":"Field","name":{"kind":"Name","value":"createdAt"}},{"kind":"Field","name":{"kind":"Name","value":"secret"}}]}}]}}]} as unknown as DocumentNode<CreateApiKeyMutation, CreateApiKeyMutationVariables>;
export const GetApiKeysDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"GetApiKeys"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"apiKeys"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"description"}},{"kind":"Field","name":{"kind":"Name","value":"createdAt"}}]}}]}}]} as unknown as DocumentNode<GetApiKeysQuery, GetApiKeysQueryVariables>;
export const LoginDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"mutation","name":{"kind":"Name","value":"Login"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"email"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"password"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"login"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"email"},"value":{"kind":"Variable","name":{"kind":"Name","value":"email"}}},{"kind":"Argument","name":{"kind":"Name","value":"password"},"value":{"kind":"Variable","name":{"kind":"Name","value":"password"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"jwt"}}]}}]}}]} as unknown as DocumentNode<LoginMutation, LoginMutationVariables>;