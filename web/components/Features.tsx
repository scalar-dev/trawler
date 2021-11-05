import {
  AnnotationIcon,
  GlobeAltIcon,
  LightningBoltIcon,
  ScaleIcon,
} from "@heroicons/react/outline";

export const Features = () => {
  const features = [
    {
      name: "Connect your data",
      description: (
        <p>
          Trawler&apos;s agent can ingest data from a variety of data sources
          including SQL, flat-files and Spark. Use the Python API to write your
          own integrations.{" "}
          <a
            className="text-indigo-600 hover:text-indigo-500"
            href="https://docs.trawler.dev/integration/connectors/agent"
          >
            Read the docs
          </a>
        </p>
      ),
      icon: GlobeAltIcon,
    },
    {
      name: "Flexible schema",
      description: (
        <p>
          Trawler builds on semantic web technologies like JSON-LD to accurately
          model your metadata. Extend the ontology to add your own metadata
          types.{" "}
          <a
            className="text-indigo-600 hover:text-indigo-500"
            href="https://docs.trawler.dev/concepts/#ontology"
          >
            Read the docs
          </a>
        </p>
      ),
      icon: ScaleIcon,
    },
    {
      name: "Simple deployment",
      description: (
        <p>
          Trawler can be deployed simply and easily on top of a postgres
          database.{" "}
          <a
            className="text-indigo-600 hover:text-indigo-500"
            href="https://docs.trawler.dev/architecture"
          >
            Read the docs
          </a>
        </p>
      ),
      icon: LightningBoltIcon,
    },
    {
      name: "Open source",
      description: (
        <p>
          As well as supporting open APIs and standard data formats, Trawler
          itself is open source.{" "}
          <a
            className="text-indigo-600 hover:text-indigo-500"
            href="https://github.com/scalar-dev/trawler"
          >
            Check out our Github
          </a>
        </p>
      ),
      icon: AnnotationIcon,
    },
  ];

  return (
    <div className="relative bg-white py-16 sm:py-24 lg:py-32">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="lg:text-center">
          <h2 className="text-base text-indigo-600 font-semibold tracking-wide uppercase">
            Flexible data model
          </h2>
          <p className="mt-2 text-3xl leading-8 font-extrabold tracking-tight text-gray-900 sm:text-4xl"></p>
          <p className="mt-4 max-w-2xl text-xl text-gray-500 lg:mx-auto">
            Trawler&apos;s flexible data model allows you to ingest metadata
            from across your organisation.
          </p>
        </div>

        <div className="mt-10">
          <dl className="space-y-10 md:space-y-0 md:grid md:grid-cols-2 md:gap-x-8 md:gap-y-10">
            {features.map((feature) => (
              <div key={feature.name} className="relative">
                <dt>
                  <div className="absolute flex items-center justify-center h-12 w-12 rounded-md bg-indigo-500 text-white">
                    <feature.icon className="h-6 w-6" aria-hidden="true" />
                  </div>
                  <p className="ml-16 text-lg leading-6 font-medium text-gray-900">
                    {feature.name}
                  </p>
                </dt>
                <dd className="mt-2 ml-16 text-base text-gray-500">
                  {feature.description}
                </dd>
              </div>
            ))}
          </dl>
        </div>
      </div>
    </div>
  );
};

