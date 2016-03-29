(ns clj-cb.bucket
  (:import [com.couchbase.client.java Bucket]
           [com.couchbase.client.java.document JsonDocument]
           [com.couchbase.client.java.document.json JsonObject])
  (:require [clojure.data.json :as json]
            [clj-cb.utils :as u]))

(defn read-json
  "Reads a JSON value from input String.
  If data is nil, then nil is returned."
  [data]
  (when-not (nil? data) (json/read-json data true false "")))

(def ^{:doc "Wrapper of clojure.data.json/json-str."}
  write-json json/json-str)


(defn create-bucket
  ([cluster bucket-name]
   (create-bucket cluster bucket-name 20 :seconds))
  ([cluster bucket-name time time-type]
   (.openBucket cluster bucket-name time (u/time-utils time-type))))

(defn create-counter
  "Increment or decrement a counter with 0 value
  and a default value of 20 with the default key/value timeout."
  ([bucket k ]
   (create-counter bucket k 0 1))
  ([bucket k delta initial]
   (.counter bucket k delta initial)))

(defn get
  [bucket id]
  (let [document (.get bucket id)]
    (if document
      (-> document
        .content
        .toString))))

(defn get-json
  "Gets a json document"
  [bucket id]
  (read-json (get bucket id)))

(defn get-and-lock
  [bucket id seconds]
  (let [document (.getAndLock bucket id seconds)]
    (if document
      nil
      (.content document))))

(defn touch
  "Renews the expiration time of a document with the default key/value timeout"
  [bucket id expiry]
  (.touch bucket id expiry))

(defn get-and-touch
  "Retrieve and touch a JsonDocument by its unique ID with the default key/value timeout."
  ([bucket id] (get-and-touch bucket id 0))
  ([bucket id expiry]
   (-> (.getAndTouch bucket id expiry)
       .content
       .toString
       read-json)))

(defn unlock-document
  [bucket id document]
  (.unlock bucket id (.cas document)))

(defn- create-json-document
  [id json]
  (let [json-object (JsonObject/fromJson (write-json json))]
    (JsonDocument/create id json-object)))

(defn replace
  [bucket id json]
  (let [document (create-json-document id json)]
    (.upsert bucket document)))

(defn remove
  [bucket id]
  (.remove bucket id))

(defn close
  ([bucket] (close bucket 30 :seconds))
  ([bucket time type]
   (.close bucket time (u/time-utils type))))

