import {
  DatabaseIcon,
  QuestionMarkCircleIcon,
  TableIcon,
  DotsVerticalIcon,
} from "@heroicons/react/solid";
import { useContext, useState } from "react";
import { useHistory, useParams } from "react-router";
import { Link } from "react-router-dom";
import { useQuery, gql } from "urql";
import { Header, Main } from "../components/Layout";
import { Option, Selector } from "../components/Selector";
import { ProjectContext } from "../ProjectContext";
import { SearchByTypeDocument } from "../types";

export const EntityIcon = ({ type }: { type: string }) => {
  if (type === "SqlDatabase") {
    return <DatabaseIcon className="h-6 w-6 flex-shrink-0 text-gray-600" />;
  } else if (type === "SqlTable") {
    return <TableIcon className="h-6 w-6 flex-shrink-0 text-gray-600" />;
  } else if (type === "SqlColumn") {
    return <DotsVerticalIcon className="h-6 w-6 flex-shrink-0 text-gray-600" />;
  } else {
    return (
      <QuestionMarkCircleIcon className="h-6 w-6 flex-shrink-0 text-gray-600" />
    );
  }
};

const Table = ({ entities }: { entities: any[] }) => {
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
                    Count
                  </th>
                </tr>
              </thead>
              <tbody>
                {entities?.map((entity, idx) => (
                  <tr
                    key={entity.entityId}
                    onClick={() => history.push(entityLink(entity.entityId))}
                    className={`hover:bg-gray-100 cursor-pointer ${
                      idx % 2 === 0 ? "bg-white" : "bg-gray-50"
                    }`}
                  >
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 flex items-center">
                      <EntityIcon type={entity.typeName} />
                      <span className="ml-2">
                        {
                          entity.facets.find(
                            (facet: any) =>
                              facet.uri === "http://schema.org/name"
                          )?.value
                        }
                        <div className="text-xs font-mono text-gray-600">
                          {entity.urn}
                        </div>
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {entity.typeName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {
                        entity.facets.find(
                          (facet: any) =>
                            facet.uri ===
                            "http://trawler.dev/schema/metrics#count"
                        )?.value
                      }
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

const types = [
  {
    label: "SqlTable",
    value: "http://trawler.dev/schema/core#SqlTable",
  },
  {
    label: "SqlDatabase",
    value: "http://trawler.dev/schema/core#SqlDatabase",
  },
  {
    label: "SqlColumn",
    value: "http://trawler.dev/schema/core#SqlColumn",
  },
];

export const SEARCH_BY_TYPE = gql`
  query SearchByType($type: [String!]!, $project: String!) {
    search(
      filters: [
        { uri: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", value: $type }
      ]
      project: $project
    ) {
      entityId
      urn
      facets {
        name
        uri
        value
      }
      type
      typeName
    }
  }
`;

export const Dashboard = () => {
  const { project } = useParams<{ project: string }>();
  const [selectedType, setSelectedType] = useState<Option>(types[0]);
  const [data] = useQuery({
    query: SearchByTypeDocument,
    variables: {
      type: selectedType.value,
      project,
    },
  });

  if (data.fetching) {
    return null;
  }

  if (!data.data?.search) {
    return (
      <div className="flex flex-1">
        <div className="m-auto font-bold text-3xl text-gray-500 text-center">
          Project {project} not found
          <div className="text-lg">
            Perhaps you need to{" "}
            <Link
              to="/sign-in"
              className="text-indigo-500 hover:text-indigo-600"
            >
              sign in
            </Link>
            ?
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
      <Header>
        <div className="flex items-center">
          <div className="flex-1">Dashboard</div>
          <div className="w-32">
            <Selector
              values={types}
              selected={selectedType}
              setSelected={setSelectedType}
            />
          </div>
        </div>
      </Header>
      <Main>
        <Table entities={data.data?.search || []} />
      </Main>
    </>
  );
};
