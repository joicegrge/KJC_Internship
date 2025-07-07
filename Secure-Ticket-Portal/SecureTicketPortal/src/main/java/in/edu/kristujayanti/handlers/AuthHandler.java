package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import in.edu.kristujayanti.services.UserService;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthHandler implements HttpHandler {
  private final UserService userService;

  public AuthHandler(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
        sendErrorResponse(exchange, 405, "Method Not Allowed");
        return;
      }

      // Read request body
      StringBuilder body = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          body.append(line);
        }
      }

      // Parse JSON
      JSONObject json = new JSONObject(body.toString());
      String email = json.getString("email").trim();
      String password = json.getString("password");

      // Authenticate
      String result = userService.authenticateUser(email, password);

      // Prepare response
      JSONObject responseJson = new JSONObject();
      if (result != null) {
        responseJson.put("tokens", new JSONObject(result));
        sendResponse(exchange, 200, responseJson.toString());
      } else {
        responseJson.put("error", "Invalid credentials");
        sendResponse(exchange, 401, responseJson.toString());
      }
    } catch (Exception e) {
      JSONObject error = new JSONObject().put("error", "Internal server error");
      sendResponse(exchange, 500, error.toString());
    }
  }

  private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
    byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    exchange.sendResponseHeaders(statusCode, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }

  private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
    JSONObject response = new JSONObject().put("error", message);
    sendResponse(exchange, statusCode, response.toString());
  }
}
