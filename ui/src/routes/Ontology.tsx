import { gql } from "@urql/core";
import { useContext } from "react";
import { useQuery } from "urql";
import { Header, Main } from "../components/Layout";
import { ProjectContext } from "../ProjectContext";
import { OntologyQuery, OntologyDocument } from "../types";

export const ONTOLOGY_QUERY = gql`
  query Ontology($project: String!) {
    entityTypes(project: $project) {
      id
      name
      uri
      isRootType
    }
    facetTypes(project: $project) {
      id
      name
      uri
      metaType
      jsonSchema
      indexTimeSeries
      isRootType
    }
  }
`;

type FacetType = OntologyQuery["facetTypes"][0];

const FacetTable = ({ facetTypes }: { facetTypes: FacetType[] }) => {
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
                    URI
                  </th>
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
                    MetaType
                  </th>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    Root
                  </th>
                </tr>
              </thead>
              <tbody>
                {facetTypes.map((facetType, idx) => (
                  <tr
                    key={facetType.id}
                    className={idx % 2 === 0 ? "bg-white" : "bg-gray-50"}
                  >
                    <td className="px-6 py-4 whitespace-nowrap text-xs text-gray-900 font-mono">
                      <pre>{facetType.uri}</pre>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {facetType.name}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {facetType.metaType}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {facetType.isRootType ? "Root" : "Project"}
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

type EntityType = OntologyQuery["entityTypes"][0];

const EntityTable = ({ entityTypes }: { entityTypes: EntityType[] }) => {
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
                    URI
                  </th>
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
                    Root
                  </th>
                </tr>
              </thead>
              <tbody>
                {entityTypes.map((entityType, idx) => (
                  <tr
                    key={entityType.id}
                    className={idx % 2 === 0 ? "bg-white" : "bg-gray-50"}
                  >
                    <td className="px-6 py-4 whitespace-nowrap text-xs text-gray-900 font-mono">
                      <pre>{entityType.uri}</pre>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {entityType.name}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {entityType.isRootType ? "Root" : "Project"}
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

export const Ontology = () => {
  const { project } = useContext(ProjectContext);
  const [data] = useQuery({ query: OntologyDocument, variables: { project } });

  return (
    <>
      <Header>
        <div className="flex items-center">
          <div className="flex-1">Ontology</div>
        </div>
      </Header>
      <Main>
        <div className="text-2xl font-black py-2 text-gray-700">Entities</div>
        {data.data && <EntityTable entityTypes={data.data?.entityTypes} />}
        <div className="text-2xl font-black py-2 text-gray-700">Facets</div>
        {data.data && <FacetTable facetTypes={data.data?.facetTypes} />}
      </Main>
    </>
  );
};
