(ns clj-cb.cluster
  (:require [clj-cb.utils :as u])
  (:import [com.couchbase.client.java CouchbaseCluster]))

(defn cb-cluster-builder
  "Create and sets the cluster"
  [string]
  (let [urls (if (string? string) (vector string) string)]
    (CouchbaseCluster/create urls)))

(defn cb-cluster-manager
  [cluster username password]
  (.clusterManager username password))

(defn disconnect
  ([cluster]
   (disconnect cluster 20 :seconds))
  ([cluster timeout type]
   (.disconnect cluster (u/time-utils type)))
