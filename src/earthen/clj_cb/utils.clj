(ns earthen.clj-cb.utils
  (:refer-clojure :exclude [time])
  (:import [java.util.concurrent TimeUnit]
           [com.couchbase.client.java.bucket BucketType]))


(defn time
  "Transform type to a TimeUnit"
  [time-type]
  (cond (= :MICROSECONDS time-type)
        TimeUnit/MICROSECONDS
        (= :MILLISECONDS time-type)
        TimeUnit/MILLISECONDS
        (= :SECONDS time-type)
        TimeUnit/SECONDS
        (= :MINUTES time-type)
        TimeUnit/MINUTES
        (= :HOURS time-type)
        TimeUnit/HOURS
        (= :DAYS time-type)
        TimeUnit/DAYS))

(defn btype->map
  [type]
  (cond (= (BucketType/COUCHBASE) type)
        :COUCHBASE
        (= (BucketType/MEMCACHED) type)
        :MEMCACHED))

(defn map->btype
  ":COUCHBASE or :MEMCACHED"
  [type]
  (cond (= :COUCHBASE type)
        (BucketType/COUCHBASE)
        (= :MEMCACHED type)
        (BucketType/MEMCACHED)))
