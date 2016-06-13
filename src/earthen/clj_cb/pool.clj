(ns earthen.clj-cb.pool
  (:require [clj-cb.cluster :as cluster]
            [clj-cb.bucket :as bucket]))

(def cb-clusters
  "Only one cluster per group"
  (ref {}))

(def cb-default-cluster
  (ref nil))

(defn- cb-cluster-builder
  "Create and sets the cluster"
  [string]
  (dosync (ref-set cb-cluster (cluster/cb-cluster-builder urls))))

(def created-buckets (ref {}))

(defn- create-bucket
  [name]
  (.openBucket @cb-cluster name))

(defn get-bucket
  ([name]
   (get-bucket name @cb-default-cluster @created-buckets))
  ([name cluster bucket])
  (let [k (keyword name)]
    (when ((complement contains?) @created-buckets k)
      (dosync (ref-set a (merge @a {k (create-bucket name)}))))
    (k @created-buckets)))
