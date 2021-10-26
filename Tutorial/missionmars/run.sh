#!/bin/sh

echo "--Step 1: Building projects."
./gradlew clean build

echo "--Step 2: Creating cpb file."
cordapp-builder create --cpk contracts/build/libs/corda5-missionmars-contracts-1.0-cordapp.cpk --cpk workflows/build/libs/corda5-missionmars-workflows-1.0-cordapp.cpk -o missionMars.cpb

echo "--Step 3: Configure the network"
corda-cli network config docker-compose missionmars-network

echo "--Step 4: Creating docker compose yaml file and starting docker containers.--"
corda-cli network deploy -n missionmars-network -f mission-mars.yaml | docker-compose -f - up -d

echo "--Listening to the docker processes.--"
corda-cli network wait -n missionmars-network

echo "--Step 5: Install the cpb file into the network.--"
corda-cli package install -n missionmars-network missionMars.cpb

echo "--Listening to the docker processes.--"
corda-cli network wait -n missionmars-network

echo "++Cordapp Setup Finished, Nodes Status: ++"
corda-cli network status -n missionmars-network