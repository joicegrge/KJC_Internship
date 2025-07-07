package in.edu.kristujayanti;

import com.sun.net.httpserver.HttpServer;
import in.edu.kristujayanti.handlers.*;
import in.edu.kristujayanti.services.EventService;
import in.edu.kristujayanti.services.UserService;
import in.edu.kristujayanti.services.BookingService;
import in.edu.kristujayanti.handlers.BookingHandler;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
  public static void main(String[] args) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    UserService userService = new UserService();
    EventService eventService = new EventService();
    BookingService bookingService = new BookingService();



    // Register API handlers
    server.createContext("/api/auth", new AuthHandler(userService));
    server.createContext("/api/refresh", new RefreshHandler(userService));
    server.createContext("/api/logout", new LogoutHandler(userService));
    server.createContext("/api/user", new UserHandler(userService));
    server.createContext("/api/register", new RegisterHandler(userService));
    server.createContext("/api/reset-request", new ResetRequestHandler(userService));
    server.createContext("/api/reset-complete", new ResetCompleteHandler(userService));
    server.createContext("/api/events", new PublicEventHandler(eventService));
    server.createContext("/api/book", new BookingHandler(bookingService));
    server.createContext("/api/admin/events", new AdminEventHandler(eventService));
    server.createContext("/api/admin/login", new AdminLoginHandler());
    server.createContext("/api/bookings", new BookingHandler(bookingService));





    // Serve static files
    server.createContext("/", exchange -> {
      try {
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
          path = "/index.html";
        } else if (path.equals("/dashboard") || path.equals("/dashboard.html")) {
          path = "/dashboard.html";
        } else if (path.equals("/admindashboard") || path.equals("/admindashboard.html")) {
          path = "/admindashboard.html"; // ✅ Add this line
        } else if (path.equals("/reset-password")) {
          path = "/reset-complete.html";
        }

        // Load static file
        Path filePath = Paths.get(System.getProperty("user.dir"),
          "src", "main", "resources", "static", path.substring(1));

        if (!Files.exists(filePath)) {
          exchange.sendResponseHeaders(404, -1);
          return;
        }

        byte[] bytes = Files.readAllBytes(filePath);

        // Set content type
        String contentType = "text/html";
        if (path.endsWith(".css")) {
          contentType = "text/css";
        } else if (path.endsWith(".js")) {
          contentType = "text/javascript";
        }

        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
          os.write(bytes);
        }
      } catch (Exception e) {
        exchange.sendResponseHeaders(404, -1);
      }
    });


    server.setExecutor(null);
    server.start();
    System.out.println("Server started at http://localhost:8080");
  }
}
