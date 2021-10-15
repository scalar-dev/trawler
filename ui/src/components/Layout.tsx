import { Fragment, useContext, useEffect } from "react";
import { Disclosure, Menu, Transition } from "@headlessui/react";
import { MenuAlt1Icon, XIcon } from "@heroicons/react/outline";
import { classNames } from "../utils";
import { Search } from "./Search";
import { useHistory } from "react-router";
import { gql, useQuery } from "urql";
import { MeDocument } from "../types";
import { Link } from "react-router-dom";
import { ProjectContext } from "../ProjectContext";
import { CheckIcon } from "@heroicons/react/solid";

export const TwoColumn = () => (
  <div className="flex-grow w-full max-w-7xl mx-auto xl:px-8 lg:flex">
    <main className="min-w-0 flex-1 border-t border-gray-200 lg:flex">
      {/* Primary column */}
      <section
        aria-labelledby="primary-heading"
        className="min-w-0 flex-1 h-full flex flex-col overflow-hidden lg:order-last"
      >
        <h1 id="primary-heading" className="sr-only">
          Home
        </h1>
        {/* Your content */}
      </section>

      {/* Secondary column (hidden on smaller screens) */}
      <aside className="hidden lg:block lg:flex-shrink-0 lg:order-first">
        <div className="h-full relative flex flex-col w-96 border-r border-gray-200">
          {/* Your content */}
        </div>
      </aside>
    </main>
  </div>
);

export const Header: React.FC = ({ children }) => (
  <header className="bg-white shadow-sm">
    <div className="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8">
      <h1 className="text-lg leading-6 font-semibold text-gray-900">
        {children}
      </h1>
    </div>
  </header>
);

export const Main: React.FC = ({ children }) => (
  <main>
    <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">{children}</div>
  </main>
);

export const ME_QUERY = gql`
  query Me {
    me {
      email
      firstName
      lastName
    }
    projects {
      id
      name
      slug
    }
  }
`;

const getInitials = (firstName?: string | null, lastName?: string | null) => {
  return [firstName, lastName]
    .map((s) => (s && s.length > 0 ? s[0] : ""))
    .join("");
};

