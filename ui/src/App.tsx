import React from 'react';
import logo from './logo.svg';
import './App.css';
import { gql, Provider, useQuery } from "urql";
import { client } from './graphql';

const Test = () => {
  const [data] = useQuery({
    query: gql`
      {
        entityGraph(id: "2f4beece-c45b-49c8-9eb4-9d8af93b68b7", d: 1) {
          entityId

          facets {
            name
            value
          }
          type
        }
      }
    `,
  });

  console.log(data);

  return null;
}

function App() {
  return (
    <Provider value={client}>
      <div className="App">
        <Test /> 
      </div>
    </Provider>
  );
}

export default App;
