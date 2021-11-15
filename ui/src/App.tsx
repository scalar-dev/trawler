import "./App.css";
import { Provider } from "urql";
import { client } from "./graphql";
import { Layout } from "./components/Layout";
import {
  Route,
  BrowserRouter as Router,
  Switch,
  useRouteMatch,
} from "react-router-dom";
import { Dashboard } from "./routes/Dashboard";
import { Entity } from "./routes/Entity";
import { SignIn } from "./routes/SignIn";
import { Settings } from "./routes/Settings";
import { ProjectContextURLProvider } from "./ProjectContext";
import { Home } from "./routes/Home";
import { Ontology } from "./routes/Ontology";

const App = () => (
  <Provider value={client}>
    <Router>
      <Switch>
        <Route path="/sign-in" exact>
          <SignIn />
        </Route>

        <Route path="/" exact>
          <Home />
        </Route>

        <Route path="/settings" exact>
          <Layout>
            <Settings />
          </Layout>
        </Route>

        <Route path="/:project">
          <ProjectContextURLProvider>
            <Layout>
              <ProjectRoutes />
            </Layout>
          </ProjectContextURLProvider>
        </Route>
      </Switch>
    </Router>
  </Provider>
);

const ProjectRoutes = () => {
  const { path } = useRouteMatch();

  return (
    <Switch>
      <Route path={`${path}/entity/:entity`}>
        <Entity />
      </Route>

      <Route path={`${path}/ontology`}>
        <Ontology />
      </Route>

      <Route path={path}>
        <Dashboard />
      </Route>
    </Switch>
  );
};

export default App;
