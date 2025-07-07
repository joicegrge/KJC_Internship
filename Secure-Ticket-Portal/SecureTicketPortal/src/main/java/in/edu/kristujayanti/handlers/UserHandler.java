package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import in.edu.kristujayanti.services.UserService;
import in.edu.kristujayanti.utils.JwtUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class UserHandler implements HttpHandler {
  private final UserService userService;

  public UserHandler(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
      exchange.sendResponseHeaders(405, -1); // Method Not Allowed
      return;
    }

    // Get Authorization header
    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      exchange.sendResponseHeaders(401, -1); // Unauthorized
      return;
    }



    String token = authHeader.substring(7);
    try {
      String email = JwtUtil.parseToken(token).getSubject();

      JSONObject response = new JSONObject();
      response.put("email", email);

      byte[] responseBytes = response.toString().getBytes();
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, responseBytes.length);

      OutputStream os = exchange.getResponseBody();
      os.write(responseBytes);
      os.close();
    } catch (Exception e) {
      exchange.sendResponseHeaders(401, -1); // Unauthorized
    }
  }
}
