package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.*;
import in.edu.kristujayanti.services.EventService;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    String method = exchange.getRequestMethod();

    if (method.equalsIgnoreCase("GET")) {
      List<Document> events = eventService.listEvents();
      JSONArray arr = new JSONArray();
      for (Document event : events) {
        JSONObject obj = new JSONObject(event);
        obj.put("id", event.getObjectId("_id").toString()); // Expose _id to frontend
        arr.put(obj);
      }
      byte[] resp = arr.toString().getBytes();
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, resp.length);
      exchange.getResponseBody().write(resp);
      exchange.getResponseBody().close();

    } else if (method.equalsIgnoreCase("DELETE")) {
      URI uri = exchange.getRequestURI();
      String query = uri.getQuery();
      if (query != null && query.contains("id=")) {
        String id = query.split("id=")[1];
        eventService.deleteEventById(id);
        exchange.sendResponseHeaders(200, -1);
      } else {
        exchange.sendResponseHeaders(400, -1);
      }

    } else if (method.equalsIgnoreCase("POST")) {
      InputStream is = exchange.getRequestBody();
      String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      JSONObject json = new JSONObject(body);

      String title = json.getString("title");
      String date = json.getString("date");
      int availableTokens = json.getInt("availableTokens");

      Document newEvent = new Document("title", title)
        .append("date", date)
        .append("availableTokens", availableTokens);

      eventService.getEventCollection().insertOne(newEvent);

      exchange.sendResponseHeaders(201, -1);
    } else {
      exchange.sendResponseHeaders(405, -1); // Method not allowed
    }
  }
}
