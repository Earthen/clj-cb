(ns clj-cb.utils
  (:import [java.util.concurrent TimeUnit]))


(defn time-utils
  "Transform type to a TimeUnit"
  [time-type]
  (cond (= :microseconds time-type)
        TimeUnit/MICROSECONDS
        (= :milliseconds time-type)
        TimeUnit/MILLISECONDS
        (= :seconds time-type)
        TimeUnit/SECONDS
        (= :minutes time-type)
        TimeUnit/MINUTES
        (= :hours time-type)
        TimeUnit/HOURS
        (= :days time-type)
        TimeUnit/DAYS))
