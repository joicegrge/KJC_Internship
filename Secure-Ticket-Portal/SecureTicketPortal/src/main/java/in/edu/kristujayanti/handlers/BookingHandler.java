package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import in.edu.kristujayanti.services.BookingService;
import in.edu.kristujayanti.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class BookingHandler implements HttpHandler {
  private final BookingService bookingService;

  public BookingHandler(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @Override
  public void handle(HttpExchange exchange) {
    try {
      String method = exchange.getRequestMethod();

      if (method.equalsIgnoreCase("POST")) {
        handleBookEvent(exchange);
      } else if (method.equalsIgnoreCase("GET")) {
        handleGetBookings(exchange);
      } else {
        exchange.sendResponseHeaders(405, -1); // Method Not Allowed
      }
    } catch (Exception e) {
      e.printStackTrace();
      try {
        sendJsonResponse(exchange, 500, new JSONObject().put("error", "Internal server error"));
      } catch (Exception ignored) {}
    }
  }

  private void handleBookEvent(HttpExchange exchange) throws Exception {
    // Your existing booking logic
    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      sendJsonResponse(exchange, 401, new JSONObject().put("error", "Unauthorized"));
      return;
    }

    String token = authHeader.substring("Bearer ".length());
    String userEmail;

    try {
      Claims claims = JwtUtil.parseToken(token);
      userEmail = claims.getSubject();
    } catch (Exception e) {
      sendJsonResponse(exchange, 401, new JSONObject().put("error", "Invalid token"));
      return;
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
    String requestBody = reader.lines().collect(Collectors.joining());
    JSONObject json = new JSONObject(requestBody);

    String eventId = json.optString("eventId");
    if (eventId == null || eventId.isEmpty()) {
      sendJsonResponse(exchange, 400, new JSONObject().put("error", "Missing eventId"));
      return;
    }

    try {
      Document result = bookingService.bookEvent(userEmail, eventId);
      sendJsonResponse(exchange, 200,
        new JSONObject()
          .put("message", "Booking successful")
          .put("token", result.getString("token")));
    } catch (RuntimeException rex) {
      sendJsonResponse(exchange, 400, new JSONObject().put("error", rex.getMessage()));
    }
  }

  private void handleGetBookings(HttpExchange exchange) throws Exception {
    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      sendJsonResponse(exchange, 401, new JSONObject().put("error", "Unauthorized"));
      return;
    }

    String token = authHeader.substring("Bearer ".length());
    String userEmail;

    try {
      Claims claims = JwtUtil.parseToken(token);
      userEmail = claims.getSubject();
    } catch (Exception e) {
      sendJsonResponse(exchange, 401, new JSONObject().put("error", "Invalid token"));
      return;
    }

    List<Document> bookings = bookingService.getUserBookings(userEmail);
    JSONArray jsonArray = new JSONArray(bookings);

    sendJsonResponse(exchange, 200,
      new JSONObject()
        .put("success", true)
        .put("bookings", jsonArray));
  }

  private void sendJsonResponse(HttpExchange exchange, int statusCode, JSONObject json) throws Exception {
    byte[] responseBytes = json.toString().getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(statusCode, responseBytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(responseBytes);
    }
  }
}
