import _ from "lodash";
import CytoscapeComponent from "react-cytoscapejs";

import cytoscape from "cytoscape";
import dagre from "cytoscape-dagre";
import fcose from "cytoscape-fcose";
import { useHistory } from "react-router";
import { useContext } from "react";
import { ProjectContext } from "../ProjectContext";

cytoscape.use(dagre);
cytoscape.use(fcose);

export const Graph = ({ entityGraph }: { entityGraph: any }) => {
  const history = useHistory();
  const { entityLink } = useContext(ProjectContext);

  const parents = _.chain(entityGraph)
    .flatMap((entity: any) => {
      const relationshipFacets = entity.facets
        .filter((facet: any) => facet.metaType === "relationship")
        .filter(
          (facet: any) =>
            facet.uri === "http://trawler.dev/schema/core#hasField"
        );

      return relationshipFacets.flatMap((facet: any) =>
        facet.value.map((value: any) => [entity.entityId, value])
      );
    })
    .keyBy((pair: any[]) => pair[1])
    .mapValues((pair: any[]) => pair[0])
    .value();

  const groups = _.chain(entityGraph)
    .map((entity: any) => entity.entityId)
    .filter((id: any) => id in parents)
    .groupBy((id: any) => parents[id])
    .values()
    .value();


  const edges = _.flatMap(entityGraph, (entity: any) => {
    const relationshipFacets = entity.facets
      .filter((facet: any) => facet.metaType === "relationship")
      .filter(
        (facet: any) =>
          !(
            facet.uri === "http://trawler.dev/schema/core#hasField" ||
            (entity.typeName === "SqlDatabase" &&
              facet.uri === "http://trawler.dev/schema/core#has")
          )
      );

    return _.flatMap(relationshipFacets, (facet: any) =>
      facet.value.map((target: string) => ({
        data: { source: entity.entityId, target, label: facet.name },
      }))
    );
  })

  const nodes = entityGraph.map((entity: any) => ({
    data: {
      id: entity.entityId,
      parent: parents[entity.entityId],
      label:
        entity.facets.find(
          (facet: any) => facet.uri === "http://schema.org/name"
        )?.value || entity.typeName,
    },
  }));

  const nodeIds = new Set(nodes.map((node: any) => node.data.id));

  const elements = [
    ...nodes,
    ...edges.filter(
      (edge: any) =>
        nodeIds.has(edge.data.source) && nodeIds.has(edge.data.target)
    ),
  ];
  console.log(elements);

  const constrains = groups.flatMap((group: any[]) => {
    return _.zip(group, group.slice(1))
      .filter((pair: any[]) => pair[0] && pair[1])
      .map((pair: any[]) => ({
        top: pair[0],
        bottom: pair[1],
        gap: 20,
      }));
  });

  return (
    <>
      <CytoscapeComponent
        cy={(cy) => {
          cy.on("tap", "node", function (this: any, event) {
            history.push(entityLink(this.id()));
          });
        }}
        elements={elements}
        layout={
          {
            name: "fcose",
            // rankDir: "LR",
            alignmentConstraint: { vertical: groups },
            relativePlacementConstraint: constrains,
            // fit: true,
            quality: "proof",
            randomize: false,
          } as any
        }
        style={{ flex: 1 }}
        stylesheet={[
          {
            selector: "node",
            style: {
              label: "data(label)",
              width: "label",
              height: "label",
              // "padding-bottom": "0px",
              // "padding-top": "0px",
              "padding-left": "2px",
              "padding-right": "2px",
              color: "#ffffff",
              "font-size": "5px",
              "text-valign": "center",
              shape: "roundrectangle",
              "background-opacity": 0.7,
              "background-color": (ele) =>
                ele.id() === entityGraph[0].entityId ? "#db1e7b" : "#1c6a9d",

              "border-width": "1px",
              "border-color": (ele) =>
                ele.id() === entityGraph[0].entityId ? "#db1e7b" : "#1c6a9d",
            },
          },
          {
            selector: ":parent",
            style: {
              label: "data(label)",
              "padding-bottom": "2px",
              "padding-top": "2px",
              "padding-left": "2px",
              "padding-right": "2px",
              color: "#ffffff",
              "font-size": "7px",
              "text-valign": "center",
              shape: "roundrectangle",
              "background-opacity": 0.3,
              "background-color": (ele) =>
                ele.id() === entityGraph[0].entityId ? "#db1e7b" : "#1c6a9d",
              "border-width": "1px",
              "border-color": (ele) =>
                ele.id() === entityGraph[0].entityId ? "#db1e7b" : "#1c6a9d",
            },
          },
          {
            selector: "edge",
            style: {
              width: 2,
              label: "data(label)",
              "font-size": "7px",
              "line-color": "#ccc",
              "target-arrow-color": "#ccc",
              "target-arrow-shape": "triangle",
              "curve-style": "unbundled-bezier",
              "control-point-distances": "20 -20",
              "control-point-weights": "0.25 0.75",
            },
          },
        ]}
      />
    </>
  );
};
