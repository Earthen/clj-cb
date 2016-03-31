(ns clj-cb.bucket-manager
  (:require [clj-cb.utils :as u])
  (:import [com.couchbase.client.java.bucket BucketManager]))

(defn flush
  ([bucket-manager]
   (flush bucket-manager 20 :SECONDS))
  ([bucket-manager time time-type]
   (.flush bucket-manager time (u/time time-type))))

