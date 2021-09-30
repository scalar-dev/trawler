import { formatDistanceStrict, formatRelative, parseISO } from "date-fns";
import _ from "lodash";
import { useParams } from "react-router-dom";
import { useQuery, gql } from "urql";
import { Header, Main } from "../components/Layout";
import { EntityQuery, FacetLogQuery } from "../types";

type Facet = {
  uri: string;
  name: string;
  value?: any[];
  metaType: string;
};

type FacetLog = {
  entities: {
    entityId: string;
  }[];
};

type FacetLogDiff = {
  added: any[];
  deleted: any[];
  log: any;
};

const FacetValue: React.FC<{ facet: Facet }> = ({ facet }) => {
  if (facet.metaType === "relationship") {
    return (
      <div className="flex flex-wrap">
        {facet.value?.map((val: any) => (
          <span className="ml-1 mt-1 px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
            <a href={`/entity/${val}`}>{val}</a>
          </span>
        ))}
      </div>
    );
  } else {
    return (
      <>
        {facet.value?.map((value: any) => (
          <div>{value}</div>
        ))}
      </>
    );
  }
};

export const FacetHistory = ({
  entityId,
  facets,
}: {
  entityId: string;
  facets: string[];
}) => {
  const [data] = useQuery<FacetLogQuery>({
    query: gql`
      query FacetLog($id: UUID!, $facets: [String!]!) {
        entity(id: $id) {
          facetLog(facets: $facets) {
            id
            createdAt
            name
            urn
            version
            entities {
              entityId
              facets {
                name
                uri
                value
              }
            }
          }
        }
      }
    `,
    variables: {
      id: entityId,
      facets,
    },
  });

  const log: FacetLog[] = _.orderBy(data.data?.entity?.facetLog, [
    "version",
    "asc",
  ]);

  const diffs = _.reduce(
    log,
    (acc: FacetLogDiff[], curr: FacetLog) => {
      const lastVersion = _.last(acc);

      const previousEntityIds = new Set(
        lastVersion?.log.entities?.map((entity: any) => entity.entityId) || []
      );

      const currentEntityIds = new Set(
        curr.entities.map((entity) => entity.entityId)
      );

      const added = curr.entities.filter(
        (entity) => !previousEntityIds.has(entity.entityId)
      );
      const deleted =
        lastVersion?.log.entities?.filter(
          (entity: any) => !currentEntityIds.has(entity.entityId)
        ) || [];

      return [
        ...acc,
        {
          added,
          deleted,
          log: curr,
        },
      ];
    },
    []
  );

  return (
    <div className="flex flex-col">
      <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
        <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
          <div className="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    Version
                  </th>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    Changes
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {_.orderBy(diffs, (diff) => diff.log.version, ["desc"]).map(
                  (diff) => (
                    <tr key={diff.log.id}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="ml-4">
                            <div className="text-sm font-medium text-gray-900">
                              {diff.log.version}
                            </div>
                            <div className="text-sm text-gray-500">
                              {formatDistanceStrict(
                                parseISO(diff.log.createdAt),
                                new Date(),
                                { addSuffix: true }
                              )}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap flex flex-wrap">
                        {diff.added.map((added) => (
                          <span className="ml-1 mt-1 px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                            <a href={`/entity/${added.entityId}`}>
                              {
                                added.facets.find(
                                  (facet: any) =>
                                    facet.uri === "http://schema.org/name"
                                )?.value
                              }
                            </a>
                          </span>
                        ))}

                        {diff.deleted.map((deleted) => (
                          <span className="ml-1 mt-1 px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800">
                            <a href={`/entity/${deleted.entityId}`}>
                              {
                                deleted.facets.find(
                                  (facet: any) =>
                                    facet.uri === "http://schema.org/name"
                                )?.value
                              }
                            </a>
                          </span>
                        ))}
                      </td>
                    </tr>
                  )
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

const FacetTable = ({ facets }: { facets: Facet[] }) => {
  return (
    <div className="flex flex-col">
      <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
        <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
          <div className="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    Name
                  </th>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    Value
                  </th>
                </tr>
              </thead>
              <tbody>
                {facets?.map((facet, idx) => (
                  <tr
                    key={facet.uri}
                    className={idx % 2 === 0 ? "bg-white" : "bg-gray-50"}
                  >
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {facet.name}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <FacetValue facet={facet} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export const Entity = () => {
  const { entity } = useParams<{ entity: string }>();
  const [data] = useQuery<EntityQuery>({
    query: gql`
      query Entity($id: UUID!) {
        entityGraph(id: $id, d: 1) {
          entityId
          urn
          facets {
            name
            uri
            value
            metaType
          }
          type
          typeName
        }
      }
    `,
    variables: {
      id: entity,
    },
  });

  const facets = data.data?.entityGraph[0]?.facets;

  const hasFacet = facets?.find(
    (facet) => facet.uri === "http://trawler.dev/schema/core#has"
  );

  const name = facets?.find(
    (facet: any) => facet.uri === "http://schema.org/name"
  )?.value;

  return (
    <>
      <Header>
        <div className="flex items-center">
          {name}
          {data.data && (
            <span className="ml-4 inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-indigo-100 text-indigo-800">
              <svg
                className="mr-1.5 h-2 w-2 text-indigo-400"
                fill="currentColor"
                viewBox="0 0 8 8"
              >
                <circle cx={4} cy={4} r={3} />
              </svg>
              {data.data.entityGraph[0].typeName}
            </span>
          )}
        </div>
        <div className="mt-1 text-gray-500 text-xs font-mono">
          {data.data && <>{data.data.entityGraph[0].urn}</>}
        </div>
      </Header>
      <Main>
        <FacetTable facets={data.data?.entityGraph[0].facets || []} />

        {hasFacet && (
          <div className="mt-4">
            <FacetHistory
              entityId={entity}
              facets={["http://trawler.dev/schema/core#has"]}
            />
          </div>
        )}
      </Main>
    </>
  );
};
