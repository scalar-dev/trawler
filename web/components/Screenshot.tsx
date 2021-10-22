
export const Screenshot = () => (
  <div className="relative bg-gray-50 pt-16 sm:pt-24 lg:pt-32" id="product">
    <div className="mx-auto max-w-md px-4 text-center sm:px-6 sm:max-w-3xl lg:px-8 lg:max-w-7xl">
      <div>
        <h2 className="text-base font-semibold tracking-wider text-cyan-600 uppercase">
          Graph based data catalogue.
        </h2>
        <p className="mt-2 text-3xl font-extrabold text-gray-900 tracking-tight sm:text-4xl">
          Connect metadata across datasets and systems.
        </p>
        <p className="mt-5 max-w-prose mx-auto text-xl text-gray-500">
          Trawler's graph data model supports column-level data lineage so you
          can trace data flow throughout your datasets and systems.
        </p>
      </div>
      <div className="mt-12 -mb-10 sm:-mb-24 lg:-mb-80">
        <img
          className="rounded-lg shadow-xl ring-1 ring-black ring-opacity-5"
          src="/screenshot.png"
          alt=""
        />
      </div>
    </div>
  </div>
);