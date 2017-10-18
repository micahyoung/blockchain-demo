#!/bin/bash
set -o errexit
set -o pipefail
set -o nounset

mvn clean package -DskipTests
cf push sender -p target/*.jar --no-start

cf allow-access sender miners --port 8545 --protocol tcp
MINER_IP=$(cf ssh miners -c 'echo $CF_INSTANCE_INTERNAL_IP') 
cf set-env sender SPRING_APPLICATION_JSON '{"config":{"nodeUrl":"http://'$MINER_IP':8545","senderAccountPassword":"password"}}'

cf start sender
