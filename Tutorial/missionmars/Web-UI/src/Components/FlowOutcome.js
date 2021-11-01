import React, { useState } from "react";
import Stack from "@mui/material/Stack";
import Box from "@mui/material/Box";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemText from "@mui/material/ListItemText";
import Grid from "@mui/material/Grid";

import FlowService from "../API/FlowService";

const FlowOutcome = () => {
  const [flowId, setFlowId] = useState("");
  const [flowOutcome, setFlowOutcome] = useState(null);

  const onStartFlow = async () => {
    const response = await FlowService.getFlowOutcomeByFlowId(flowId);
    const status = response.data.status;
    if (status === "COMPLETED") {
      const resultJson = JSON.parse(response.data.resultJson);
      const info = JSON.parse(resultJson.outputStates[0]);
      setFlowOutcome({
        status,
        ...info,
      });
    } else {
      const { message } = response.data.exceptionDigest;
      setFlowOutcome({ status, message });
    }
  };

  const renderFlowOutcomeResult = () => {
    if (flowOutcome) {
      return (
        <Grid item md={6} sm={12}>
          <List>
            {Object.keys(flowOutcome).map((key, index) => {
              return (
                <ListItem key={key}>
                  <ListItemText primary={flowOutcome[key]} secondary={key} />
                </ListItem>
              );
            })}
          </List>
        </Grid>
      );
    }
    return null;
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Grid container spacing={4}>
        <Grid item md={6} sm={12}>
          <Stack spacing={2}>
            <TextField
              label="Flow ID"
              variant="outlined"
              value={flowId}
              onChange={(e) => setFlowId(e.target.value)}
            />

            <Button
              variant="contained"
              onClick={onStartFlow}
              disabled={flowId.length === 0}
            >
              Check Flow Outcome
            </Button>
          </Stack>
        </Grid>
        {renderFlowOutcomeResult()}
      </Grid>
    </Box>
  );
};

export default FlowOutcome;
