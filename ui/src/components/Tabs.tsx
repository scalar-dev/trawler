import { useHistory } from "react-router";
import { Link } from "react-router-dom";
import { classNames } from "../utils";

export type Tab = {
  name: string;
  href: string;
  current?: boolean;
  icon: React.ComponentType<any>;
};

export const Tabs = ({ tabs }: { tabs: Tab[] }) => {
  const history = useHistory();
  return (
    <div>
      <div className="sm:hidden">
        <label htmlFor="tabs" className="sr-only">
          Select a tab
        </label>
        <select
          id="tabs"
          name="tabs"
          className="block w-full focus:ring-indigo-500 focus:border-indigo-500 border-gray-300 rounded-md"
          onChange={(e) =>
            history.push(tabs.find((tab) => tab.name === e.target.value)!.href)
          }
          defaultValue={tabs.find((tab) => tab.current)?.name}
        >
          {tabs.map((tab) => (
            <option key={tab.name}>{tab.name}</option>
          ))}
        </select>
      </div>
      <div className="hidden sm:block">
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8" aria-label="Tabs">
            {tabs.map((tab) => (
              <Link
                to={tab.href}
                key={tab.name}
                className={classNames(
                  tab.current
                    ? "border-indigo-500 text-indigo-600"
                    : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300",
                  "group inline-flex items-center py-4 px-1 border-b-2 font-medium text-sm"
                )}
                aria-current={tab.current ? "page" : undefined}
              >
                <tab.icon
                  className={classNames(
                    tab.current
                      ? "text-indigo-500"
                      : "text-gray-400 group-hover:text-gray-500",
                    "-ml-0.5 mr-2 h-5 w-5"
                  )}
                  aria-hidden="true"
                />
                <span>{tab.name}</span>
              </Link>
            ))}
          </nav>
        </div>
      </div>
    </div>
  );
};
