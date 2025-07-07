package in.edu.kristujayanti.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import in.edu.kristujayanti.db.MongoConnection;
import in.edu.kristujayanti.utils.EmailUtil;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.inc;

public class BookingService {

  private final MongoCollection<Document> bookingCollection;
  private final MongoCollection<Document> eventCollection;
  private final MongoCollection<Document> userCollection;
  private final SecureRandom random = new SecureRandom();

  public BookingService() {
    MongoDatabase db = MongoConnection.getDatabase();
    bookingCollection = db.getCollection("bookings");
    eventCollection = db.getCollection("events");
    userCollection = db.getCollection("users");
  }

  public Document bookEvent(String userEmail, String eventId) {
    try {
      ObjectId objId = new ObjectId(eventId);
      System.out.println("Booking event for user: " + userEmail + " | Event ID: " + eventId);

      // Verify user exists
      Document user = userCollection.find(eq("email", userEmail)).first();
      if (user == null) throw new RuntimeException("User not found");

      // Get event details
      Document event = eventCollection.find(eq("_id", objId)).first();
      if (event == null) throw new RuntimeException("Event not found");

      // Check token availability
      int available = event.getInteger("tokensAvailable", 0);
      if (available <= 0) throw new RuntimeException("No tokens available");

      // Check for existing booking
      Document existingBooking = bookingCollection.find(and(
        eq("userEmail", userEmail),
        eq("eventId", eventId)
      )).first();

      if (existingBooking != null) throw new RuntimeException("Already booked");

      // Generate unique token
      String bookingToken = generateBookingToken();

      // Create comprehensive booking document
      Document booking = new Document()
        .append("userEmail", userEmail)
        .append("eventId", eventId)
        .append("token", bookingToken)
        .append("bookingDate", new Date())
        .append("status", "confirmed")
        .append("eventName", event.getString("name"))
        .append("eventDate", event.getString("dateTime"))
        .append("eventLocation", event.getString("location"));

      // Save booking and update event
      bookingCollection.insertOne(booking);
      eventCollection.updateOne(eq("_id", objId), inc("tokensAvailable", -1));

      // Send confirmation email
      sendConfirmationEmail(userEmail, bookingToken, event);

      return booking;

    } catch (IllegalArgumentException ex) {
      throw new RuntimeException("Invalid event ID format");
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new RuntimeException("Unexpected error occurred during booking");
    }
  }

  private void sendConfirmationEmail(String userEmail, String token, Document event) {
    String subject = "🎟️ Event Booking Confirmation";
    String body = "Thank you for booking.\n\n" +
      "Your token: " + token + "\n" +
      "Event: " + event.getString("name") + "\n" +
      "Date: " + event.getString("dateTime") + "\n" +
      "Location: " + event.getString("location") + "\n\n" +
      "Please show this token at the entrance.";

    System.out.println("Sending confirmation email to " + userEmail);
    EmailUtil.sendEmailAsync(userEmail, subject, body);
  }

  private String generateBookingToken() {
    byte[] bytes = new byte[12];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public List<Document> getUserBookings(String userEmail) {
    // Get all bookings for the user
    List<Document> bookings = bookingCollection.find(eq("userEmail", userEmail))
      .sort(new Document("timestamp", -1)) // Sort by most recent first
      .into(new ArrayList<>());

    // Enrich each booking with event details
    for (Document booking : bookings) {
      try {
        // Get the associated event
        Document event = eventCollection.find(eq("_id", new ObjectId(booking.getString("eventId"))))
          .first();

        if (event != null) {
          // Add event details to booking if they don't exist
          if (!booking.containsKey("eventName")) {
            booking.put("eventName", event.getString("name"));
          }
          if (!booking.containsKey("eventDate")) {
            booking.put("eventDate", event.getString("dateTime"));
          }
          if (!booking.containsKey("eventLocation")) {
            booking.put("eventLocation", event.getString("location"));
          }
          if (!booking.containsKey("bookingDate")) {
            booking.put("bookingDate", booking.getDate("timestamp"));
          }
          if (!booking.containsKey("status")) {
            booking.put("status", "confirmed");
          }
        }
      } catch (Exception e) {
        System.err.println("Error enriching booking: " + e.getMessage());
      }
    }

    return bookings;
  }
}
