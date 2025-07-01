package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.*;
import in.edu.kristujayanti.services.EventService;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class EventListHandler implements HttpHandler {
  private final EventService eventService;

  public EventListHandler(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
      exchange.sendResponseHeaders(405, -1);
      return;
    }

    List<Document> events = eventService.listEvents();
    JSONArray jsonArray = new JSONArray();

    for (Document doc : events) {
      JSONObject event = new JSONObject();
      event.put("id", doc.getObjectId("_id").toHexString());
      event.put("title", doc.getString("title"));
      event.put("date", doc.getString("date"));
      event.put("availableTokens", doc.getInteger("availableTokens"));
      jsonArray.put(event);
    }

    byte[] resp = jsonArray.toString().getBytes();
    exchange.sendResponseHeaders(200, resp.length);
    OutputStream os = exchange.getResponseBody();
    os.write(resp);
    os.close();
  }
}
