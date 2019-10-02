(ns earthen.clj-cb.cluster
  (:require [earthen.clj-cb.utils :as u])
  (:import [com.couchbase.client.java CouchbaseCluster]
           [com.couchbase.client.java.bucket BucketManager]
           [com.couchbase.client.java.cluster DefaultBucketSettings BucketSettings]))

(defn create
  "Create and sets the cluster/s with a vector or a string, if no params passed it uses default url (172.0.0.1)"
  ([]
   (CouchbaseCluster/create))
  ([string]
   (let [urls (if (string? string) (vector string) string)]
     (CouchbaseCluster/create urls))))

(defn authenticate
  "Authenticate with the bucket password, note: the password parameter on the c/bucket-open
    no longer works and you will get the exception MixedAuthenticationException Mixed mode
    authentication not allowed, use Bucket credentials, User credentials (rbac) or Certificate auth"
  [cluster username password]
  (.authenticate cluster username password))
  

(defn open-bucket
  "Open a bucket from a the cluster"
  ([cluster bucket-name]
   (open-bucket cluster bucket-name 20 :SECONDS))
  ([cluster bucket-name time time-type]
   (.openBucket cluster bucket-name time (u/time time-type))))

(defn manager
  "Returns a cluster manager giving a cluster and credentials"
  [cluster {username :username password :password}]
  (.clusterManager cluster username password))

(defn disconnect
  ([cluster]
   (disconnect cluster 20 :SECONDS))
  ([cluster timeout type]
   (.disconnect cluster timeout (u/time type))))

(defn create-bucket-settings
  "Create a bucket settings object for insert"
  [{:keys [name type quota port password replicas index-replicas flush?]}]
  (-> (DefaultBucketSettings/builder)
      (.name name)
      (.type (u/map->btype type))
      (.quota quota)
      (.port port)
      (.password password)
      (.replicas replicas)
      (.indexReplicas index-replicas)
      (.enableFlush flush?)
      (.build)))

(defn- bucket-settings
  [^BucketSettings bucket]
  {:name (.name bucket)
   :type (u/btype->map(.type bucket))
   :quota (.quota bucket)
   :port (.port bucket)
   :password (.password bucket)
   :replicas (.replicas bucket)
   :index-replicas (.indexReplicas bucket)
   :flush? (.enableFlush bucket)})

(defn buckets
  "Giving a cluster manager returns all the bucket settings from the cluster"
  ([cluster {:keys [username password] :as credentials :or {username "" password ""}}]
   (buckets (manager cluster credentials)))
  ([cluster-manager]
   (map bucket-settings (.getBuckets cluster-manager))))


(defn insert-bucket!
  "Creates a new bucket in the cluster giving a bucket settings"
  ([cluster-manager bucket-settings]
   (insert-bucket! cluster-manager bucket-settings 60 :SECONDS))
  ([cluster-manager bucket-settings time time-type]
   (try
     (.insertBucket cluster-manager (create-bucket-settings bucket-settings) time (u/time time-type))
     (catch Exception e (str "caught exception: " (.getMessage e))))))

(defn remove-bucket!
  "Creates a new bucket in the cluster giving a bucket settings"
  ([cluster-manager bucket-name]
   (remove-bucket! cluster-manager bucket-name 60 :SECONDS))
  ([cluster-manager bucket-name time time-type]
   (try
     (.removeBucket cluster-manager bucket-name time (u/time time-type))
     (catch Exception e (str "caught exception: " (.getMessage e))))))
