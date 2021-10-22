import ReactFlow, {
  Handle,
  Position,
  MiniMap,
  Controls,
  ConnectionMode,
  Node,
  isNode,
  isEdge,
  ArrowHeadType,
} from "react-flow-renderer";
import _ from "lodash";
import { useMemo, useState } from "react";
import { dataTypeFacet, fields, nameFacet } from "../../ontology";
import dagre from "dagre";

type Field = {
  id: string;
  name: string;
  type: string;
};

type Entity = {
  typeName: string;
  entityId: string;
  facets: any[];
};

const TableNode = ({
  id,
  selected,
  data,
}: {
  id: string;
  selected: boolean;
  data: any;
}) => {
  return (
    <div
      className={`border rounded-md ${
        selected ? "border-indigo-600" : "border-gray-500"
      } ${data.secondarySelected ? "border-red-600" : "border-gray-500"}
      `}
    >
      <div className="flex bg-white rounded-md">
        <Handle
          id={id}
          type="target"
          position={Position.Top}
          isConnectable
          style={{
            background: data.connectedHandles.has(id)
              ? "rgba(79. 70, 229, 1.0)"
              : "#000",
          }}
        />
        <div className="px-3 py-2 border-b w-full">
          {data.name}
          <div className="text-xs font-mono">{data.type}</div>
        </div>
      </div>

      <div className="bg-opacity-60 bg-white">
        {data.fields.map((field: Field) => (
          <div className="flex w-full">
            <Handle
              id={`${field.id}_in`}
              type="target"
              position={Position.Left}
              isConnectable
              style={{
                background: data.connectedHandles.has(`${field.id}_in`)
                  ? "rgba(79, 70, 229, 1.0)"
                  : "#000",
                position: "relative",
                top: 15,
              }}
            />
            <div className="px-1 py-1 flex-1 text-xs">
              {field.name}: <span className="font-mono">{field.type}</span>
            </div>
            <Handle
              id={`${field.id}_out`}
              type="source"
              position={Position.Right}
              isConnectable
              style={{
                background: data.connectedHandles.has(`${field.id}_out`)
                  ? "rgba(79, 70, 229, 1.0)"
                  : "#000",
                position: "relative",
                top: 15,
              }}
            />
          </div>
        ))}
      </div>
    </div>
  );
};

const getLayoutedElements = (elements: any[], direction = "TB") => {
  const nodeWidth = 150;
  const dagreGraph = new dagre.graphlib.Graph();
  dagreGraph.setDefaultEdgeLabel(() => ({}));
  const isHorizontal = direction === "LR";
  dagreGraph.setGraph({ rankdir: direction });

  elements.forEach((el) => {
    if (isNode(el)) {
      dagreGraph.setNode(el.id, {
        width: nodeWidth,
        height: el.data.fields.length * 30,
      });
    } else {
      dagreGraph.setEdge(el.source, el.target);
    }
  });

  dagre.layout(dagreGraph);

  return elements.map((el) => {
    if (isNode(el)) {
      const nodeWithPosition = dagreGraph.node(el.id);
      el.targetPosition = isHorizontal ? Position.Left : Position.Top;
      el.sourcePosition = isHorizontal ? Position.Right : Position.Bottom;

      // unfortunately we need this little hack to pass a slightly different position
      // to notify react flow about the change. Moreover we are shifting the dagre node position
      // (anchor=center center) to the top left so it matches the react flow node anchor point (top left).
      el.position = {
        x: nodeWithPosition.x - nodeWidth / 2 + Math.random() / 1000,
        y: nodeWithPosition.y - (el.data.fields.length * 30) / 2,
      };
    }

    return el;
  });
};

