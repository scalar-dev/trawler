import { createClient } from 'urql';

export const client = createClient({
  url: "http://localhost:3000/graphql",
  fetchOptions: {
    headers: {
      Authorization: `Bearer ${process.env.REACT_APP_JWT}`,
    },
  },
});