(ns earthen.clj-cb.bucket-manager
  (:refer-clojure :exclude [flush])
  (:require [clj-cb.utils :as u])
  (:import [com.couchbase.client.java.bucket BucketManager]))

(defn flush
  ([bucket-manager]
   (flush bucket-manager 60 :SECONDS))
  ([bucket-manager time time-type]
   (.flush bucket-manager time (u/time time-type))))

