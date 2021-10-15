import { useHistory } from "react-router";
import { useQuery } from "urql";
import { MeDocument } from "../types";

export const Home = () => {
  const history = useHistory();
  const [me] = useQuery({ query: MeDocument });

  if (me.fetching) {
    return (
      <div className="w-screen h-screen flex bg-indigo-500">
        <div className="m-auto animate-bounce text-4xl font-black text-white">
          trawler
        </div>
      </div>
    );
  }

  if (me.data?.projects && me.data.projects.length > 0) {
    history.push(`/${me.data.projects[0].slug}`);
  } else {
    return (
      <div className="w-screen h-screen flex bg-indigo-500">
        <div className="m-auto font-black text-4xl text-white text-center">
          You don't have any projects :-(
        </div>
      </div>
    );
  }

  return null;
};
