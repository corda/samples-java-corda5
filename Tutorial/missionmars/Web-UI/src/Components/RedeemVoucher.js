import React, { useState, useContext } from "react";
import Stack from "@mui/material/Stack";
import Box from "@mui/material/Box";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Grid from "@mui/material/Grid";

import FlowService from "../API/FlowService";
import FlowContext from "../Context/flow.context";
import FlowResult from "./FlowResult";

const RedeemVoucher = ({ setLoading }) => {
  const { setVoucherRedeemed } = useContext(FlowContext);
  const [voucherId, setVoucherId] = useState("");
  const [holder, setHolder] = useState("");
  const [flowId, setFlowId] = useState("");
  const [clientId, setClientId] = useState("");
  const [flowStatus, setFlowStatus] = useState("");

  const onStartFlow = async () => {
    setLoading(true);
    const response = await FlowService.redeemBoardingTicketWithVoucher(
      voucherId,
      holder
    );
    if (response.data) {
      setFlowId(response.data.flowId.uuid);
      setClientId(response.data.clientId);

      let flowOutcomeStatus = "RUNNING";
      let res = null;
      while (flowOutcomeStatus === "RUNNING") {
        res = await FlowService.getFlowOutcomeByFlowId(
          response.data.flowId.uuid
        );
        flowOutcomeStatus = res.data.status;
      }
      if (flowOutcomeStatus === "COMPLETED") {
        setVoucherRedeemed(true);
      }
      setFlowStatus(flowOutcomeStatus);
    }
    setLoading(false);
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Grid container spacing={4}>
        <Grid item md={6} sm={12}>
          <Stack spacing={2}>
            <TextField
              label="Voucher ID"
              variant="outlined"
              value={voucherId}
              onChange={(e) => setVoucherId(e.target.value)}
            />
            <TextField
              label="Voucher Holder's X500"
              variant="outlined"
              value={holder}
              onChange={(e) => setHolder(e.target.value)}
            />
            <Button
              variant="contained"
              onClick={onStartFlow}
              disabled={voucherId.length === 0 || holder.length === 0}
            >
              Start Flow
            </Button>
          </Stack>
        </Grid>

        <Grid item md={6} sm={12}>
          <FlowResult
            flowId={flowId}
            clientId={clientId}
            flowStatus={flowStatus}
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default RedeemVoucher;
