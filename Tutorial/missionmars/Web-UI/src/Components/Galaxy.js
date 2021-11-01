import React, { useEffect, useRef, useContext } from "react";
import Box from "@mui/material/Box";
import { motion, useAnimation } from "framer-motion";
import earth from "../Assets/earth.png";
import mars from "../Assets/mars.png";
import rocket from "../Assets/Corda_Rocket.svg";
import FloatInfo from "./FloatInfo";
import FlowContext from "../Context/flow.context";

const Galaxy = () => {
  const { networkChecked, voucherIssued, ticketCreated, voucherRedeemed } =
    useContext(FlowContext);

  const controls = useAnimation();
  const earthRef = useRef(null);
  const marsRef = useRef(null);
  const rocketRef = useRef(null);

  const scaleRocket = async () => {
    await controls.start({
      scale: [1, 1.5, 1],
      transition: { duration: 1, type: "spring" },
    });
  };

  const rotateRocket = async () => {
    await controls.start({
      rotate: 65,
      scale: [1, 1.25, 1],
      transition: { duration: 1, type: "spring" },
    });
  };

  const launchRocket = async () => {
    const marsX =
      marsRef.current.getBoundingClientRect().x -
      rocketRef.current.getBoundingClientRect().x;
    const marsY =
      marsRef.current.getBoundingClientRect().y -
      rocketRef.current.getBoundingClientRect().y;

    await controls.start({
      x: [0, marsX],
      y: [0, marsY],
      scale: [1, 0.5],
    });
    await controls.start({
      rotate: 245,
    });
    await controls.start({
      x: 0,
      y: 0,
      scale: 1,
    });
    await controls.start({
      rotate: 0,
    });
  };

  useEffect(() => {
    if (networkChecked || voucherIssued) {
      scaleRocket();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [networkChecked, voucherIssued]);

  useEffect(() => {
    if (ticketCreated) {
      rotateRocket();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ticketCreated]);

  useEffect(() => {
    if (voucherRedeemed) {
      launchRocket();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [voucherRedeemed]);

  useEffect(() => {
    controls.start({ rotate: 360, opacity: [0, 1] });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Box
      className="universe"
      sx={{
        width: "100vw",
        height: "45vh",
        minHeight: 400,
        position: "relative",
      }}
    >
      <div className="stars"></div>

      <motion.img
        whileHover={{
          rotate: [0, 360],
          scale: 1.15,
          transition: { duration: 1, type: "spring" },
        }}
        ref={earthRef}
        className="earth"
        src={earth}
        alt="earth"
        animate={{
          rotate: 360,
          opacity: [0, 1],
        }}
        transition={{ type: "spring", duration: 2, stiffness: 10 }}
      />

      <motion.img
        whileHover={{
          rotate: [0, 360],
          scale: 1.15,
          transition: { duration: 1, type: "spring" },
        }}
        ref={marsRef}
        className="mars"
        src={mars}
        alt="mars"
        animate={{
          rotate: 360,
          opacity: [0, 1],
        }}
        transition={{ type: "spring", duration: 2 }}
      />

      <motion.img
        ref={rocketRef}
        className="rocket"
        src={rocket}
        alt="rocket"
        animate={controls}
        transition={{ type: "spring", duration: 2 }}
      />

      <FloatInfo />
    </Box>
  );
};

export default Galaxy;
