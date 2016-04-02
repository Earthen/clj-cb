(ns earthen.clj-cb.utils
  (:import [java.util.concurrent TimeUnit]))


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
