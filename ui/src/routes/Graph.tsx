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

  const edges = _.flatMap(entityGraph, (entity: any) => {
    const relationshipFacets = entity.facets.filter(
      (facet: any) => facet.metaType === "relationship"
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
            fit: true,
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
