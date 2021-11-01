import axios from "axios";

axios.interceptors.request.use(async (config) => {
  let username = "angelenos";
  let password = "password";

  config.baseURL = "partya/api/v1";

  config.headers = {
    accept: "application/json",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET,PUT,POST,DELETE,PATCH,OPTIONS",
  };

  config.auth = {
    username: username,
    password: password,
  };

  return config;
});

export default axios;
