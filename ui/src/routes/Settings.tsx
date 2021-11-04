import { CheckCircleIcon, XIcon } from "@heroicons/react/solid";
import { useState } from "react";
import { gql, useMutation, useQuery } from "urql";
import { Header, Main } from "../components/Layout";
import { CreateApiKeyDocument, GetApiKeysDocument } from "../types";

export const CREATE_API_KEY = gql`
  mutation CreateApiKey {
    createApiKey {
      id
      description
      createdAt
      secret
    }
  }
`;

export const GET_API_KEYS = gql`
  query GetApiKeys {
    apiKeys {
      id
      description
      createdAt
    }
  }
`;

const KeyAlert = ({
  secret,
  onDismiss,
}: {
  secret: string;
  onDismiss: () => void;
}) => {
  return (
    <div className="rounded-md bg-green-50 p-4">
      <div className="flex">
        <div className="flex-shrink-0">
          <CheckCircleIcon
            className="h-5 w-5 text-green-400"
            aria-hidden="true"
          />
        </div>
        <div className="ml-3">
          <p className="text-sm font-medium text-green-800">
            Your API key is{" "}
            <span className="font-mono bg-green-200">{secret}</span>. Please
            make a note of it as you will not be able to retrieve it again.
          </p>
        </div>
        <div className="ml-auto pl-3">
          <div className="-mx-1.5 -my-1.5">
            <button
              type="button"
              className="inline-flex bg-green-50 rounded-md p-1.5 text-green-500 hover:bg-green-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-green-50 focus:ring-green-600"
              onClick={onDismiss}
            >
              <span className="sr-only">Dismiss</span>
              <XIcon className="h-5 w-5" aria-hidden="true" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};


const CreateKeyPanel = ({ reloadKeys }: { reloadKeys: () => void }) => {
  const [, createKey] = useMutation(CreateApiKeyDocument);
  const [key, setKey] = useState<string | null | undefined>(null);

  const onCreateKey = async () => {
    const key = await createKey();
    setKey(key.data?.createApiKey.secret);
    reloadKeys();
  };

  return (
    <div className="bg-white shadow sm:rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <div className="sm:flex sm:items-start sm:justify-between">
          <div>
            <h3 className="text-lg leading-6 font-medium text-gray-900">
              Create an API Key
            </h3>
            <div className="mt-2 max-w-xl text-sm text-gray-500">
              <p>An API key allows you to ingest data into trawler.</p>
            </div>
          </div>
          <div className="mt-5 sm:mt-0 sm:ml-6 sm:flex-shrink-0 sm:flex sm:items-center">
            <button
              type="button"
              className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:text-sm"
              onClick={onCreateKey}
            >
              Create Key
            </button>
          </div>
        </div>
        {key && <KeyAlert secret={key} onDismiss={() => setKey(null)} />}
      </div>
    </div>
  );
};


export const Settings = () => {
  const [apiKeys, refetch] = useQuery({ query: GetApiKeysDocument });


  return (
    <>
      <Header>
        <div className="flex items-center">
          <div className="flex-1">Settings</div>
        </div>
      </Header>
      <Main>
        <CreateKeyPanel reloadKeys={refetch} />
        <div>API Keys</div>
        <div className="text-xs break-all w-1/2 text-gray-500">
          {apiKeys.data?.apiKeys.map((key) => (
            <div>{key.createdAt}</div>
          ))}
        </div>
      </Main>
    </>
  );
};
