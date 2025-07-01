package in.edu.kristujayanti.handlers;

import com.mongodb.client.MongoCollection;
import com.sun.net.httpserver.*;
import in.edu.kristujayanti.services.EventService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import static com.mongodb.client.model.Filters.eq;


public class AdminBookingHandler implements HttpHandler {
  private final EventService eventService;

  public AdminBookingHandler(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
      exchange.sendResponseHeaders(405, -1);
      return;
    }

    List<Document> bookings = eventService.getAllBookings();
    MongoCollection<Document> eventCollection = eventService.getEventCollection();

    JSONArray arr = new JSONArray();
    for (Document b : bookings) {
    JSONObject obj = new JSONObject();
    obj.put("email", b.getString("email"));
    obj.put("token", b.getString("token"));
    obj.put("date", b.getString("bookedAt"));
    ObjectId eventId = b.getObjectId("eventId");
    Document event = eventCollection.find(eq("_id", eventId)).first();
    obj.put("eventTitle", event != null ? event.getString("title") : "Unknown");
      arr.put(obj);
    }

    byte[] resp = arr.toString().getBytes();
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, resp.length);
    OutputStream os = exchange.getResponseBody();
    os.write(resp);
    os.close();
  }
}
