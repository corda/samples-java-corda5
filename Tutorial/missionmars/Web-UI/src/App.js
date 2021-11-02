import React from "react";
import "./App.scss";
import Box from "@mui/material/Box";
import Header from "./Components/Header";
import Footer from "./Components/Footer";
import Galaxy from "./Components/Galaxy";
import Flow from "./Components/Flow";

const App = () => {
  return (
    <Box className="app-background" sx={{ minHeight: "100vh" }}>
      <Box sx={{ minHeight: "95vh" }}>
        <Header />
        <Galaxy />
        <Flow />
      </Box>
      <Box>
        <Footer />
      </Box>
    </Box>
  );
};

export default App;
