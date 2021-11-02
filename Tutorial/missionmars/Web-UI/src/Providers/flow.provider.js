import React, { useState, useEffect } from "react";
import FlowContext from "../Context/flow.context";

const FlowProvider = ({ children }) => {
  const [networkChecked, setNetworkChecked] = useState(false);
  const [voucherIssued, setVoucherIssued] = useState(false);
  const [ticketCreated, setTicketCreated] = useState(false);
  const [voucherRedeemed, setVoucherRedeemed] = useState(false);
  const [voucherId, setVoucherId] = useState(null);

  const reset = () => {
    setNetworkChecked(false);
    setVoucherIssued(false);
    setTicketCreated(false);
    setVoucherRedeemed(false);
    setVoucherId(null);
  };

  useEffect(() => {
    if (voucherRedeemed) {
      reset();
    }
  }, [voucherRedeemed]);

  return (
    <FlowContext.Provider
      value={{
        networkChecked,
        voucherIssued,
        ticketCreated,
        voucherRedeemed,
        voucherId,
        setNetworkChecked,
        setVoucherIssued,
        setTicketCreated,
        setVoucherRedeemed,
        setVoucherId,
      }}
    >
      {children}
    </FlowContext.Provider>
  );
};

export default FlowProvider;
