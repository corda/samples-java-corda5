import React from "react";
import Container from "@mui/material/Container";
import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Tab from "@mui/material/Tab";
import TabContext from "@mui/lab/TabContext";
import TabList from "@mui/lab/TabList";
import TabPanel from "@mui/lab/TabPanel";
import Backdrop from "@mui/material/Backdrop";
import CircularProgress from "@mui/material/CircularProgress";

import Network from "./Network";
import IssueVoucher from "./IssueVoucher";
import CreateTicket from "./CreateTicket";
import RedeemVoucher from "./RedeemVoucher";
import FlowOutcome from "./FlowOutcome";

const Flow = () => {
  const [loading, setLoading] = React.useState(false);
  const handleClose = () => {
    setLoading(false);
  };
  const [value, setValue] = React.useState("network");
  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  return (
    <Container maxWidth="lg" component="main">
      <Box>
        <Backdrop
          sx={{ color: "#fff", zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={loading}
          onClick={handleClose}
        >
          <CircularProgress color="inherit" />
        </Backdrop>
        <Card variant="outlined" sx={{ background: "rgba(250,250,250,0.6)" }}>
          <CardContent>
            <Box>
              <TabContext value={value}>
                <Box sx={{ borderBottom: 1, borderColor: "divider" }}>
                  <TabList onChange={handleChange} aria-label="Flows">
                    <Tab label="Corda Network" value="network" />
                    <Tab label="Issue Voucher" value="issueVoucher" />
                    <Tab label="Create Ticket" value="createTicket" />
                    <Tab label="Redeem Voucher" value="redeemVoucher" />
                    <Tab label="Flow Outcome" value="checkFlowOutcome" />
                  </TabList>
                </Box>
                <TabPanel value="network">
                  <Network />
                </TabPanel>
                <TabPanel value="issueVoucher">
                  <IssueVoucher setLoading={setLoading} />
                </TabPanel>
                <TabPanel value="createTicket">
                  <CreateTicket setLoading={setLoading} />
                </TabPanel>
                <TabPanel value="redeemVoucher">
                  <RedeemVoucher setLoading={setLoading} />
                </TabPanel>
                <TabPanel value="checkFlowOutcome">
                  <FlowOutcome />
                </TabPanel>
              </TabContext>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Container>
  );
};

export default Flow;
