package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import in.edu.kristujayanti.services.UserService;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class RefreshHandler implements HttpHandler {
  private final UserService userService;

  public RefreshHandler(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
      exchange.sendResponseHeaders(405, -1); // Method Not Allowed
      return;
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
    StringBuilder body = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      body.append(line);
    }

    JSONObject json = new JSONObject(body.toString());
    String refreshToken = json.getString("refreshToken");

    String newAccessToken = userService.refreshToken(refreshToken);

    JSONObject responseJson = new JSONObject();
    if (newAccessToken != null) {
      responseJson.put("accessToken", newAccessToken);
      exchange.sendResponseHeaders(200, responseJson.toString().getBytes().length);
    } else {
      responseJson.put("error", "Invalid or expired refresh token");
      exchange.sendResponseHeaders(401, responseJson.toString().getBytes().length);
    }

    exchange.getResponseHeaders().add("Content-Type", "application/json");
    OutputStream os = exchange.getResponseBody();
    os.write(responseJson.toString().getBytes());
    os.close();
  }
}
