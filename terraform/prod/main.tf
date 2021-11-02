
resource "scaleway_k8s_cluster" "cluster" {
  name    = "trawler-prod"
  version = "1.22.2"
  cni     = "cilium"
}

resource "scaleway_k8s_pool" "pool" {
  cluster_id = scaleway_k8s_cluster.cluster.id
  name       = "trawler-prod"
  node_type  = "DEV1-M"
  size       = 2
}
