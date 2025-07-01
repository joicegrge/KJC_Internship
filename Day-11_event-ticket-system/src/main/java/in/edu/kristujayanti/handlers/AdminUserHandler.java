// AdminUserHandler.java
package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.*;
import in.edu.kristujayanti.services.UserService;
import org.bson.Document;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

public class AdminUserHandler implements HttpHandler {
  private final UserService userService;

  public AdminUserHandler(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();

    if (method.equalsIgnoreCase("GET")) {
      List<Document> users = userService.getAllUsers();
      JSONArray arr = new JSONArray();
      for (Document user : users) {
        arr.put(user);
      }
      byte[] resp = arr.toString().getBytes();
      exchange.sendResponseHeaders(200, resp.length);
      OutputStream os = exchange.getResponseBody();
      os.write(resp);
      os.close();

    } else if (method.equalsIgnoreCase("DELETE")) {
      URI uri = exchange.getRequestURI();
      String query = uri.getQuery();
      if (query != null && query.contains("email=")) {
        String email = query.split("email=")[1];
        userService.deleteUserByEmail(email);
        exchange.sendResponseHeaders(200, -1);
      } else {
        exchange.sendResponseHeaders(400, -1);
      }
    } else {
      exchange.sendResponseHeaders(405, -1);
    }
  }
}
