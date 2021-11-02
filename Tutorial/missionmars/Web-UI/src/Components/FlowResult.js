import React, { useState } from "react";
import Button from "@mui/material/Button";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemText from "@mui/material/ListItemText";

const FlowResult = ({ flowId, clientId, flowStatus }) => {
  const [copied, setCopied] = useState(false);

  const copyToClipboard = (value) => {
    navigator.clipboard.writeText(value);
  };

  if (flowId && clientId) {
    return (
      <List>
        <ListItem
          secondaryAction={
            <Button
              variant="outlined"
              size="small"
              onClick={() => {
                copyToClipboard(flowId);
                setCopied(true);
              }}
            >
              {copied ? "copied" : "copy"}
            </Button>
          }
        >
          <ListItemText primary={flowId} secondary="Flow ID" />
        </ListItem>
        <ListItem>
          <ListItemText primary={clientId} secondary="Client ID" />
        </ListItem>
        <ListItem>
          <ListItemText primary={flowStatus} secondary="Flow Status" />
        </ListItem>
      </List>
    );
  } else {
    return null;
  }
};

export default FlowResult;
