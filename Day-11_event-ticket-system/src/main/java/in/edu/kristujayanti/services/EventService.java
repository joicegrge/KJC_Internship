package in.edu.kristujayanti.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.inc;

public class EventService {
  private final MongoCollection<Document> eventCollection;
  private final MongoCollection<Document> bookingCollection;


  public EventService(MongoDatabase database) {
    this.eventCollection = database.getCollection("events"); // Make sure the collection name is "events"
    this.bookingCollection = database.getCollection("bookings");
  }

  public List<Document> listEvents() {
    return eventCollection.find().into(new ArrayList<>());
  }

  public MongoCollection<Document> getEventCollection() {
    return this.eventCollection;
  }


  public void deleteEventById(String id) {
    eventCollection.deleteOne(eq("_id", new ObjectId(id)));
  }

  public List<Document> getAllBookings() {
    return bookingCollection.find().into(new ArrayList<>());
  }



  public void recordBooking(String email, String eventId, String token) {
    Document booking = new Document();
    booking.append("email", email);
    booking.append("eventId", new ObjectId(eventId));
    booking.append("token", token);
    booking.append("bookedAt", LocalDateTime.now().toString());
    bookingCollection.insertOne(booking);
  }

  public boolean bookToken(String eventIdStr) {
    try {
      ObjectId eventId = new ObjectId(eventIdStr);

      Document event = eventCollection.find(eq("_id", eventId)).first();
      if (event == null) return false;

      int available = event.getInteger("availableTokens", 0);
      if (available <= 0) return false;

      eventCollection.updateOne(eq("_id", eventId), inc("availableTokens", -1));
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
