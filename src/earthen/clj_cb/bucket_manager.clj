(ns earthen.clj-cb.bucket-manager
  (:refer-clojure :exclude [flush])
  (:require [earthen.clj-cb.utils :as u])
  (:import [com.couchbase.client.java.bucket BucketManager]
           [com.couchbase.client.java.view SpatialView DesignDocument DefaultView]))


(defn default-view->map
  [defaultview]
  {:name (.name defaultview)
   :map (.map defaultview)
   :reduce (.reduce defaultview)})

(defn map->default-view
  [{:keys [name map reduce]}]
  (DefaultView/create name map reduce))

(defn design-document->map
  "Converts a DesignDocument to a map"
  [document]
  {:name (.name document)
   :views (into [] (map default-view->map (.views document)))})

(defn designed-documents
  [bucket-manager]
  (map design-document->map (.getDesignDocuments bucket-manager)))

(defn create-spatial-view
  "Creates a SpatialView giving a name and the string of the function of the view"
  [name string]
  (SpatialView/create name string))

(defn create-view
  ([name string-map]
   (create-view name string-map nil))
  ([{:keys [name map reduce]}]
   (create-view name map reduce))
  ([name string-map string-reduce]
   (DefaultView/create name string-map string-reduce)))

(defn create-design-document
  "Creates a DesignDocument giving the name and a-list of views"
  [name a-list]
  (DesignDocument/create name a-list))

(defn map->design-document
  "Converts a DesignDocument to a map"
  [{:keys [name views]}]
  (create-design-document name (map map->default-view views)))

(defn insert-design-document!
  "Inserts the design to the bucketManager"
  [bucket-manager design]
  (.insertDesignDocument bucket-manager design))

(defn flush!
  "On 2.2.7 doesn't work properly"
  ([bucket-manager]
   (flush! bucket-manager 60 :SECONDS))
  ([bucket-manager time time-type]
   (.flush bucket-manager time (u/time time-type))))

