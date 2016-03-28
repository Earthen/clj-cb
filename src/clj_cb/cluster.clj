(ns clj-cb.cluster
  (:import [com.couchbase.client.java.CouchbaseCluster]))

(defn cb-cluster-builder
  "Create and sets the cluster"
  [string]
  (let [urls (if (string? string) (vector string) string)]
    (CouchbaseCluster/create urls)))
