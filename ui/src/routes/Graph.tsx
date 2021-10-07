import _ from 'lodash';
import CytoscapeComponent from 'react-cytoscapejs';
import { FacetHistory } from './Entity/Schema';

import cytoscape from 'cytoscape';
import dagre from 'cytoscape-dagre';

cytoscape.use(dagre);

export const Graph = ({ entityGraph }: { entityGraph: any }) => {
    // const elements = [
    //    { data: { id: 'one', label: 'Node 1' }, position: { x: 0, y: 0 } },
    //    { data: { id: 'two', label: 'Node 2' }, position: { x: 100, y: 0 } },
    //    { data: { source: 'one', target: 'two', label: 'Edge from Node1 to Node2' } }
    // ];

    const nodes = entityGraph.map((entity: any) => ({
      data: {
        id: entity.entityId,
        label: entity.facets.find(
          (facet: any) => facet.uri === "http://schema.org/name"
        )?.value,
      },
    }));

    const nodeIds = new Set(nodes.map((node: any) => node.data.id));

    console.log(entityGraph);

    const edges = _.flatMap(entityGraph, (entity: any) => {
      const relationshFacets = entity.facets.filter(
        (facet: any) => facet.metaType == "relationship"
      );

      return _.flatMap(relationshFacets, (facet: any) =>
        facet.value.map((target: string) => ({
          data: { source: entity.entityId, target, label: facet.name },
        }))
      );
    }).filter(
      (edge: any) =>
        nodeIds.has(edge.data.source) && nodeIds.has(edge.data.target)
    );

    const elements = [...nodes, ...edges];

    console.log(nodes);
    console.log(nodeIds);
    console.log(elements);

  return (
    <>
      <CytoscapeComponent
        elements={elements}
        layout={
          {
            name: "dagre",
            rankDir: "LR",
            fit: true,
          } as any
        }
        style={{ width: "100%", height: "600px" }}
        stylesheet={[
          {
            selector: "node",
            style: {
              label: "data(label)",
              width: "label",
              "padding-bottom": "2px",
              "padding-top": "2px",
              "padding-left": "2px",
              "padding-right": "2px",
              color: "#ffffff",
              "font-size": "7px",
              "text-valign": "center",
              shape: "roundrectangle",
              "background-opacity": 0.7,
              "background-color": (ele) =>
                ele.data("type") === "derived" ? "#db1e7b" : "#1c6a9d",
              "border-width": "2px",
              "border-color": (ele) =>
                ele.data("type") === "derived" ? "#db1e7b" : "#1c6a9d",
            },
          },
          {
            selector: "edge",
            style: {
              width: 2,
              "line-color": "#ccc",
              "target-arrow-color": "#ccc",
              "target-arrow-shape": "triangle",
              "curve-style": "bezier",
              "control-point-distances": "20 -20",
              "control-point-weights": "0.25 0.75",
            },
          },
        ]}
      />
    </>
  );
};