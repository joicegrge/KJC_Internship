package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.*;
import in.edu.kristujayanti.services.UserService;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LoginHandler implements HttpHandler {
  private final UserService userService;

  public LoginHandler(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
      exchange.sendResponseHeaders(405, -1);
      return;
    }

    InputStream is = exchange.getRequestBody();
    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
    JSONObject json = new JSONObject(body);

    String email = json.getString("email");
    String password = json.getString("password");

    boolean isValid = userService.login(email, password);

    JSONObject response = new JSONObject();
    response.put("success", isValid);

    // Hardcoded admin login
    if (email.equals("admin@domain.com") && password.equals("admin123")) {
      response.put("success", true);
    } else if (userService.login(email, password)) {
      response.put("success", true);
    } else {
      response.put("success", false);
    }

    byte[] respBytes = response.toString().getBytes();
    exchange.sendResponseHeaders(isValid ? 200 : 401, respBytes.length);
    OutputStream os = exchange.getResponseBody();
    os.write(respBytes);
    os.close();
  }
}
