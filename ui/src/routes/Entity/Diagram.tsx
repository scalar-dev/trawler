import { CanvasWidget } from "@projectstorm/react-canvas-core";
import createEngine, {
  DagreEngine,
  PathFindingLinkFactory,
  DiagramModel,
} from "@projectstorm/react-diagrams";

import { NodeModel, DefaultPortModel } from "@projectstorm/react-diagrams";
import { BaseModelOptions } from "@projectstorm/react-canvas-core";

import { AbstractReactFactory } from "@projectstorm/react-canvas-core";

import { DiagramEngine, PortWidget } from "@projectstorm/react-diagrams-core";
import React, { useEffect, useState } from "react";
import _ from "lodash";
import { dataTypeFacet, fields, nameFacet } from "../../ontology";
import { setTimeout } from "timers";

interface TableWidgetProps {
  node: TableNodeModel;
  engine: DiagramEngine;
}

type Field = {
  id: string;
  name: string;
  type: string;
};

class TableWidget extends React.Component<TableWidgetProps, {}> {
  constructor(props: TableWidgetProps) {
    super(props);
    this.state = {};
  }

  render() {
    return (
      <div className="border rounded-md shadow-md">
        <PortWidget
          engine={this.props.engine}
          port={this.props.node.getPort(`${this.props.node.entityId}_in`)!}
        >
          <div className="px-4 py-2 font-bold w-full h-8 border-b">
            {this.props.node.name}
          </div>
        </PortWidget>

        {this.props.node.fields.map((field) => (
          <div className="w-full flex">
            <PortWidget
              engine={this.props.engine}
              port={this.props.node.getPort(`${field.id}_in`)!}
            >
              <div className="w-8 h-8"></div>
            </PortWidget>
            <div className="flex-1">{field.name}</div>
            <PortWidget
              engine={this.props.engine}
              port={this.props.node.getPort(`${field.id}_out`)!}
            >
              <div className="w-8 h-8"></div>
            </PortWidget>
          </div>
        ))}
      </div>
    );
  }
}

export class TableNodeFactory extends AbstractReactFactory<
  TableNodeModel,
  DiagramEngine
> {
  constructor() {
    super("table-node");
  }

  generateModel(initialConfig: any) {
    return new TableNodeModel();
  }

  generateReactWidget(event: any): JSX.Element {
    return (
      <TableWidget engine={this.engine as DiagramEngine} node={event.model} />
    );
  }
}

export interface TableNodeModelOptions extends BaseModelOptions {
  entityId: string;
  name: string;
  fields: Field[];
}

export class TableNodeModel extends NodeModel {
  entityId: string;
  name: string;
  fields: Field[];

  constructor(
    options: TableNodeModelOptions = { entityId: "", name: "", fields: [] }
  ) {
    super({
      ...options,
      type: "table-node",
    });
    this.entityId = options.entityId;
    this.name = options.name;
    this.fields = options.fields;

    this.addPort(
      new DefaultPortModel({
        in: true,
        name: `${this.entityId}_in`,
        locked: true,
      })
    );

    this.addPort(
      new DefaultPortModel({
        in: true,
        name: `${this.entityId}_out`,
        locked: true,
      })
    );

    this.fields.forEach((field) => {
      this.addPort(
        new DefaultPortModel({
          in: true,
          name: `${field.id}_in`,
          locked: true,
        })
      );

      this.addPort(
        new DefaultPortModel({
          in: false,
          name: `${field.id}_out`,
          locked: true,
        })
      );
    });
  }

  serialize() {
    return {
      ...super.serialize(),
      name: this.name,
      fields: this.fields,
    };
  }

  deserialize(event: any): void {
    super.deserialize(event);
    this.name = event.data.name;
    this.fields = event.data.fields;
  }
}

const engine = createEngine();
engine
  .getLinkFactories()
  .getFactory<PathFindingLinkFactory>(
    PathFindingLinkFactory.NAME
  ).ROUTING_SCALING_FACTOR = 1000;

