import "./App.css";
import { Provider } from "urql";
import { client } from "./graphql";
import { Layout } from "./components/Layout";
import { Route, BrowserRouter as Router, Switch } from "react-router-dom";
import { Dashboard } from "./routes/Dashboard";
import { Entity } from "./routes/Entity";
import { SignIn } from "./routes/SignIn";

const App = () => (
  <Provider value={client}>
    <Router>
      <Switch>
        <Route path="/sign-in" exact>
          <SignIn />
        </Route>
        <Layout>
          <Route path="/" exact>
            <Dashboard />
          </Route>
          <Route path="/entity/:entity">
            <Entity />
          </Route>
        </Layout>
      </Switch>
    </Router>
  </Provider>
);

export default App;
