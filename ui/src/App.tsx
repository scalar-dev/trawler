import "./App.css";
import { Provider } from "urql";
import { client } from "./graphql";
import { Layout } from "./components/Layout";
import { Route, BrowserRouter as Router } from "react-router-dom";
import { Dashboard } from "./routes/Dashboard";
import { Entity } from "./routes/Entity";

const App = () => (
  <Provider value={client}>
    <Router>
      <Layout>
        <Route path="/" exact>
          <Dashboard />
        </Route>
        <Route path="/entity/:entity">
          <Entity />
        </Route>
      </Layout>
    </Router>
  </Provider>
);

export default App;
