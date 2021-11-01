import React from "react";
import Typography from "@mui/material/Typography";
import Link from "@mui/material/Link";
import Container from "@mui/material/Container";

const Footer = () => {
  return (
    <Container maxWidth="md" component="footer">
      <Typography
        variant="body2"
        color="#fff"
        align="center"
        sx={{ marginTop: 3 }}
      >
        {"Copyright © "}
        <Link color="inherit" href="https://www.r3.com/">
          R3
        </Link>{" "}
        {new Date().getFullYear()}
        {"."}
      </Typography>
    </Container>
  );
};

export default Footer;
