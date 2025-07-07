package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import in.edu.kristujayanti.services.UserService;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ResetCompleteHandler implements HttpHandler {

  private final UserService userService;

  public ResetCompleteHandler(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
      exchange.sendResponseHeaders(405, -1); // Method Not Allowed
      return;
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
    StringBuilder requestBody = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      requestBody.append(line);
    }

    JSONObject requestJson = new JSONObject(requestBody.toString());
    String token = requestJson.getString("token");
    String newPassword = requestJson.getString("newPassword");

    String result = userService.completePasswordReset(token, newPassword);

    JSONObject responseJson = new JSONObject();
    responseJson.put("message", result);

    byte[] responseBytes = responseJson.toString().getBytes();
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, responseBytes.length);

    OutputStream os = exchange.getResponseBody();
    os.write(responseBytes);
    os.close();
  }
}
