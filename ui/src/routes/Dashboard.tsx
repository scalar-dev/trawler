import { useQuery, gql } from "urql";
import { Header, Main } from "../components/Layout";

const Table = ({datasets}: {datasets: any[]})=> {
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
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {
                        dataset.facets.find(
                          (facet: any) => facet.uri === "http://schema.org/name"
                        )?.value
                      }
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

export const Dashboard = () => {
  const [data] = useQuery({
    query: gql`
      {
        search(
          filters: [
            {
              uri: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
              value: "http://trawler.dev/schema/core#SqlTable"
            }
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
    `,
  });

  return (
    <>
      <Header>Dashboard</Header>
      <Main>
        <Table datasets={data?.data?.search} />
      </Main>
    </>
  );
};