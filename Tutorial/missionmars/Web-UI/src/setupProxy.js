const { createProxyMiddleware } = require("http-proxy-middleware");

const PARTY_A_PORT = 12112;

module.exports = function (app) {
  app.use(
    "/partya",
    createProxyMiddleware({
      target: `https://localhost:${PARTY_A_PORT}`,
      pathRewrite: { "^/partya/api": "/api" },
      changeOrigin: true,
      secure: false,
    })
  );
};
