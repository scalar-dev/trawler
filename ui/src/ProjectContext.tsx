import React from "react";
import { generatePath, useParams } from "react-router";

type Project = {
  project: string;
  entityLink: (entityId: string) => string;
};

export const ProjectContext = React.createContext<Project>({
  project: "dummy",
  entityLink: () => "dummy",
});

export const ProjectContextURLProvider: React.FC = ({ children }) => {
  const { project } = useParams<{ project: string }>();

  return (
    <ProjectContext.Provider
      value={{
        project,
        entityLink: (entityId) =>
          generatePath(`/:project/entity/:entity`, {
            project,
            entity: entityId,
          }),
      }}
    >
      {children}
    </ProjectContext.Provider>
  );
};
