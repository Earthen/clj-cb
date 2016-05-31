(ns earthen.clj-cb.bucket
  (:refer-clojure :exclude [get replace remove])
  (:import [com.couchbase.client.java Bucket]
           [com.couchbase.client.java.document JsonDocument]
           [com.couchbase.client.java.document.json JsonObject])
  (:require [clojure.data.json :as json]
            [earthen.clj-cb.utils :as u]))

(defn read-json
  "Reads a JSON value from input String.
  If data is nil, then nil is returned."
  [data]
  (when-not (nil? data) (json/read-json data true false "")))

(def ^{:doc "Wrapper of clojure.data.json/json-str."}
  write-json json/json-str)

(defn content
  [json-document]
  (if json-document
    (-> json-document
        .content
        .toString
        read-json)))

(defn document
  [jsondocument]
  {:id (.id jsondocument)
   :cas (.cas jsondocument)
   :expiry (.expiry jsondocument)
   :mutation-token (.mutationToken jsondocument)
   :content (content jsondocument)})


(defn create-counter
  "Increment or decrement a counter with 0 value
  and a default value of 20 with the default key/value timeout."
  ([bucket k ]
   (create-counter bucket k 0 1))
  ([bucket k delta initial]
   (.counter bucket k delta initial)))

(defn get
  ([bucket id]
   (get bucket id :json))
  ([bucket id format]
   (let [doc (.get bucket id 5 (u/time :SECONDS))]
     (if doc
       (if (= :raw format)
         (-> doc
             .content
            .toString)
         (document doc))))))

(defn get-and-lock
  [bucket id seconds]
  (let [doc (.getAndLock bucket id seconds)]
    (if doc
       (document doc))))

(defn touch
  "Renews the expiration time of a document with the default key/value timeout"
  [bucket id expiry]
  (.touch bucket id expiry))

(defn get-and-touch
  "Retrieve and touch a JsonDocument by its unique ID with the default key/value timeout."
  ([bucket id] (get-and-touch bucket id 0))
  ([bucket id expiry]
   (document (.getAndTouch bucket id expiry))))

(defn unlock-document
  [bucket document]
  (.unlock bucket (:id document) (:cas document)))

(defn create-json-document
  [id json]
  (let [json-object (JsonObject/fromJson (write-json json))]
    (JsonDocument/create id json-object)))

(defn replace
  [bucket id json]
  (let [json (if (string? json) (read-json json) json)
        doc (create-json-document id json)]
    (document (.upsert bucket doc ))))

(defn manager
  [bucket]
  (.bucketManager bucket))

(defn remove
  [bucket id]
  (.remove bucket id))

(defn close
  ([bucket] (close bucket 30 :SECONDS))
  ([bucket time type]
   (.close bucket time (u/time type))))

