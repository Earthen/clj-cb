#!/bin/bash
set -e

pushd `dirname $0`
docker-compose down
popd
