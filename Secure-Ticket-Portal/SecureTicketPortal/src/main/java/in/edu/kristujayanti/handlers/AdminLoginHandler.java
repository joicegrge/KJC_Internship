package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import in.edu.kristujayanti.utils.JwtUtil;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class AdminLoginHandler implements HttpHandler {

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
      exchange.sendResponseHeaders(405, -1); // Method Not Allowed
      return;
    }

    // Read JSON request body
    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }

    JSONObject requestBody = new JSONObject(sb.toString());
    String email = requestBody.optString("email", "");
    String password = requestBody.optString("password", "");

    // ✅ Hardcoded admin credentials
    if (!email.equals("admin@domain.com") || !password.equals("admin")) {
      sendJsonResponse(exchange, 401, new JSONObject().put("error", "Invalid admin credentials"));
      return;
    }

    // ✅ Generate JWT tokens with role = admin
    JSONObject tokens = new JSONObject();
    tokens.put("accessToken", JwtUtil.generateAccessToken(email, "admin"));
    tokens.put("refreshToken", JwtUtil.generateRefreshToken(email, "admin"));

    sendJsonResponse(exchange, 200, tokens);
  }

  private void sendJsonResponse(HttpExchange exchange, int statusCode, JSONObject body) throws IOException {
    byte[] responseBytes = body.toString().getBytes();
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(statusCode, responseBytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(responseBytes);
    }
  }
}
