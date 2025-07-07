package in.edu.kristujayanti.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import in.edu.kristujayanti.db.MongoConnection;
import in.edu.kristujayanti.models.Event;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class EventService {
  private final MongoCollection<Document> eventCollection;

  public EventService() {
    MongoDatabase database = MongoConnection.getDatabase();
    eventCollection = database.getCollection("events");
  }

  // ✅ Add new event
  public void addEvent(Event event) {
    eventCollection.insertOne(event.toDocument());
  }

  // ✅ Get all events as Event objects
  public List<Event> getAllEvents() {
    List<Event> events = new ArrayList<>();
    for (Document doc : eventCollection.find()) {
      events.add(Event.fromDocument(doc));
    }
    return events;
  }

  // ✅ Get single event by ID
  public Event getEventById(String id) {
    Document doc = eventCollection.find(eq("_id", new ObjectId(id))).first();
    return doc != null ? Event.fromDocument(doc) : null;
  }

  // ✅ Update event by ID
  public void updateEvent(Event updated) {
    if (updated.getId() == null || updated.getId().isEmpty()) return;

    Document updateDoc = new Document("$set", new Document()
      .append("name", updated.getName())
      .append("description", updated.getDescription())
      .append("location", updated.getLocation())
      .append("dateTime", updated.getDateTime())
      .append("tokenLimit", updated.getTokenLimit())
      .append("tokensAvailable", updated.getTokensAvailable())
      .append("status", updated.getStatus()));

    eventCollection.updateOne(eq("_id", new ObjectId(updated.getId())), updateDoc);
  }

  // ✅ Delete event by ID
  public void deleteEvent(String id) {
    eventCollection.deleteOne(eq("_id", new ObjectId(id)));
  }

  // ✅ Public listing: Upcoming Events (pagination)
  public List<Document> getUpcomingEvents(int page, int size) {
    int skip = (page - 1) * size;
    return eventCollection.find(eq("status", "upcoming"))
      .skip(skip)
      .limit(size)
      .into(new ArrayList<>());
  }

  // ✅ Count for pagination
  public long getUpcomingEventsCount() {
    return eventCollection.countDocuments(eq("status", "upcoming"));
  }
}
