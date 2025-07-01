package in.edu.kristujayanti.services;

import com.mongodb.client.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class UserService {
  private final MongoCollection<Document> users;

  public UserService(MongoDatabase db) {
    this.users = db.getCollection("users");
  }

  public boolean userExists(String email) {
    return users.find(eq("email", email)).first() != null;
  }

  public void registerUser(String name, String email, String password) {
    Document doc = new Document("name", name).append("email", email).append("password", password);
    users.insertOne(doc);
  }

  public boolean login(String email, String password) {
    Document user = users.find(eq("email", email)).first();
    return user != null && user.getString("password").equals(password);
  }

  public List<Document> getAllUsers() {
    return users.find().into(new ArrayList<>());
  }

  public void deleteUserByEmail(String email) {
    users.deleteOne(eq("email", email));
  }

}
