#!/usr/bin/env bash

COUCHBASE_USER=${COUCHBASE_USER:-Administrator}
COUCHBASE_PASS=${COUCHBASE_PASS:-Admin123}


pushd `dirname $0`

#see:  https://developer.couchbase.com/documentation/server/3.x/admin/REST/rest-ddocs-create.html
curl -X PUT \
     -H 'Content-Type: application/json' \
     -d @views/by_year.ddoc \
     http://$COUCHBASE_USER:$COUCHBASE_PASS@localhost:8092/earthen_test2/_design/test_designdoc

popd
