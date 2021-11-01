import React, { useContext, useState, useEffect } from "react";
import Alert from "@mui/material/Alert";
import AlertTitle from "@mui/material/AlertTitle";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Stack from "@mui/material/Stack";
import FlowContext from "../Context/flow.context";

const FloatInfo = () => {
  const {
    voucherId,
    networkChecked,
    voucherIssued,
    ticketCreated,
    voucherRedeemed,
  } = useContext(FlowContext);

  const [x500Copied, setX500Copied] = useState(false);
  const [voucherIdCopied, setVoucherIdCopied] = useState(false);

  const copyToClipboard = (value) => {
    navigator.clipboard.writeText(value);
  };

  useEffect(() => {
    if (voucherRedeemed) {
      setX500Copied(false);
      setVoucherIdCopied(false);
    }
  }, [voucherRedeemed]);

  return (
    <React.Fragment>
      {networkChecked ? (
        <Alert
          icon={false}
          severity="error"
          sx={{ position: "absolute", left: "20%", top: "10%" }}
        >
          <AlertTitle>Peter's X500 </AlertTitle>
          <Stack direction="row" spacing={2} alignItems="center">
            <Typography>C=US, L=New York, O=Peter, OU=INC</Typography>

            <Button
              variant="outlined"
              size="small"
              onClick={() => {
                setX500Copied(true);
                setVoucherIdCopied(false);
                copyToClipboard("C=US, L=New York, O=Peter, OU=INC");
              }}
            >
              {x500Copied ? "copied" : "copy"}
            </Button>
          </Stack>
        </Alert>
      ) : null}
      {voucherIssued ? (
        <Alert
          icon={false}
          severity="success"
          sx={{ position: "absolute", left: "22%", top: "30%" }}
        >
          <AlertTitle>Voucher ID: </AlertTitle>
          <Stack direction="row" spacing={2} alignItems="center">
            <Typography>{voucherId}</Typography>

            <Button
              variant="outlined"
              size="small"
              onClick={() => {
                setX500Copied(false);
                setVoucherIdCopied(true);
                copyToClipboard(voucherId);
              }}
            >
              {voucherIdCopied ? "copied" : "copy"}
            </Button>
          </Stack>
        </Alert>
      ) : null}
      {ticketCreated ? (
        <Alert
          icon={false}
          severity="info"
          sx={{ position: "absolute", left: "25%", top: "50%" }}
        >
          Ticket Ready!
        </Alert>
      ) : null}
    </React.Fragment>
  );
};

export default FloatInfo;
