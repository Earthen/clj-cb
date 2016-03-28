(ns clj-cb.bucket
  (:import [com.couchbase.client.java.Bucket]
           [com.couchbase.client.java.document.JsonDocument]
           [com.couchbase.client.java.document.json.JsonObject])
  (:require [clojure.data.json :as json]))

(defn read-json
  "Reads a JSON value from input String.
  If data is nil, then nil is returned."
  [data]
  (when-not (nil? data) (json/read-json data true false "")))

(def ^{:doc "Wrapper of clojure.data.json/json-str."}
  write-json json/json-str)


(defn create-bucket
  "Increment or decrement a counter with 0 value
  and a default value of 0 with the default key/value timeout."
  [cluster bucket-name]
  (.openBucket cluster bucket-name))

(defn create-counter
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
  [bucket id]
  (read-json (get bucket id)))

(defn get-and-lock
  [bucket id seconds]
  (let [document (.getAndLock bucket id seconds)]
    (if document
      nil
      (.content document))))

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
  (.unlock bucket k (.cas document)))

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
  [bucket & {:keys [microseconds milliseconds seconds minutes hours days] :or [seconds 75]}]
  (let [[time time-unit]  (cond microseconds
                                [microseconds TimeUnit/MICROSECONDS]
                                milliseconds
                                [milliseconds TimeUnit/MILLISECONDS]
                                seconds
                                [seconds TimeUnit/SECONDS]
                                minutes
                                [minutes TimeUnit/MINUTES]
                                hours
                                [hours TimeUnit/HOURS]
                                days
                                [days TimeUnit/DAYS])]
    (.close bucket time time-unit)))
