package in.edu.kristujayanti;

import com.mongodb.client.*;
import com.sun.net.httpserver.HttpServer;
import in.edu.kristujayanti.handlers.*;
import in.edu.kristujayanti.services.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
  public static void main(String[] args) throws IOException {
    MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    MongoDatabase database = mongoClient.getDatabase("eventSystem");

    UserService userService = new UserService(database);
    EventService eventService = new EventService(database);

    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

    // API routes
    server.createContext("/api/register", new RegisterHandler(userService));
    server.createContext("/api/login", new LoginHandler(userService));
    server.createContext("/api/events", new EventListHandler(eventService));
    server.createContext("/api/book", new BookTokenHandler(eventService));
    server.createContext("/api/admin/users", new AdminUserHandler(userService));
    server.createContext("/api/admin/events", new AdminEventHandler(eventService));
    server.createContext("/api/admin/bookings", new AdminBookingHandler(eventService));


    // Serve static files (HTML, CSS, JS) from public folder
    server.createContext("/", exchange -> {
      String uri = exchange.getRequestURI().getPath();
      if (uri.equals("/")) {
        uri = "/register_login.html"; // default landing page
      }

      Path filePath = Path.of("src/main/resources/public" + uri);
      if (Files.exists(filePath)) {
        String contentType = "text/html";
        if (uri.endsWith(".js")) contentType = "application/javascript";
        else if (uri.endsWith(".css")) contentType = "text/css";
        else if (uri.endsWith(".json")) contentType = "application/json";

        byte[] fileBytes = Files.readAllBytes(filePath);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, fileBytes.length);
        exchange.getResponseBody().write(fileBytes);
      } else {
        String msg = "404 - File not found: " + uri;
        exchange.sendResponseHeaders(404, msg.length());
        exchange.getResponseBody().write(msg.getBytes());
      }
      exchange.close();
    });

    server.setExecutor(null);
    server.start();

    System.out.println("✅ Server started at http://localhost:8080");
  }
}
