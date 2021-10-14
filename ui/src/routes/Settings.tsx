import { useState, useEffect } from "react";
import { gql, useMutation } from "urql";
import { Header, Main } from "../components/Layout";
import { GetCollectTokenDocument } from "../types";

export const GET_COLLECT_TOKEN = gql`
  mutation GetCollectToken {
    collectToken {
      jwt
    }
  }
`;

export const Settings = () => {
  const [, getCollectToken] = useMutation(GetCollectTokenDocument);
  const [collectToken, setCollectToken] = useState<string | undefined>("");

  useEffect(() => {
    getCollectToken().then((result) =>
      setCollectToken(result.data?.collectToken.jwt)
    );
  }, [setCollectToken]);

  return (
    <>
      <Header>
        <div className="flex items-center">
          <div className="flex-1">Settings</div>
        </div>
      </Header>
      <Main>
        Collect token
        <div className="text-xs break-all w-1/2 text-gray-500">
          {collectToken}
        </div>
      </Main>
    </>
  );
};
