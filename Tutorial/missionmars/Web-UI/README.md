# Mission Mars Demo UI

A React App to demo the work of Mission Mars Cordapp that built on Corda 5: https://github.com/corda/samples-kotlin-corda5/tree/main/tutorial/missionmars

More info about the Mission Mars project can be found here:
https://docs.r3.com/en/platform/corda/5.0-dev-preview-1/tutorials/building-cordapp/c5-basic-cordapp-intro.html

# Required Prerequisites:

nodejs (npm)

Mission Mars back-end:
https://github.com/corda/samples-kotlin-corda5/tree/main/tutorial/missionmars

## Corda 5 Virutal Node Porting

Once back-end code is up running, check the RPC port for partyA node:

### `corda-cli network status -n missionmars-network`

The default RPC port for partyA node will be 12112. 
However, if different, please change the default `PARTY_A_PORT` in `setupProxy.js` accordingly.

## Available Scripts

In the project directory, you can run:

### `npm install`

To install all necessary npm modules to run this app.

### `npm start`

Runs the app in the development mode.\
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

The page will reload if you make edits.\
You will also see any lint errors in the console.