export const Layout: React.FC = ({ children }) => {
  const [me] = useQuery({ query: MeDocument });
  const { project } = useContext(ProjectContext);

  return (
    <>
      <div className="relative min-h-screen flex flex-col bg-gray-100">
        {/* Navbar */}
        <Disclosure as="nav" className="flex-shrink-0 bg-indigo-600">
          {({ open }) => (
            <>
              <div className="max-w-7xl mx-auto px-2 sm:px-4 lg:px-8">
                <div className="relative flex items-center justify-between h-16">
                  {/* Logo section */}
                  <div className="flex items-center px-2 lg:px-0 xl:w-64">
                    {/* <div className="flex-shrink-0">
                      <Link to="/">
                          <img
                            className="h-8 w-auto"
                            src="https://tailwindui.com/img/logos/workflow-mark-indigo-300.svg"
                            alt="Workflow"
                          />
                      </Link>
                    </div> */}
                    <div className="ml-4 text-xl text-indigo-300 hover:text-white font-bold">
                      <Link to="/">trawler</Link>
                    </div>
                  </div>

                  {project && (
                    <div className="flex-1 flex justify-center lg:justify-end">
                      <div className="w-full px-2 lg:px-6">
                        <label htmlFor="search" className="sr-only">
                          Search projects
                        </label>

                        <Search />
                      </div>
                    </div>
                  )}

                  <div className="flex lg:hidden">
                    {/* Mobile menu button */}
                    <Disclosure.Button className="bg-indigo-600 inline-flex items-center justify-center p-2 rounded-md text-indigo-400 hover:text-white hover:bg-indigo-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-indigo-600 focus:ring-white">
                      <span className="sr-only">Open main menu</span>
                      {open ? (
                        <XIcon className="block h-6 w-6" aria-hidden="true" />
                      ) : (
                        <MenuAlt1Icon
                          className="block h-6 w-6"
                          aria-hidden="true"
                        />
                      )}
                    </Disclosure.Button>
                  </div>
                  {/* Links section */}
                  <div className="hidden lg:block lg:w-80">
                    <div className="flex items-center justify-end">
                      <div className="flex">
                        <a
                          href="https://docs.trawler.dev"
                          className="px-3 py-2 rounded-md text-sm font-medium text-indigo-200 hover:text-white"
                          target="_blank"
                          rel="noreferrer"
                        >
                          Documentation
                        </a>
                        {!me.data?.me && (
                          <>
                            <Link
                              to="/sign-in"
                              className="px-3 py-2 rounded-md text-sm font-medium text-indigo-200 hover:text-white"
                              rel="noreferrer"
                            >
                              Sign in
                            </Link>
                            <div className="flex-shrink-0">
                              <button
                                type="button"
                                className="relative inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-500 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-800 focus:ring-indigo-500"
                              >
                                <span>Sign up</span>
                              </button>
                            </div>
                          </>
                        )}
                      </div>
                      {/* Profile dropdown */}
                      {me.data?.me && (
                        <Menu as="div" className="ml-4 relative flex-shrink-0">
                          <div>
                            <Menu.Button className="bg-indigo-700 flex text-sm rounded-full text-white focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-indigo-700 focus:ring-white">
                              <span className="sr-only">Open user menu</span>
                              <span className="inline-flex items-center justify-center h-8 w-8 rounded-full bg-indigo-400">
                                <span className="text-sm font-medium leading-none text-white">
                                  {getInitials(
                                    me.data.me.firstName,
                                    me.data.me.lastName
                                  )}
                                </span>
                              </span>
                            </Menu.Button>
                          </div>
                          <Transition
                            as={Fragment}
                            enter="transition ease-out duration-100"
                            enterFrom="transform opacity-0 scale-95"
                            enterTo="transform opacity-100 scale-100"
                            leave="transition ease-in duration-75"
                            leaveFrom="transform opacity-100 scale-100"
                            leaveTo="transform opacity-0 scale-95"
                          >
                            <Menu.Items className="origin-top-right absolute z-10 right-0 mt-2 w-48 rounded-md shadow-lg py-1 bg-white divide-y divide-gray-100 ring-1 ring-black ring-opacity-5 focus:outline-none">
                              <div className="py-1">
                                {me.data?.projects.map((proj) => (
                                  <Menu.Item key={proj.id}>
                                    {({ active }) => (
                                      <Link
                                        to={`/${proj.slug}`}
                                        className={classNames(
                                          active ? "bg-gray-100" : "",
                                          "flex px-4 py-2 text-sm text-gray-700"
                                        )}
                                      >
                                        <span className="flex-1">
                                          {proj.name}
                                        </span>
                                        {project === proj.slug && (
                                          <span className="text-indigo-600">
                                            <CheckIcon
                                              className="h-5 w-5"
                                              aria-hidden="true"
                                            />
                                          </span>
                                        )}
                                      </Link>
                                    )}
                                  </Menu.Item>
                                ))}
                              </div>
                              <div className="py-1">
                                <Menu.Item>
                                  {({ active }) => (
                                    <Link
                                      to="/settings"
                                      className={classNames(
                                        active ? "bg-gray-100" : "",
                                        "block px-4 py-2 text-sm text-gray-700"
                                      )}
                                    >
                                      Settings
                                    </Link>
                                  )}
                                </Menu.Item>
                                <Menu.Item>
                                  {({ active }) => (
                                    <a
                                      href="#"
                                      onClick={() => {
                                        localStorage.removeItem("jwt");
                                        window.location.pathname = "/";
                                      }}
                                      className={classNames(
                                        active ? "bg-gray-100" : "",
                                        "block px-4 py-2 text-sm text-gray-700"
                                      )}
                                    >
                                      Logout
                                    </a>
                                  )}
                                </Menu.Item>
                              </div>
                            </Menu.Items>
                          </Transition>
                        </Menu>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              <Disclosure.Panel className="lg:hidden">
                <div className="px-2 pt-2 pb-3">
                  <a
                    href="#"
                    className="block px-3 py-2 rounded-md text-base font-medium text-white bg-indigo-800"
                  >
                    Dashboard
                  </a>
                </div>
                <div className="pt-4 pb-3 border-t border-indigo-800">
                  <div className="px-2">
                    {me.data?.projects.map((proj) => (
                      <Link
                        to={`/${proj.slug}`}
                        key={proj.id}
                        className="flex block px-3 py-2 rounded-md text-base font-medium text-indigo-200 hover:text-indigo-100 hover:bg-indigo-600"
                      >
                        <span className="flex-1">{proj.name}</span>
                        {project === proj.slug && (
                          <span className="text-white">
                            <CheckIcon className="h-5 w-5" aria-hidden="true" />
                          </span>
                        )}
                      </Link>
                    ))}
                  </div>
                </div>
                <div className="pt-4 pb-3 border-t border-indigo-800">
                  <div className="px-2">
                    <Link
                      to="/settings"
                      className="mt-1 block px-3 py-2 rounded-md text-base font-medium text-indigo-200 hover:text-indigo-100 hover:bg-indigo-600"
                    >
                      Settings
                    </Link>
                    <a
                      href="#"
                      className="mt-1 block px-3 py-2 rounded-md text-base font-medium text-indigo-200 hover:text-indigo-100 hover:bg-indigo-600"
                    >
                      Sign out
                    </a>
                  </div>
                </div>
              </Disclosure.Panel>
            </>
          )}
        </Disclosure>

        {children}
      </div>
    </>
  );
};
