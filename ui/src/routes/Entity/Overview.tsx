type Facet = {
  uri: string;
  name: string;
  value?: any[];
  metaType: string;
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

export const Overview = ({ entity }: { entity: any }) => {
  return <FacetTable facets={entity.facets || []} />;
};
