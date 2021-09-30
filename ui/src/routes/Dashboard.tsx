import {
  DatabaseIcon,
  QuestionMarkCircleIcon,
  TableIcon,
  DotsVerticalIcon,
} from "@heroicons/react/solid";
import { useState } from "react";
import { useQuery, gql } from "urql";
import { Header, Main } from "../components/Layout";
import { Option, Selector } from "../components/Selector";
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

const Table = ({ datasets }: { datasets: any[] }) => {
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
                    ID
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
                  <th scope="col" className="relative px-6 py-3">
                    <span className="sr-only">Edit</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {datasets?.map((dataset, idx) => (
                  <tr
                    key={dataset.entityId}
                    className={idx % 2 === 0 ? "bg-white" : "bg-gray-50"}
                  >
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 flex items-center">
                      <EntityIcon type={dataset.typeName} />
                      <span className="ml-2">
                        {
                          dataset.facets.find(
                            (facet: any) =>
                              facet.uri === "http://schema.org/name"
                          )?.value
                        }
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {dataset.entityId}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {dataset.typeName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {
                        dataset.facets.find(
                          (facet: any) =>
                            facet.uri ===
                            "http://trawler.dev/schema/metrics#count"
                        )?.value
                      }
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <a
                        href={`/entity/${dataset.entityId}`}
                        className="text-indigo-600 hover:text-indigo-900"
                      >
                        View
                      </a>
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

const SEARCH_BY_TYPE = gql`
  query SearchByType($type: [String!]!) {
    search(
      filters: [
        { uri: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", value: $type }
      ]
    ) {
      entityId
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
  const [selectedType, setSelectedType] = useState<Option>(types[0]);
  const [data] = useQuery({
    query: SearchByTypeDocument,
    variables: {
      type: selectedType.value,
    },
  });

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
        <Table datasets={data.data?.search || []} />
      </Main>
    </>
  );
};
