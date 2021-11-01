import axios from "./axiosInstance";

const getRgisteredFlows = async () => {
  try {
    const response = await axios.get("flowstarter/registeredflows");
    return response;
  } catch (error) {
    throw error;
  }
};

const getFlowOutcomeByFlowId = async (flowId) => {
  try {
    const response = await axios.get(`flowstarter/flowoutcome/${flowId}`);
    return response;
  } catch (error) {
    throw error;
  }
};

const getFlowOutcomeByClientId = async (clientId) => {
  try {
    const response = await axios.get(
      `flowstarter/flowoutcomeforclientid/${clientId}`
    );
    return response;
  } catch (error) {
    throw error;
  }
};

const createAndIssueMarsVoucher = async (voucherDesc, holder) => {
  const launchPadNumber = `launchpad-${Math.floor(Math.random() * 1000 + 1)}`;
  try {
    const response = await axios.post(`flowstarter/startflow`, {
      rpcStartFlowRequest: {
        clientId: launchPadNumber,
        flowName:
          "net.corda.missionMars.flows.CreateAndIssueMarsVoucher$CreateAndIssueMarsVoucherInitiator",
        parameters: {
          parametersInJson: `{"voucherDesc": "${voucherDesc}", "holder": "${holder}"}`,
        },
      },
    });
    return response;
  } catch (error) {
    throw error;
  }
};

const createBoardingTicket = async (ticketDesc, daysUntilLaunch) => {
  const launchPadNumber = `launchpad-${Math.floor(Math.random() * 1000 + 1)}`;
  try {
    const response = await axios.post(`flowstarter/startflow`, {
      rpcStartFlowRequest: {
        clientId: launchPadNumber,
        flowName: "net.corda.missionMars.flows.CreateBoardingTicket$CreateBoardingTicketInitiator",
        parameters: {
          parametersInJson: `{"ticketDescription": "${ticketDesc}", "daysUntilLaunch": "${daysUntilLaunch}"}`,
        },
      },
    });
    return response;
  } catch (error) {
    throw error;
  }
};

const redeemBoardingTicketWithVoucher = async (voucherID, holder) => {
  const launchPadNumber = `launchpad-${Math.floor(Math.random() * 1000 + 1)}`;
  try {
    const response = await axios.post(`flowstarter/startflow`, {
      rpcStartFlowRequest: {
        clientId: launchPadNumber,
        flowName:
          "net.corda.missionMars.flows.RedeemBoardingTicketWithVoucher$RedeemBoardingTicketWithVoucherInitiator",
        parameters: {
          parametersInJson: `{"voucherID": "${voucherID}", "holder": "${holder}"}`,
        },
      },
    });
    return response;
  } catch (error) {
    throw error;
  }
};

const FlowService = {
  getRgisteredFlows,
  getFlowOutcomeByFlowId,
  getFlowOutcomeByClientId,
  createAndIssueMarsVoucher,
  createBoardingTicket,
  redeemBoardingTicketWithVoucher,
};

export default FlowService;
