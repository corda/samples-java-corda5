import React, { useContext, useState } from "react";
import Stack from "@mui/material/Stack";
import Box from "@mui/material/Box";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Grid from "@mui/material/Grid";
import Alert from "@mui/material/Alert";

import FlowContext from "../Context/flow.context";
import FlowService from "../API/FlowService";
import FlowResult from "./FlowResult";

const IssueVoucher = ({ setLoading }) => {
  const { setVoucherId, setVoucherIssued } = useContext(FlowContext);
  const [error, setError] = useState(null);
  const [voucherDesc, setVoucherDesc] = useState("");
  const [holder, setHolder] = useState("");
  const [flowId, setFlowId] = useState("");
  const [clientId, setClientId] = useState("");
  const [flowStatus, setFlowStatus] = useState("");

  const onStartFlow = async () => {
    setLoading(true);
    try {
      const response = await FlowService.createAndIssueMarsVoucher(
        voucherDesc,
        holder
      );
      if (response.data) {
        setFlowId(response.data.flowId.uuid);
        setClientId(response.data.clientId);

        //check the flow outcome
        let flowOutcomeStatus = "RUNNING";
        let res = null;

        while (flowOutcomeStatus === "RUNNING") {
          res = await FlowService.getFlowOutcomeByFlowId(
            response.data.flowId.uuid
          );
          flowOutcomeStatus = res.data.status;
        }

        if (flowOutcomeStatus === "COMPLETED") {
          setVoucherIssued(true);
          const resultJson = JSON.parse(res.data.resultJson);
          const outputStates = resultJson.outputStates[0];
          setVoucherId(outputStates.split("linearId:")[1]);
        }

        setFlowStatus(flowOutcomeStatus);
      }
    } catch (err) {
      setError(err);
    }
    setLoading(false);
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Grid container spacing={4}>
        <Grid item md={6} sm={12}>
          <Stack spacing={2}>
            <TextField
              label="Voucher Description"
              variant="outlined"
              value={voucherDesc}
              onChange={(e) => {
                setVoucherDesc(e.target.value);
              }}
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
              disabled={voucherDesc.length === 0 || holder.length === 0}
            >
              Start Flow
            </Button>
            {error ? <Alert severity="error">{error.message}</Alert> : null}
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

export default IssueVoucher;
