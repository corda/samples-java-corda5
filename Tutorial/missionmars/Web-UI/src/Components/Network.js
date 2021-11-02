import React, { useContext, useState } from "react";
import Box from "@mui/material/Box";
import TextField from "@mui/material/Input";
import Button from "@mui/material/Button";
import Stack from "@mui/material/Stack";
import Grid from "@mui/material/Grid";
import Alert from "@mui/material/Alert";

import GroupMembers from "./GroupMembers";
import MembershipGroupService from "../API/MembershipGroupService";
import FlowContext from "../Context/flow.context";

const Network = () => {
  const { setNetworkChecked } = useContext(FlowContext);
  const [members, setMembers] = useState([]);
  const [error, setError] = useState(null);

  const checkMembers = async () => {
    try {
      const response = await MembershipGroupService.getAllMembers();
      if (response.data) {
        setMembers(response.data.map((member) => member.x500Name));
        setNetworkChecked(true);
      }
    } catch (err) {
      setError(err);
    }
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Grid container spacing={4}>
        <Grid item md={6} sm={12}>
          <Stack spacing={2}>
            <TextField
              fullWidth
              disabled
              label="api"
              variant="filled"
              defaultValue="GET https://localhost:{NODE_RPC_PORT}/api/v1/membershipgroup/getallmembers"
            />
            <Button
              variant="contained"
              onClick={() => {
                checkMembers();
              }}
            >
              Check
            </Button>
            {error ? <Alert severity="error">{error.message}</Alert> : null}
          </Stack>
        </Grid>

        {members.length ? (
          <Grid item md={6} sm={12}>
            <GroupMembers />
          </Grid>
        ) : null}
      </Grid>
    </Box>
  );
};

export default Network;
