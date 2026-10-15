window.addEventListener("load", function () {
  window.ui = SwaggerUIBundle({
    url: "../v3/api-docs",
    dom_id: "#swagger-ui",
    deepLinking: true,
    validatorUrl: null,
    persistAuthorization: false,
    supportedSubmitMethods: ["get", "post"]
  });
});
