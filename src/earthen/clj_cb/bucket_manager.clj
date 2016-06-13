(ns earthen.clj-cb.bucket-manager
  (:refer-clojure :exclude [flush])
  (:require [earthen.clj-cb.utils :as u])
  (:import [com.couchbase.client.java.bucket BucketManager]
           [com.couchbase.client.java.view SpatialView DesignDocument]))

(defn create-spatial-view
  "Creates a SpatialView giving a name and the string of the function of the view"
  [name string]
  (SpatialView/create name string))

(defn create-design-document
  "Creates a DesignDocument giving the name and a-list of views"
  [name a-list]
  (DesignDocument/create name a-list))

(defn insert-design-document
  "Inserts the design to the bucketManager"
  [bucket-manager design]
  (.insertDesignDocument bucket-manager design))

(defn flush
  "On 2.2.7 doesn't work properly"
  ([bucket-manager]
   (flush bucket-manager 60 :SECONDS))
  ([bucket-manager time time-type]
   (.flush bucket-manager time (u/time time-type))))

