import React from "react";

export const ProjectContext = React.createContext<{
  projectId: string;
  projectName: string;
}>({ projectId: "", projectName: "" });
