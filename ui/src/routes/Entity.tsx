import { useParams } from "react-router-dom";
import { useQuery, gql } from "urql";
import { Header, Main } from "../components/Layout";
import { EntityQuery } from "../types";

const FacetValue = ({ facet }: { facet: any }) => {
  if (facet.metaType === "relationship") {
    return facet.value.map((val: any) => (
      <div>
        <a href={`/entity/${val}`}>{val}</a>
      </div>
    ));
  } else {
    return facet.value.map((value: any) => <div>{value}</div>);
  }
};

const Table = ({ facets }: { facets: any[] }) => {
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

  return (
    <>
      <Header>
        Entity: {data.data && <>{data.data.entityGraph[0].typeName}</>}
        <pre className="text-gray-500 text-sm">
          {data.data && <>{data.data.entityGraph[0].urn}</>}
        </pre>
      </Header>
      <Main>
        <Table facets={data.data?.entityGraph[0].facets || []} />
      </Main>
    </>
  );
};
