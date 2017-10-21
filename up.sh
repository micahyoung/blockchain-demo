#!/bin/bash
set -o errexit
set -o pipefail
set -o nounset

HOSTNAME=${1:?"!"}
DOMAIN=${2:?"!"}
MINER_URL=${3:?"!"}
pushd sender_app
  mvn clean package -DskipTests
  cf push sender -p target/*.jar --no-route --no-start
  cf map-route -n $HOSTNAME $DOMAIN 
  
  cf set-env sender SPRING_APPLICATION_JSON '{"config":{"nodeUrl":"'$MINER_URL'","senderAccountPassword":"password"}}'
  
  cf start sender
popd
