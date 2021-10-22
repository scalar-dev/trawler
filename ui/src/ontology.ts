export const nameFacet = (entity: any): string =>
  entity.facets.find((facet: any) => facet.uri === "http://schema.org/name")
    ?.value[0] || "";

export const dataTypeFacet = (entity: any): string =>
  entity.facets.find(
    (facet: any) => facet.uri === "http://trawler.dev/schema/core#dataType"
  )?.value[0] || "";

export const fields = (entity: any) =>
  entity.facets.find(
    (facet: any) => facet.uri === "http://trawler.dev/schema/core#hasField"
  )?.value || [];
