# clj-cb

UNDER CONSTRUCTION

A Clojure java-client wrapper for Couchbase Server 4.

## Usage

```clojure
    (require '[earthen.clj-cb.cluster :as c]
                '[earthen.clj-cb.bucket :as b]
                '[earthen.clj-cb.bucket-manager :as bm])
    
    ;; we create the cluster
    (def cluster (c/create "localhost"))
    
    ;; we create a bucket
    (def bucket (c/create-bucket cluster "gamesim-sample"))
    
    ;; we get documents from bucket
    (b/get-json bucket "Aaron1")
        => {:hitpoints 23832, :level 141, :loggedIn true, :name "Aaron1", :jsonType "player", :experience 14248, :uuid "78edf902-7dd2-49a4-99b4-1c94ee286a33"}
    
    ;; to create/update a json
    (b/replace bucket "Aaron1" "{
        \"experience\": 248,
        \"hitpoints\": 23832,
        \"jsonType\": \"player\",
        \"level\": 141,
        \"loggedIn\": true,
        \"name\": \"Aaron1\",
        \"uuid\": \"78edf902-7dd2-49a4-99b4-1c94ee286a33\"
        }")
```
## License

Copyright © 2016 FIXME

Distributed under the Apache Public License either version 1.0 or (at
your option) any later version.
