import React, { useState, useContext } from "react";
import Stack from "@mui/material/Stack";
import Box from "@mui/material/Box";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Grid from "@mui/material/Grid";

import FlowContext from "../Context/flow.context";
import FlowService from "../API/FlowService";
import FlowResult from "./FlowResult";

const CreateTicket = ({ setLoading }) => {
  const { setTicketCreated } = useContext(FlowContext);
  const [ticketDescription, setTicketDescription] = useState("");
  const [daysUntilLaunch, setdaysUntilLaunch] = useState("");
  const [flowId, setFlowId] = useState("");
  const [clientId, setClientId] = useState("");
  const [flowStatus, setFlowStatus] = useState("");

  const onStartFlow = async () => {
    setLoading(true);
    const response = await FlowService.createBoardingTicket(
      ticketDescription,
      daysUntilLaunch
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
        setTicketCreated(true);
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
              label="Ticket Description"
              variant="outlined"
              value={ticketDescription}
              onChange={(e) => setTicketDescription(e.target.value)}
            />
            <TextField
              type="number"
              label="Days Till Launch"
              variant="outlined"
              value={daysUntilLaunch}
              onChange={(e) => setdaysUntilLaunch(e.target.value)}
            />
            <Button
              variant="contained"
              onClick={onStartFlow}
              disabled={
                ticketDescription.length === 0 || daysUntilLaunch.length === 0
              }
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

export default CreateTicket;
