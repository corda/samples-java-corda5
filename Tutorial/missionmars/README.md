# Corda5 Tutorial Cordapp: Mission Mars

Please refer to the documentation for a detailed walk through of developing and running this App [here](https://docs.r3.com/en/platform/corda/5.0-dev-preview-1/tutorials/building-cordapp/c5-basic-cordapp-intro.html)

Flow #1 input:
```json
{
  "rpcStartFlowRequest": {
    "clientId": "launchpad-1", 
    "flowName": "net.corda.missionMars.flows.CreateAndIssueMarsVoucher$CreateAndIssueMarsVoucherInitiator", 
    "parameters": { 
      "parametersInJson": "{\"voucherDesc\": \"Space Shuttle 323\", \"holder\": \"C=US, L=New York, O=Peter, OU=INC\"}" 
    } 
  } 
}
```

Flow #2 input:
```json
{
  "rpcStartFlowRequest": {
    "clientId": "launchpad-2", 
    "flowName": "net.corda.missionMars.flows.CreateBoardingTicket$CreateBoardingTicketInitiator", 
    "parameters": {
      "parametersInJson": "{\"ticketDescription\": \"Space Shuttle 323 - Seat 16B\", \"launchDate\": \"2023-11-02\"}"
    } 
  } 
}
```
01380f76-e2d8-4f3c-a102-cd41fa37e7e0
[Optional]: If you would like gift the voucher to a different party, run this in PartyB's API interface
```json
{
  "rpcStartFlowRequest": {
    "clientId": "launchpad-3", 
    "flowName": "net.corda.missionMars.flows.GiftVoucherToFriend$GiftVoucherToFriendInitiator", 
    "parameters": {
      "parametersInJson": "{\"voucherID\": \"01380f76-e2d8-4f3c-a102-cd41fa37e7e0\", \"holder\": \"C=US, L=San Diego, O=Friend, OU=LLC\"}"
    } 
  } 
}
```
Flow #3 input: (The voucherID needs to be retrieved from flow #2's output. Use the /flowstarter/flowoutcomeforclientid/{clientid} method, and input [launchpad-3])
```json
{
  "rpcStartFlowRequest": {
    "clientId": "launchpad-4", 
    "flowName": "net.corda.missionMars.flows.RedeemBoardingTicketWithVoucher$RedeemBoardingTicketWithVoucherInitiator", 
    "parameters": { 
      "parametersInJson": "{\"voucherID\": \"87cd2421-9182-42fc-8893-3781be1cd720\", \"holder\": \"C=US, L=San Diego, O=Friend, OU=LLC\"}" 
    } 
  } 
}
```

How to shut down the app
```
corda-cli network terminate -n missionmars-network -ry
```
