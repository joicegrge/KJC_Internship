package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.*;
import in.edu.kristujayanti.services.*;
import in.edu.kristujayanti.utils.PasswordGenerator;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RegisterHandler implements HttpHandler {
  private final UserService userService;

  public RegisterHandler(UserService userService) {
    this.userService = userService;
  }

  public void handle(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
      exchange.sendResponseHeaders(405, -1);
      return;
    }

    InputStream is = exchange.getRequestBody();
    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
    JSONObject json = new JSONObject(body);

    String name = json.getString("name");
    String email = json.getString("email");

    if (userService.userExists(email)) {
      exchange.sendResponseHeaders(409, -1); return;
    }

    String password = PasswordGenerator.generate(8);
    userService.registerUser(name, email, password);
    EmailService.send(email, "Your Event Token Password", "Your password: " + password);

    String res = new JSONObject().put("status", "registered").toString();
    exchange.sendResponseHeaders(200, res.length());
    OutputStream os = exchange.getResponseBody();
    os.write(res.getBytes());
    os.close();
  }
}