export const Diagram = ({ entityGraph }: { entityGraph: Entity[] }) => {
  const [selection, setSelection] = useState(new Set<string>());

  const elements = useMemo(() => {
    const entityById = _.keyBy(entityGraph, (entity) => entity.entityId);

    const parentEntity: Record<string, string> = _.chain(entityGraph)
      .flatMap((entity) => {
        const fieldIds: string[] = fields(entity);
        return fieldIds.map((fieldId: string) => [entity.entityId, fieldId]);
      })
      .keyBy((kv) => kv[1])
      .mapValues((kv) => kv[0])
      .value();

    const entityByParent = _.chain(entityGraph)
      .groupBy((entity: any) => parentEntity[entity.entityId])
      .value();

    const nodeForEntity = (entityId: string) => {
      const entity = entityById[entityId];

      if (!entity) {
        return null;
      }

      if (entity.typeName === "SqlTable") {
        return entityId;
      } else if (entity.typeName === "SqlColumn") {
        return parentEntity[entityId];
      }

      return null;
    };

    const targetForConstraint = (entityId: string) => {
      const entity = entityById[entityId];

      const constrains = entity.facets.find(
        (facet: any) =>
          facet.uri === "http://trawler.dev/schema/core#constrains"
      );

      if (constrains) {
        return constrains.value[0];
      }

      return null;
    };

    const nodes: any[] = [];

    entityGraph.forEach((entity: any, idx: number) => {
      if (entity.typeName === "SqlTable") {
        const fields = entityByParent[entity.entityId] || [];

        const node = {
          id: entity.entityId,
          type: "table",
          data: {
            name: nameFacet(entity),
            type: entity.typeName,
            fields: fields.map((field: any) => ({
              id: field.entityId,
              name: nameFacet(field),
              type: dataTypeFacet(field),
            })),
          },

          position: { x: idx * 200, y: 0 },
        };

        nodes.push(node);
      }
    });

    const links: any[] = [];

    const connectedHandles = new Map<string, Set<string>>();

    entityGraph.forEach((entity: any) => {
      const sourceNode = nodeForEntity(entity.entityId);

      if (sourceNode) {
        const relationshipFacets = entity.facets.filter(
          (facet: any) => facet.metaType === "relationship"
        );

        relationshipFacets.forEach((facet: any) => {
          facet.value.forEach((targetId: string) => {
            const resolvedTargetId =
              facet.uri === "http://trawler.dev/schema/core#hasConstraint"
                ? targetForConstraint(targetId)
                : targetId;

            const targetNode = nodeForEntity(resolvedTargetId);

            if (targetNode && targetNode !== sourceNode) {
              const link = {
                id: `${entity.entityId}_${targetId}`,
                source: sourceNode,
                sourceHandle: `${entity.entityId}_out`,
                target: targetNode,
                targetHandle: `${resolvedTargetId}_in`,
                label: facet.name,
                arrowHeadType: ArrowHeadType.Arrow,
              };

              connectedHandles.set(
                sourceNode,
                (connectedHandles.get(sourceNode) || new Set<string>()).add(
                  link.sourceHandle
                )
              );

              connectedHandles.set(
                targetNode,
                (connectedHandles.get(targetNode) || new Set<string>()).add(
                  link.targetHandle
                )
              );

              links.push(link);
            }
          });
        });
      }
    });

    nodes.forEach(
      (node) =>
        (node.data = {
          ...node.data,
          connectedHandles: connectedHandles.get(node.id) || new Set(),
        })
    );

    return getLayoutedElements([...nodes, ...links]);
  }, [entityGraph]);

  const connectedNodes = new Set(
    elements
      .map((element) => {
        if (isEdge(element)) {
          if (selection.has(element.source)) {
            return element.target;
          } else if (selection.has(element.target)) {
            return element.source;
          }
        }
        return null;
      })
      .filter((element) => element)
  );

  return (
    <div className="flex-1">
      <ReactFlow
        connectionMode={ConnectionMode.Loose}
        minZoom={0.1}
        nodeTypes={{ table: TableNode }}
        elements={elements.map((element) => {
          if (isEdge(element)) {
            return selection.has(element.source) ||
              selection.has(element.target)
              ? { ...element, animated: true }
              : { ...element, animated: false };
          } else if (isNode(element)) {
            return connectedNodes.has(element.id)
              ? {
                  ...element,
                  data: { ...element.data, secondarySelected: true },
                }
              : {
                  ...element,
                  data: { ...element.data, secondarySelected: false },
                };
          }
          return element;
        })}
        onSelectionChange={(selectedElements) => {
          const node = selectedElements?.[0] as unknown as Node;

          if (node) {
            setSelection(new Set([node.id]));
          } else {
            setSelection(new Set());
          }
        }}
      >
        <Controls />
        <MiniMap
          nodeColor={(node) => {
            switch (node.type) {
              case "input":
                return "red";
              case "default":
                return "#00ff00";
              case "output":
                return "rgb(0,0,255)";
              default:
                return "#eee";
            }
          }}
          nodeStrokeWidth={3}
        />
      </ReactFlow>
    </div>
  );
};
