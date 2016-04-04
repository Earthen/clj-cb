(ns earthen.clj-cb.cluster
  (:require [earthen.clj-cb.utils :as u])
  (:import [com.couchbase.client.java CouchbaseCluster]
           [com.couchbase.client.java.bucket BucketManager]))

(defn create
  "Create and sets the cluster/s with a vector or a string"
  [string]
  (let [urls (if (string? string) (vector string) string)]
    (CouchbaseCluster/create urls)))

(defn create-bucket
  ([cluster bucket-name]
   (create-bucket cluster bucket-name 20 :SECONDS))
  ([cluster bucket-name time time-type]
   (.openBucket cluster bucket-name time (u/time time-type))))


(defn manager
  [cluster {username :username password :password}]
  (.clusterManager cluster username password))

(defn disconnect
  ([cluster]
   (disconnect cluster 20 :SECONDS))
  ([cluster timeout type]
   (.disconnect cluster (u/time type))))

(defn buckets
  ([cluster {username :username password :password}]
   (buckets (manager cluster username password)))
  ([cluster-manager]
   (.getBuckets cluster-manager)))
