(ns clj-cb.cluster
  (:require [clj-cb.utils :as u])
  (:import [com.couchbase.client.java CouchbaseCluster]))

(defn cb-cluster-builder
  "Create and sets the cluster"
  [string]
  (let [urls (if (string? string) (vector string) string)]
    (CouchbaseCluster/create urls)))

(defn create-bucket
  ([cluster bucket-name]
   (create-bucket cluster bucket-name 20 :seconds))
  ([cluster bucket-name time time-type]
   (.openBucket cluster bucket-name time (u/time time-type))))


(defn cb-cluster-manager
  [cluster username password]
  (.clusterManager username password))

(defn disconnect
  ([cluster]
   (disconnect cluster 20 :seconds))
  ([cluster timeout type]
   (.disconnect cluster (u/time type))))
