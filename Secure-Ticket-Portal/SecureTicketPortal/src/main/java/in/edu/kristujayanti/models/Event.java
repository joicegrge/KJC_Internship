package in.edu.kristujayanti.models;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

public class Event {
  private String id;
  private String name;
  private String description;
  private String location;
  private String dateTime;  // ISO 8601 format
  private int tokenLimit;
  private int tokensAvailable;
  private String status; // upcoming, active, closed

  public Event() {}

  public Event(String name, String description, String location, String dateTime,
               int tokenLimit, String status) {
    this.name = name;
    this.description = description;
    this.location = location;
    this.dateTime = dateTime;
    this.tokenLimit = tokenLimit;
    this.tokensAvailable = tokenLimit; // default to full availability
    this.status = status;
  }

  // ✅ Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() { return name; }

  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }

  public void setDescription(String description) { this.description = description; }

  public String getLocation() { return location; }

  public void setLocation(String location) { this.location = location; }

  public String getDateTime() { return dateTime; }

  public void setDateTime(String dateTime) { this.dateTime = dateTime; }

  public int getTokenLimit() { return tokenLimit; }

  public void setTokenLimit(int tokenLimit) { this.tokenLimit = tokenLimit; }

  public int getTokensAvailable() { return tokensAvailable; }

  public void setTokensAvailable(int tokensAvailable) { this.tokensAvailable = tokensAvailable; }

  public String getStatus() { return status; }

  public void setStatus(String status) { this.status = status; }

  // ✅ Convert to MongoDB Document
  public Document toDocument() {
    Document doc = new Document()
      .append("name", name)
      .append("description", description)
      .append("location", location)
      .append("dateTime", dateTime)
      .append("tokenLimit", tokenLimit)
      .append("tokensAvailable", tokensAvailable)
      .append("status", status);

    if (id != null && !id.isEmpty()) {
      doc.append("_id", new ObjectId(id));
    }

    return doc;
  }

  // ✅ Convert from MongoDB Document
  public static Event fromDocument(Document doc) {
    Event event = new Event();
    if (doc.containsKey("_id")) {
      event.setId(doc.getObjectId("_id").toHexString());
    }
    event.setName(doc.getString("name"));
    event.setDescription(doc.getString("description"));
    event.setLocation(doc.getString("location"));
    event.setDateTime(doc.getString("dateTime"));
    event.setTokenLimit(doc.getInteger("tokenLimit", 0));
    event.setTokensAvailable(doc.getInteger("tokensAvailable", event.getTokenLimit()));
    event.setStatus(doc.getString("status"));
    return event;
  }

  // ✅ Convert from frontend JSON (e.g., admindashboard.html)
  public static Event fromJSON(JSONObject json) {
    Event event = new Event();
    event.setName(json.getString("name"));
    event.setDescription(json.optString("description", ""));
    event.setLocation(json.optString("location", ""));
    event.setDateTime(json.getString("dateTime"));
    event.setTokenLimit(json.getInt("tokenLimit"));
    event.setTokensAvailable(json.getInt("tokenLimit")); // reset to full
    event.setStatus(json.optString("status", "upcoming"));
    return event;
  }

  // ✅ Convert to JSON for frontend consumption
  public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    json.put("id", id);
    json.put("name", name);
    json.put("description", description);
    json.put("location", location);
    json.put("dateTime", dateTime);
    json.put("tokenLimit", tokenLimit);
    json.put("tokensAvailable", tokensAvailable);
    json.put("status", status);
    return json;
  }
}
