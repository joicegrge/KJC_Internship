package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.*;
import in.edu.kristujayanti.models.Event;
import in.edu.kristujayanti.services.EventService;
import in.edu.kristujayanti.utils.JwtUtil;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AdminEventHandler implements HttpHandler {
  private final EventService eventService;

  public AdminEventHandler(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    Headers headers = exchange.getRequestHeaders();
    String authHeader = headers.getFirst("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      sendResponse(exchange, 401, "Missing or invalid Authorization header");
      return;
    }

    String token = authHeader.substring("Bearer ".length());

    if (!JwtUtil.isAdmin(token)) {
      sendResponse(exchange, 403, "Access denied: Admin only");
      return;
    }

    URI requestURI = exchange.getRequestURI();
    String method = exchange.getRequestMethod();
    String path = requestURI.getPath();
    String[] pathParts = path.split("/");

    try {
      if (pathParts.length == 4) { // /api/admin/events
        switch (method) {
          case "GET":
            List<Event> events = eventService.getAllEvents();
            JSONArray array = new JSONArray();
            for (Event e : events) {
              array.put(e.toJSON());
            }
            sendJson(exchange, 200, array.toString());
            break;
          case "POST":
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(body);
            Event event = Event.fromJSON(json);
            eventService.addEvent(event);
            sendResponse(exchange, 200, "Event added");
            break;
          default:
            sendResponse(exchange, 405, "Method not allowed");
        }
      } else if (pathParts.length == 5) { // /api/admin/events/{id}
        String id = pathParts[4];

        switch (method) {
          case "GET":
            Event event = eventService.getEventById(id);
            if (event == null) {
              sendResponse(exchange, 404, "Event not found");
            } else {
              sendJson(exchange, 200, event.toJSON().toString());
            }
            break;
          case "PUT":
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(body);
            Event updated = Event.fromJSON(json);
            updated.setId(id); // Set ID manually from path
            eventService.updateEvent(updated);
            sendResponse(exchange, 200, "Event updated");
            break;
          case "DELETE":
            eventService.deleteEvent(id);
            sendResponse(exchange, 200, "Event deleted");
            break;
          default:
            sendResponse(exchange, 405, "Method not allowed");
        }
      } else {
        sendResponse(exchange, 404, "Invalid URL");
      }
    } catch (Exception e) {
      e.printStackTrace();
      sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
    }
  }

  private void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
    byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "text/plain");
    exchange.sendResponseHeaders(code, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }

  private void sendJson(HttpExchange exchange, int code, String json) throws IOException {
    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(code, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }
}
