import { gql } from "@urql/core";
import { useQuery } from "urql";
import { FacetTimeSeriesDocument } from "../../types";
import { Bar, Line } from "react-chartjs-2";
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

  const chartData = (canvas: HTMLCanvasElement) => {
    const ctx = canvas.getContext("2d");
    const gradient = ctx!.createLinearGradient(0, 0, 0, 400);

    gradient.addColorStop(0, "rgba(79, 70, 229, 1.0)");
    gradient.addColorStop(1, "rgba(255,255,255, 0.0)");

    return {
      datasets: [
        {
          label: name,
          fill: "origin",
          backgroundColor: gradient,
          borderColor: "rgba(79, 70, 229, 1.0)",
          data: points.map((point) => ({
            x: point.timestamp,
            y: point.value,
            r: 0,
          })),
        },
      ],
    };
  };

  return (
    <div className="mt-4 shadow overflow-hidden border-b border-gray-200 sm:rounded-lg bg-white p-4">
      <h3 className="text-lg leading-6 font-medium text-gray-900">{name}</h3>

      <div className="mt-2">
        <Line
          height={60}
          data={chartData}
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
                    day: "MMM d",
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

export const Histogram = ({
  facet,
  compact = false,
}: {
  facet: any;
  compact?: boolean;
}) => {
  const value = facet.value[0];
  const binWidth = (value.max - value.min) / value.buckets;

  const data = value.counts.slice(1).map((count: number, idx: number) => {
    return {
      x: value.min + (idx - 1) * binWidth,
      y: count,
    };
  });

  const chartData = (canvas: HTMLCanvasElement) => {
    const ctx = canvas.getContext("2d");
    const gradient = ctx!.createLinearGradient(0, 0, 0, 400);

    gradient.addColorStop(0, "rgba(79, 70, 229, 1.0)");
    gradient.addColorStop(1, "rgba(255,255,255, 0.0)");

    return {
      datasets: [
        {
          label: facet.name,
          fill: "origin",
          minBarLength: 1,
          backgroundColor: gradient,
          borderColor: "rgba(79, 70, 229, 1.0)",
          borderWidth: 0.25,
          barPercentage: 1,
          categoryPercentage: 1,
          data,
        },
      ],
    };
  };

  return (
    <Bar
      data={chartData}
      options={{
        maintainAspectRatio: false,
        responsive: true,
        plugins: {
          tooltip: {
            enabled: !compact,
          },
          legend: {
            display: false,
          },
        },
        scales: {
          x: {
            type: "linear",
            display: !compact,
          },
          y: {
            type: "linear",
            display: !compact,
          },
        },
      }}
    />
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
  const timeSeriesFacets = facets
    .filter(
      (facet: any) => facet.metaType === "int" || facet.metaType === "double"
    )
    .map((facet: any) => facet.uri);

  const histFacets = facets.filter(
    (facet: any) => facet.uri === "http://trawler.dev/schema/metrics#histogram"
  );

  return (
    <>
      {timeSeriesFacets.map((uri) => (
        <TimeSeries key={uri} entityId={entity} facet={uri} />
      ))}
      {histFacets.map((facet) => (
        <div className="mt-4 shadow overflow-hidden border-b border-gray-200 sm:rounded-lg bg-white p-4">
          <h3 className="text-lg leading-6 font-medium text-gray-900">
            {facet.name}
          </h3>

          <div className="mt-2">
            <Histogram key={facet.uri} facet={facet} />
          </div>
        </div>
      ))}
    </>
  );
};
