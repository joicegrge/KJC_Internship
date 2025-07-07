package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import in.edu.kristujayanti.services.EventService;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class PublicEventHandler implements HttpHandler {
  private final EventService eventService;

  public PublicEventHandler(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
      exchange.sendResponseHeaders(405, -1); // Method Not Allowed
      return;
    }

    // Parse query params (e.g., ?page=1&size=5)
    URI requestURI = exchange.getRequestURI();
    Map<String, String> queryParams = queryToMap(requestURI.getQuery());
    int page = Integer.parseInt(queryParams.getOrDefault("page", "1"));
    int size = Integer.parseInt(queryParams.getOrDefault("size", "5"));

    List<Document> events = eventService.getUpcomingEvents(page, size);
    long total = eventService.getUpcomingEventsCount();

    JSONArray jsonEvents = new JSONArray();
    for (Document doc : events) {
      JSONObject obj = new JSONObject();
      obj.put("id", doc.getObjectId("_id").toHexString());
      obj.put("name", doc.getString("name"));
      obj.put("description", doc.getString("description"));
      obj.put("location", doc.getString("location"));
      obj.put("dateTime", doc.getString("dateTime"));
      obj.put("tokensAvailable", doc.getInteger("tokensAvailable"));
      obj.put("tokenLimit", doc.getInteger("tokenLimit"));
      obj.put("status", doc.getString("status"));
      jsonEvents.put(obj);
    }

    JSONObject response = new JSONObject();
    response.put("page", page);
    response.put("size", size);
    response.put("total", total);
    response.put("events", jsonEvents);

    byte[] responseBytes = response.toString().getBytes();
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, responseBytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(responseBytes);
    }
  }

  private Map<String, String> queryToMap(String query) {
    return java.util.Arrays.stream(query != null ? query.split("&") : new String[0])
      .map(param -> param.split("="))
      .filter(pair -> pair.length == 2)
      .collect(java.util.stream.Collectors.toMap(pair -> pair[0], pair -> pair[1]));
  }
}
