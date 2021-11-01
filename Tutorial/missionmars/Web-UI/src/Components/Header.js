import React from "react";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import Logo from "./Logo";

const Header = () => {
  return (
    <AppBar
      position="static"
      color="default"
      elevation={0}
      sx={{
        background: "rgba(250,250,250,0.1)",
        color: "#fff",
        borderBottom: (theme) => `1px solid ${theme.palette.divider}`,
      }}
    >
      <Toolbar sx={{ flexWrap: "wrap" }}>
        <Stack direction="row" spacing={2}>
          <Logo />
          <Typography variant="h6" color="inherit" noWrap sx={{ flexGrow: 1 }}>
            Mission Mars
          </Typography>
        </Stack>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
