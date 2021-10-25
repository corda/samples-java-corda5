# Corda5 Land Registry to Demo Simple Issue/ Move

## Environment Requirements: 
1. Download and install Java 11
2. Download and install Docker
3. Download and install `cordapp-builder` (Latest version: 5.0.0-DevPreview)
4. Download and install `corda-cli` (Latest version: 1.0.0-DevPreview)

Step 2 - 4 To find detailed instructions visit the docs site [here](https://docs.r3.com/en/platform/corda/5.0-dev-preview-1/getting-started/overview.html)

## App Functionalities 
This sample app is a demonstration of how to do a simple Issue and Move transaction in Corda 5, with the help of a 
land registry use case. It has two simple functionality:
 - Issuer can issue land title to particular party.
 - The current owner of the land can transfer the land title to another party.

## How to run the sample

With Corda 5 test development experience has been re-engineered, it utilizes docker for test deployment. 
We need to follow a couple of steps to test deploy the sample app. 
```
#1 Build the projects.
./gradlew clean build

#2 Create the cpb file from the compiled cpk files in both contracts and workflows.
cordapp-builder create --cpk workflows/build/libs/workflows-1.0.0-DevPreview-cordapp.cpk --cpk contracts/build/libs/contracts-1.0.0-DevPreview-cordapp.cpk -o result.cpb 

#3 Configure the mock network
corda-cli network config docker-compose demo-network

#4 Start docker containers.
corda-cli network deploy -n demo-network -f node-config.yaml | docker-compose -f - up
   
This will download corda/corda-dev image from the docker hub and will take roughly a mintute to complete so wait for the Corda logo to populate. 
    
#5 Install the cpb file into the network.
corda-cli package install -n demo-network result.cpb
```
If all the command are run properly, our sample app should be successfully deployed and running on the test network at this point. 

## Check network status
We can always look at the status of the test network using the command: 
```
corda-cli network status -n demo-network
```

## Interact with the app 
Open a browser and go to `https://localhost:<port>/api/v1/swagger`

For this sample app, the ports are: 
* PartyA's node: 12112
* PartyB's node: 12116
* PartyB's node: 12120

**NOTE: This information is in the status printout of the network. Use the status command that documented above.**

The url will bring us to the swagger API interface, it is a set of HTTP API which we can use out of the box. 
In order to continue interacting with our app, we would need to log in. 

Depending on the node that we chose, we would need to log into the node using the correct credentials. 
For this sample app, the credentials are: 
* PartyA - Login: angelenos, password: password
* PartyB - Login: londoner, password: password
* PartyC - Login: mumbaikar, password: password

**NOTE: This information can be found in the node-config.yaml file.** 

### Run Issue Land Title Flow

We can run any flow registered on the node using the `/flowstarter/startflow` API. This request carries three pieces of information:
1. The clientID of this call
2. The flow we are triggering
3. The flow parameters that we are providing.

To run the `IssueLandTitleFlow`, we can log in to PartyA and run the `/flowstarter/startflow` API using the below request body.
```
{
  "rpcStartFlowRequest": {
    "clientId": "Client1",
    "flowName": "net.corda.c5.sample.landregistry.flows.IssueLandTitleFlow",
    "parameters": {
      "parametersInJson": "{\"plotNumber\":\"PT01\",\"dimensions\":\"60x60\",\"area\":\"1800sqft\",\"owner\":\"C=GB, L=London, O=PartyB, OU=INC\"}"
    }
  }
}
```

After the call, you should expect a 200 success call code, and a response body as such: 
```
{
  "flowId": {
    "uuid": "81e1415e-be7c-4038-8d06-8e76bdfd8bc7"
  },
  "clientId": "Client1"
}
```
**NOTE: This does not mean the transaction is passed through, it means the flow is successfully executed, but the success of the transaction is not guaranteed.** 

We would need either go to `/flowstarter/flowoutcomeforclientid/{clientid}` or `/flowstarter/flowoutcome/{flowid}` to see the result of the flow. 
In this case, we will use the `flowId` to query the flow result: 

Enter the flowId of our previous flow call: `81e1415e-be7c-4038-8d06-8e76bdfd8bc7`
We will getting the following response: 
```
{
  "status": "COMPLETED",
  "resultJson": "{ \n \"txId\" : \"SHA-256:CCF78722E7500302698192FED6CECE44F03D1AE95EE986F9E0F00AFB3D694537\",\n \"outputStates\" : [\"plotNumber: PT01 location: null dimensions: 60x60 area: 1800sqft owner: OU=INC, O=PartyB, L=London, C=GB issuer: OU=LLC, O=PartyA, L=Los Angeles, C=US\"], \n \"signatures\": [\"w9gaee2r09vmq0yo1Be3bFAT6LxiGenlVUizJtpura5LI83uiTKWPBZ3sDW+TzKrVMP2p9BoqUUVdbKnQXPKAw==\"]\n}",
  "exceptionDigest": null
}
```
The completed status of the flow means the success of the flow and its carried transaction. 

## Querying the state
Corda 5 introduces a new Query API that is exposed as part of the HTTP-RPC Persistence API. It allows us to invoke 
named-queries via HTTP requests and receive results marshalled to JSON. To learn more refer to our [documentation](https://docs.r3.com/en/platform/corda/5.0-dev-preview-1/cordapps/persistence/http-named-query-api.html)

To perform a query we can use the `/persistence/query` API using the below request body:

```
{
  "request": {
    "namedParameters": {
      "stateStatus": {
        "parametersInJson": "\"UNCONSUMED\""
      },
      "contractStateClassName": {
        "parametersInJson": "\"net.corda.c5.sample.landregistry.states.LandTitleState\""
      }
    },
    "queryName": "VaultState.findByStateStatusAndContractStateClassName",
    "postProcessorName": "Corda.IdentityContractStatePostProcessor"
  },
  "context": {
    "awaitForResultTimeout": "PT15M",
    "currentPosition": -1,
    "maxCount": 10
  }
}
```

It should produce a response as below showing the `LandTitleState` that is recorded on the ledger:

```
{
  "positionedValues": [
    {
      "value": {
        "json": "plotNumber: PT01  dimensions: 60x60 area: 1800sqft owner: OU=INC, O=PartyB, L=London, C=GB issuer: OU=LLC, O=PartyA, L=Los Angeles, C=US"
      },
      "position": 0
    }
  ],
  "remainingElementsCountEstimate": null
}
```

We could also verify the same at PartyB, by logging into PartyB's Swagger UI and running the `/persistence/query` API.

## Run Transfer Land Title Flow

Once the land title has been issued to PartyB, he can transfer it to PartyC, using the `TransferLandTitleFlow` flow. 
It should be pretty straightforward, login to PartyB's Swagger UI and run the flow using the `/flowstarter/startflow` API.

The request body needed would be:
```
{
  "rpcStartFlowRequest": {
    "clientId": "c002",
    "flowName": "net.corda.c5.sample.landregistry.flows.TransferLandTitleFlow",
    "parameters": {
      "parametersInJson": "{\"plotNumber\":\"PT01\",\"owner\":\"C=IN, L=Mumbai, O=PartyC, OU=INC\"}"
    }
  }
}
```

If run successfully this should transfer the land title from PartyB to PartyC, which we can again verify using the `/persistence/query` API.

## Shutting down the test network
Finally, we can shut down the test network by using the command:
```
corda-cli network terminate -n demo-network -ry
```
