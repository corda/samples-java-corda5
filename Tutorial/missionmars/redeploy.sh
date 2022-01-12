#!/bin/sh

echo "--Step 1: Building projects."
./gradlew clean build

echo "--Step 2: Creating cpb file."
cordapp-builder create --cpk contracts/build/libs/corda5-missionmars-contracts-1.0-SNAPSHOT-cordapp.cpk --cpk workflows/build/libs/corda5-missionmars-workflows-1.0-SNAPSHOT-cordapp.cpk -o missionMars.cpb

echo "--Step 3: Install the cpb file into the network.--"
corda-cli package install -n missionmars-network missionMars.cpb

echo "--Listening to the docker processes.--"
corda-cli network wait -n missionmars-network

echo "++Cordapp Setup Finished, Nodes Status: ++"
corda-cli network status -n missionmars-network