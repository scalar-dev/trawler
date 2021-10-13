import "./App.css";
import { Provider } from "urql";
import { client } from "./graphql";
import { Layout } from "./components/Layout";
import { Route, BrowserRouter as Router } from "react-router-dom";
import { Dashboard } from "./routes/Dashboard";
import { Entity } from "./routes/Entity";
import { SignIn } from "./routes/SignIn";

const App = () => (
  <Provider value={client}>
    <Router>
      <Route path="/sign-in" exact>
        <SignIn />
      </Route>
      <Route path="/" exact>
        <Layout>
          <Dashboard />
        </Layout>
      </Route>
      <Route path="/entity/:entity">
        <Layout>
          <Entity />
        </Layout>
      </Route>
    </Router>
  </Provider>
);

export default App;
