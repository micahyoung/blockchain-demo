#!/bin/bash
set -o errexit
set -o pipefail
set -o nounset

pushd sender_app
  mvn clean package -DskipTests
  cf push sender -p target/*.jar --no-start
  
  MINER_URL=https://eth-node-1.cfapps.io
  cf set-env sender SPRING_APPLICATION_JSON '{"config":{"nodeUrl":"'$MINER_URL'","senderAccountPassword":"password"}}'
  
  cf start sender
popd
