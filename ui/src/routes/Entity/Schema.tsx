import { formatDistanceStrict, parseISO } from "date-fns";
import _ from "lodash";
import numeral from "numeral";
import { useContext } from "react";
import { Link, useHistory } from "react-router-dom";
import { useQuery, gql } from "urql";
import { ProjectContext } from "../../ProjectContext";
import { Histogram } from "./Metrics";

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

export const FACET_LOG_QUERY = gql`
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
`;

const CurrentSchema = ({ entities }: { entities: any[] }) => {
  const history = useHistory();
  const { entityLink } = useContext(ProjectContext);
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
                    Type
                  </th>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    % Null
                  </th>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    % Unique
                  </th>

                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    Range
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {entities.map((entity: any) => {
                  const fields = _.chain(entity.facets)
                    .keyBy("name")
                    .mapValues((facet: any) => facet.value[0])
                    .value();

                  const histogram = entity.facets.find(
                    (facet: any) =>
                      facet.uri ===
                      "http://trawler.dev/schema/metrics#histogram"
                  );

                  return (
                    <tr
                      key={entity.entityId}
                      className="cursor-pointer hover:bg-gray-100"
                      onClick={() => history.push(entityLink(entity.entityId))}
                    >
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="ml-4">
                            <div className="text-sm font-medium text-gray-900">
                              {fields.Name}
                            </div>
                            <div className="text-sm text-gray-500">
                              {fields.Description}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-sm text-gray-900 font-mono">
                          {fields.Type}
                        </span>
                        {fields["Is Nullable"] && (
                          <span className="ml-2 px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                            Nullable
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {fields["Is Nullable"] &&
                          fields["Null Ratio"] != null && (
                            <span className="text-xs text-gray-600 ml-2">
                              {numeral(fields["Null Ratio"]).format("0.0%")}
                            </span>
                          )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {fields["Unique Ratio"] != null && (
                          <span className="text-xs text-gray-600 ml-2">
                            {numeral(fields["Unique Ratio"]).format("0.0%")}
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-0 text-center text-xs text-gray-500 font-medium">
                        <div className="flex items-center">
                          <div className="flex-1 text-left">
                            {fields["Min"] != null && (
                              <div>
                                Min: {numeral(fields["Min"]).format("0.[0][a]")}
                              </div>
                            )}
                            {fields["Max"] != null && (
                              <div>
                                Max: {numeral(fields["Max"]).format("0.[0][a]")}
                              </div>
                            )}
                          </div>
                          {histogram && (
                            <div className="h-10 max-w-md flex-1">
                              <Histogram facet={histogram} compact />
                            </div>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export const FacetHistory = ({
  entityId,
  facets,
}: {
  entityId: string;
  facets: string[];
}) => {
  const { entityLink } = useContext(ProjectContext);
  const [data] = useQuery({
    query: FACET_LOG_QUERY,
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
                            <Link to={entityLink(added.entityId)}>
                              {
                                added.facets.find(
                                  (facet: any) =>
                                    facet.uri === "http://schema.org/name"
                                )?.value
                              }
                            </Link>
                          </span>
                        ))}

                        {diff.deleted.map((deleted) => (
                          <span className="ml-1 mt-1 px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800">
                            <Link to={entityLink(deleted.entityId)}>
                              {
                                deleted.facets.find(
                                  (facet: any) =>
                                    facet.uri === "http://schema.org/name"
                                )?.value
                              }
                            </Link>
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

export const Schema = ({
  entityId,
  entity,
  entities,
}: {
  entityId: string;
  entity: any;
  entities: any[];
}) => {
  const hasFacet = entity.facets?.find(
    (facet: any) => facet.uri === "http://trawler.dev/schema/core#has"
  );

  if (!hasFacet) {
    return null;
  }

  const fields = hasFacet.value.map((value: any) => {
    return entities.find((entity: any) => entity.entityId === value);
  });

  return (
    <>
      <div className="mt-2">
        <CurrentSchema entities={fields} />
      </div>
      <div className="mt-4">
        <FacetHistory
          entityId={entityId}
          facets={["http://trawler.dev/schema/core#has"]}
        />
      </div>
    </>
  );
};
