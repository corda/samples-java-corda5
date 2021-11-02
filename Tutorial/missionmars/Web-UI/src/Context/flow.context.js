import { createContext } from "react";

const INITIAL_VALUE = {
  networkChecked: false,
  setNetworkChecked: () => {},
  voucherIssued: false,
  setVoucherIssued: () => {},
  ticketCreated: false,
  setTicketCreated: () => {},
  voucherRedeemed: false,
  setVoucherRedeemed: () => {},
  voucherId: null,
  setVoucherId: () => {},
};

const FlowContext = createContext(INITIAL_VALUE);

export default FlowContext;
