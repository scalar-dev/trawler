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

export type Entity = {
  __typename?: 'Entity';
  entityId: Scalars['UUID'];
  facetLog: Array<FacetLog>;
  facets: Array<Facet>;
  type: Scalars['String'];
  typeName: Scalars['String'];
  urn: Scalars['String'];
};


export type EntityFacetLogArgs = {
  facets: Array<Scalars['String']>;
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

export type FilterInput = {
  uri: Scalars['String'];
  value: Scalars['String'];
};

export type Mutation = {
  __typename?: 'Mutation';
  doNothing: Scalars['String'];
};

export type Query = {
  __typename?: 'Query';
  entity?: Maybe<Entity>;
  entityGraph: Array<Entity>;
  search: Array<Entity>;
};


export type QueryEntityArgs = {
  id: Scalars['UUID'];
};


export type QueryEntityGraphArgs = {
  d: Scalars['Int'];
  id: Scalars['UUID'];
};


export type QuerySearchArgs = {
  filters: Array<FilterInput>;
};

export type SearchQueryVariables = Exact<{ [key: string]: never; }>;


export type SearchQuery = { __typename?: 'Query', search: Array<{ __typename?: 'Entity', entityId: any, type: string, typeName: string, facets: Array<{ __typename?: 'Facet', name: string, uri: string, value?: any | null | undefined }> }> };

export type FacetLogQueryVariables = Exact<{
  id: Scalars['UUID'];
  facets: Array<Scalars['String']> | Scalars['String'];
}>;


export type FacetLogQuery = { __typename?: 'Query', entity?: { __typename?: 'Entity', facetLog: Array<{ __typename?: 'FacetLog', id: any, createdAt: any, name: string, urn: string, version: any, entities: Array<{ __typename?: 'Entity', entityId: any, facets: Array<{ __typename?: 'Facet', name: string, value?: any | null | undefined }> }> }> } | null | undefined };

export type EntityQueryVariables = Exact<{
  id: Scalars['UUID'];
}>;


export type EntityQuery = { __typename?: 'Query', entityGraph: Array<{ __typename?: 'Entity', entityId: any, urn: string, type: string, typeName: string, facets: Array<{ __typename?: 'Facet', name: string, uri: string, value?: any | null | undefined, metaType: string }> }> };


export const SearchDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"Search"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"search"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"filters"},"value":{"kind":"ListValue","values":[{"kind":"ObjectValue","fields":[{"kind":"ObjectField","name":{"kind":"Name","value":"uri"},"value":{"kind":"StringValue","value":"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","block":false}},{"kind":"ObjectField","name":{"kind":"Name","value":"value"},"value":{"kind":"StringValue","value":"http://trawler.dev/schema/core#SqlTable","block":false}}]}]}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"entityId"}},{"kind":"Field","name":{"kind":"Name","value":"facets"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"uri"}},{"kind":"Field","name":{"kind":"Name","value":"value"}}]}},{"kind":"Field","name":{"kind":"Name","value":"type"}},{"kind":"Field","name":{"kind":"Name","value":"typeName"}}]}}]}}]} as unknown as DocumentNode<SearchQuery, SearchQueryVariables>;
export const FacetLogDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"FacetLog"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"id"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"UUID"}}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"facets"}},"type":{"kind":"NonNullType","type":{"kind":"ListType","type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"entity"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"id"},"value":{"kind":"Variable","name":{"kind":"Name","value":"id"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"facetLog"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"facets"},"value":{"kind":"Variable","name":{"kind":"Name","value":"facets"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"createdAt"}},{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"urn"}},{"kind":"Field","name":{"kind":"Name","value":"version"}},{"kind":"Field","name":{"kind":"Name","value":"entities"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"entityId"}},{"kind":"Field","name":{"kind":"Name","value":"facets"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"value"}}]}}]}}]}}]}}]}}]} as unknown as DocumentNode<FacetLogQuery, FacetLogQueryVariables>;
export const EntityDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"Entity"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"id"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"UUID"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"entityGraph"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"id"},"value":{"kind":"Variable","name":{"kind":"Name","value":"id"}}},{"kind":"Argument","name":{"kind":"Name","value":"d"},"value":{"kind":"IntValue","value":"1"}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"entityId"}},{"kind":"Field","name":{"kind":"Name","value":"urn"}},{"kind":"Field","name":{"kind":"Name","value":"facets"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"uri"}},{"kind":"Field","name":{"kind":"Name","value":"value"}},{"kind":"Field","name":{"kind":"Name","value":"metaType"}}]}},{"kind":"Field","name":{"kind":"Name","value":"type"}},{"kind":"Field","name":{"kind":"Name","value":"typeName"}}]}}]}}]} as unknown as DocumentNode<EntityQuery, EntityQueryVariables>;