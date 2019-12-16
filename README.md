# clj-cb

A Clojure java-client wrapper for Couchbase Server 4. Now updated to support 6.0 with Couchbase Java client version 2.7.10. Simple N1QL query support has been added as well as basic N1QL prepared statement execution. Not all of the BNF-aware DSL clauses and expressions are yet supported. The unsupported clauses are JOIN, NEST, UNNEST, LET, UNION, INTERSECT and EXCEPT. Unsupported Expressions are add, between, concat, divide, exists, FALSE, in, is missing, is valued, MISSING, multiply, not between, not in, not like, NULL and subtract.

## Usage

```clojure
    (require '[earthen.clj-cb.cluster :as c]
                '[earthen.clj-cb.bucket :as b]
                '[earthen.clj-cb.bucket-manager :as bm])
    
    ;; we create the cluster
    (def cluster (c/create "localhost"))
    
    ;; we create a bucket
    (def bucket (c/open-bucket cluster "gamesim-sample"))

    ;; we authenticate with the bucket password, note: the password parameter on the c/bucket-open
    ;; no longer works and you will get the exception MixedAuthenticationException...
    ;; ...Mixed mode authentication not allowed, use Bucket credentials, User credentials (rbac) or Certificate auth


    (c/authenticate "gamesim-sample" "secret")
    
    ;; we get documents from bucket mapped
    (b/get bucket "Aaron1")
        => {:id "Aaron1", :cas 29856054464387, :expiry 0, :mutation-token nil, :content {:hitpoints 23832, :level 141, :loggedIn true, :name "Aaron1", :jsonType "player", :experience 248, :uuid "78edf902-7dd2-49a4-99b4-1c94ee286a33"}}
    
    ;; you can also get the json without mapping
     (b/get bucket "Aaron1" :raw)
         => "{\"hitpoints\":23832,\"level\":141,\"loggedIn\":true,\"name\":\"Aaron1\",\"jsonType\":\"player\",\"experience\":248,\"uuid\":\"78edf902-7dd2-49a4-99b4-1c94ee286a33\"}"
    
    ;; to create/update a json
    (b/replace! bucket "Aaron1" "{
        \"experience\": 248,
        \"hitpoints\": 23832,
        \"jsonType\": \"player\",
        \"level\": 141,
        \"loggedIn\": true,
        \"name\": \"Aaron1\",
        \"uuid\": \"78edf902-7dd2-49a4-99b4-1c94ee286a33\"
        \"sponsors\": [\"Nike\" \"Reebok\" \"Freddies\"]
        \"tues\": [{\"symptom\": {\"asthma\": \"Ventolin Inhaler\"}}
                   {\"symptom\": {\"pain\": \"Asprin\"}}]  
        }")
        => {:id "Aaron1", :cas 1569768233881239552, :expiry 0, :mutation-token nil, :content {:name "Aaron1", :hitpoints 23832, :experience 248, :level 141, :loggedIn true, :tues [{:symptom {:asthma "Ventolin Inhler"}} {:symptom {:pain "Asprin"}}], :sponsors ["Nike" "Reebok" "Freddies"], :uuid "78edf902-7dd2-49a4-99b4-1c94ee286a33", :jsonType "player"}}
        
        (b/replace! bucket "Aaron1" {:name "Aaron1", :hitpoints 23832, :experience 248, :level 141, :loggedIn true, :tues [{:symptom {:asthma "Ventolin Inhler"}} {:symptom {:pain "Asprin"}}], :sponsors ["Nike" "Reebok" "Freddies"], :uuid "78edf902-7dd2-49a4-99b4-1c94ee286a33", :jsonType "player"})

        => {:id "Aaron1", :cas 1569768233881239552, :expiry 0, :mutation-token nil, :content {:name "Aaron1", :hitpoints 23832, :experience 248, :level 141, :loggedIn true, :tues [{:symptom {:asthma "Ventolin Inhler"}} {:symptom {:pain "Asprin"}}], :sponsors ["Nike" "Reebok" "Freddies"], :uuid "78edf902-7dd2-49a4-99b4-1c94ee286a33", :jsonType "player"}}

       ;; To lookup particular objects / values in a document, pass a vector of paths. Vectors in
       ;; the response are converted from [n] to <n> as brackets are not valid in keywords.
       ;; The DocumentFragment used to execute the request also has an .exists method that returns
       ;; true or false for a given path. This has not been implemented. Instead you will get a
       ;; {:keyword nil} response for a noneixstent path.
	  
       (b/lookup-in bucket "Aaron1" ["name" "sponsors" "tues[1]" "injuries"])

       => {:name "Aaron1", :sponsors ["Nike" "Reebok" "Freddies"], :tues<1> {:symptom {:pain "Asprin"}}, :injuries nil}

       ;; We execute a simple N1QL query. Results are converted to a Clojure map and the JSON rows are returned as a 
       ;; Clojure vector

       (prn (:rows (b/query bucket "SELECT meta().id FROM `earthen_test` LIMIT 3"))

       => [{:id bigger-living-clojure} {:id bigger-living-clojure-0} {:id bigger-living-clojure-1}

       ;; We create a prepared N1QL statement with parameterized variables using the Couchbase BNF-aware DSL.

       (b/statement {:select ["foo"]
          	     :from ["bucket"]
                     :where [{:eq ["foo" "$val1"]}
                             {:or {:ne ["foo" "$val2"]}}]})

       => #object[com.couchbase.client.java.query.dsl.path.DefaultGroupByPath 0x2c738ab4 "SELECT foo FROM bucket WHERE foo = $val1 OR foo != $val2"]

       ;; We execute a parameterized N1QL query. By default queries are not ad hoc, assuming, as we have gone to the trouble
       ;; to prepare them, we want to use the client and server query optimizations. Almost all of the keywords map to
       ;; exactly what you would expect in the Couchbase DSL, :select, :select-distinct and so on. :i creates a
       ;; `backquoted` identifier as does the :as keyword, "`x` AS a". :s-as creates an unquoted identifer, "meta().id AS b".
       ;; Parameters that you want to replace are passed in a Clojure map {"key" "value"}. Keys have to be "Strings" not 
       ;; :keywords. 
       ;; See the extensive tests for more exmples and details. 

        (prn (:rows (b/p-query (fx/bucket) {:select [{:s-as ["a" "meta().id"]} {:as ["b" "pages"]}]
                                            :from [{:i "earthen_test"}]
                                            :use-index ["#primary"]
                                            :where [{:like ["meta().id" ["bigger-%"]]}
                                                    {:and {:gt ["meta().id" "$id"]}}]
                                            :order-by {:desc "meta().id"}
                                            :limit 2}
                               {"id" "bigger-living-clojure-7"})))

       => [{:a "bigger-living-clojure-9", :b 12} {:a "bigger-living-clojure-8", :b 12}]
```
## License

Copyright © 2019

Distributed under the Apache Public License either version 2.0.