const Canvas = ({ engine }: { engine: DiagramEngine }) => (
  <CanvasWidget className="flex-1" engine={engine} />
);

class DiagramWidget extends React.Component<
  { model: DiagramModel; engine: DiagramEngine },
  any
> {
  engine: DagreEngine;

  constructor(props: any) {
    super(props);
    this.engine = new DagreEngine({
      graph: {
        rankdir: "RL",
        ranker: "longest-path",
        marginx: 25,
        marginy: 25,
      },
      includeLinks: true,
    });
  }

  autoDistribute = () => {
    // this.props.
    this.engine.redistribute(this.props.model);
    // only happens if pathfing is enabled (check line 25)
    this.reroute();
    this.props.engine.repaintCanvas();
    // this.props.engine.zoomToFit();
  };

  componentDidMount(): void {
    setTimeout(() => this.autoDistribute(), 500);
  }

  reroute() {
    this.props.engine
      .getLinkFactories()
      .getFactory<PathFindingLinkFactory>(PathFindingLinkFactory.NAME)
      .calculateRoutingMatrix();
  }

  render() {
    return <Canvas engine={this.props.engine} />;
  }
}

type Entity = {
  entityId: string;
  facets: any[];
};

export const Diagram = ({ entityGraph }: { entityGraph: Entity[] }) => {
  const [isInit, setIsInit] = useState(false);

  useEffect(() => {
    engine.getNodeFactories().registerFactory(new TableNodeFactory());
    const model = new DiagramModel();

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

    let nodeByEntityId = new Map<string, TableNodeModel>();

    const nodeForEntity = (entityId: string) => {
      if (nodeByEntityId.has(entityId)) {
        return nodeByEntityId.get(entityId);
      } else {
        const parentId = parentEntity[entityId];
        return nodeByEntityId.get(parentId);
      }
    };

    const nodeForConstraint = (entityId: string) => {
      const entity = entityById[entityId];

      const constrains = entity.facets.find(
        (facet: any) =>
          facet.uri === "http://trawler.dev/schema/core#constrains"
      );

      if (constrains) {
        return nodeForEntity(constrains.value[0]);
      }

      return null;
    };

    entityGraph.forEach((entity: any, idx: number) => {
      if (entity.typeName === "SqlTable") {
        const fields = entityByParent[entity.entityId] || [];

        const node = new TableNodeModel({
          entityId: entity.entityId,
          name: nameFacet(entity),
          fields: fields.map((field: any) => ({
            id: field.entityId,
            name: nameFacet(field),
            type: dataTypeFacet(field),
          })),
        });

        nodeByEntityId.set(entity.entityId, node);

        node.setPosition(idx * 200, 0);

        model.addNode(node);
      }
    });

    entityGraph.forEach((entity: any) => {
      const sourceNode = nodeForEntity(entity.entityId);

      if (sourceNode) {
        const relationshipFacets = entity.facets.filter(
          (facet: any) => facet.metaType === "relationship"
        );

        relationshipFacets.forEach((facet: any) => {
          facet.value.forEach((targetId: string) => {
            const targetNode =
              facet.uri === "http://trawler.dev/schema/core#hasConstraint"
                ? nodeForConstraint(targetId)
                : nodeForEntity(targetId);

            if (targetNode && targetNode !== sourceNode) {
              const sourcePort = sourceNode.getPort(`${entity.entityId}_out`);
              const targetPort = targetNode.getPort(
                `${targetNode.entityId}_in`
              );

              if (sourcePort && targetPort) {
                const link = engine
                  .getLinkFactories()
                  .getFactory(PathFindingLinkFactory.NAME)
                  .generateModel({});
                // const link = new DefaultLinkModel();
                link.setSourcePort(sourcePort);
                link.setTargetPort(targetPort);
                model.addLink(link);
              }
            }
          });
        });
      }
    });

    engine.setModel(model);
    setIsInit(true);
  }, [entityGraph]);

  return isInit ? (
    <DiagramWidget model={engine.getModel()} engine={engine} />
  ) : // <Canvas engine={engine} />
  null;
};
