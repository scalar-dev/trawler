import {
  ArrowLeftIcon,
  ChartSquareBarIcon,
  CubeTransparentIcon,
  InformationCircleIcon,
  TableIcon,
} from "@heroicons/react/solid";
import {
  Link,
  Route,
  useHistory,
  useLocation,
  useParams,
  useRouteMatch,
} from "react-router-dom";
import { useQuery, gql } from "urql";
import { Header, Main } from "../../components/Layout";
import { Tab, Tabs } from "../../components/Tabs";
import { EntityDocument } from "../../types";
import { Graph } from "../Graph";
import { Metrics } from "./Metrics";
import { Overview } from "./Overview";
import { Schema } from "./Schema";

export const ENTITY_QUERY = gql`
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
`;

export const Entity = () => {
  const { entity } = useParams<{ entity: string }>();
  const { path, url } = useRouteMatch();
  const history = useHistory();
  const location = useLocation();
  const [data] = useQuery({
    query: EntityDocument,
    variables: {
      id: entity,
    },
  });

  const facets = data.data?.entityGraph[0]?.facets;

  const name = facets?.find(
    (facet: any) => facet.uri === "http://schema.org/name"
  )?.value;

  const hasFacet = facets?.find(
    (facet: any) => facet.uri === "http://trawler.dev/schema/core#has"
  );

  const TABS: Tab[] = [
    {
      name: "Overview",
      href: `${url}`,
      icon: InformationCircleIcon,
    },
    {
      name: "Schema",
      href: `${url}/schema`,
      icon: TableIcon,
      enabled: !!hasFacet,
    },
    {
      name: "Graph",
      href: `${url}/graph`,
      icon: CubeTransparentIcon,
    },
    {
      name: "Metrics",
      href: `${url}/metrics`,
      icon: ChartSquareBarIcon,
    },
  ]
    .filter((tab) => tab.enabled !== false)
    .map((tab) => ({ ...tab, current: location.pathname === tab.href }));

  if (data.fetching) {
    return null;
  }

  if (!data.data?.entityGraph) {
    return (
      <div className="flex flex-1">
        <div className="m-auto font-bold text-3xl text-gray-500 text-center">
          Entity {entity} not found
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
          <div
            className="flex flex-row text-lg text-gray-500 hover:text-indigo-500 items-center cursor-pointer"
            onClick={() => history.goBack()}
          >
            <ArrowLeftIcon className="h-5 w-5 flex-shrink-0" />
            <span className="ml-2">Back</span>
          </div>
          <div className="flex flex-col ml-4">
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
          </div>
        </div>
        <Tabs tabs={TABS} />
      </Header>
      <Main>
        <Route path={`${path}`} exact>
          {data.data?.entityGraph[0] && (
            <Overview entity={data.data?.entityGraph[0]} />
          )}
        </Route>

        <Route path={`${path}/schema`} exact>
          {data.data?.entityGraph[0] && (
            <Schema
              entityId={entity}
              entity={data.data?.entityGraph[0]}
              entities={data.data.entityGraph}
            />
          )}
        </Route>

        <Route path={`${path}/graph`} exact>
          {data.data?.entityGraph && (
            <Graph entityGraph={data.data?.entityGraph} />
          )}
        </Route>

        <Route path={`${path}/metrics`} exact>
          {data.data?.entityGraph[0] && (
            <Metrics entity={entity} facets={data.data.entityGraph[0].facets} />
          )}
        </Route>
      </Main>
    </>
  );
};
