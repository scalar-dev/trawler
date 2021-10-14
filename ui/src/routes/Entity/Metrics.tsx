import { gql } from "@urql/core";
import { useQuery } from "urql";
import { FacetTimeSeriesDocument } from "../../types";
import { Line } from "react-chartjs-2";
import "chartjs-adapter-date-fns";

export const TIME_SERIES = gql`
  query FacetTimeSeries($id: UUID!, $facet: String!) {
    entity(id: $id) {
      timeSeries(facet: $facet) {
        name
        urn
        points {
          timestamp
          value
        }
      }
    }
  }
`;

const TimeSeries = ({
  entityId,
  facet,
}: {
  entityId: string;
  facet: string;
}) => {
  const [data] = useQuery({
    query: FacetTimeSeriesDocument,
    variables: {
      id: entityId,
      facet,
    },
  });

  const points = data.data?.entity?.timeSeries?.points;
  const name = data.data?.entity?.timeSeries?.name;

  if (!points) {
    return null;
  }

  return (
    <div className="mt-4 shadow overflow-hidden border-b border-gray-200 sm:rounded-lg bg-white p-4">
      <h3 className="text-lg leading-6 font-medium text-gray-900">{name}</h3>

      <div className="mt-2">
        <Line
          height={60}
          data={{
            datasets: [
              {
                label: name,
                backgroundColor: "rgba(79, 70, 229, 1.0)",
                borderColor: "rgba(79, 70, 229, 1.0)",
                data: points.map((point) => ({
                  x: point.timestamp,
                  y: point.value,
                  r: 0,
                })),
              },
            ],
          }}
          options={{
            plugins: {
              legend: {
                display: false,
              },
            },
            scales: {
              x: {
                type: "time",
                time: {
                  minUnit: "hour",
                  displayFormats: {
                    day: "MMM D",
                    week: "MMM YYYY",
                    month: "MMM YYYY",
                    quarter: "MMM YYYY",
                  },
                },
              },
            },
          }}
        />
      </div>
    </div>
  );
};
export const Metrics = ({
  entity,
  facets,
}: {
  entity: string;
  facets: any[];
}) => {
  console.log(facets);
  const eligibleFacets =facets
        .filter(
          (facet: any) =>
            facet.metaType === "int" || facet.metaType === "double"
        )
        .map((facet: any) => facet.uri);

  console.log(eligibleFacets);

  return (
    <>
      {eligibleFacets.map((uri) => (
        <TimeSeries key={uri} entityId={entity} facet={uri} />
      ))}
    </>
  );
};