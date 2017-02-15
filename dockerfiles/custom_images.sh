#!/bin/sh

set -e

HUB_USER=$1
if [ -z $HUB_USER ]; then echo "please pass your docker hub username as an arg"; exit 2; fi

is_logged=$(docker info | grep -c Username) || true
if [ $is_logged -ne 1 ]; then
    docker login
fi

sh agents/build.sh
sh codenvy/build.sh
sh init/build.sh

# retag
docker tag codenvy/init:nightly $HUB_USER/init:nightly
docker tag codenvy/codenvy:nightly $HUB_USER/codenvy:nightly
docker tag codenvy/agents:nightly $HUB_USER/agents:nightly
# push
docker push $HUB_USER/init:nightly
docker push $HUB_USER/codenvy:nightly
docker push $HUB_USER/agents:nightly
