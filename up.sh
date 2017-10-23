#!/bin/bash
set -o errexit
set -o pipefail
set -o nounset

HOSTNAME=${1:?"!"}
DOMAIN=${2:?"!"}
ETHEREUM_URL=${3:?"!"}
QUORUM_URL=${4:?"!"}
pushd sender_app
  mvn clean package -DskipTests
  cf push sender -p target/*.jar --no-route --no-start
  cf map-route sender -n $HOSTNAME $DOMAIN 
  
  cf set-env sender SPRING_APPLICATION_JSON '{"config":{"quorumUrl":"'$QUORUM_URL'","ethereumUrl":"'$ETHEREUM_URL'","ethereumSenderAccountPassword":"password"}}'
  
  cf start sender
popd
