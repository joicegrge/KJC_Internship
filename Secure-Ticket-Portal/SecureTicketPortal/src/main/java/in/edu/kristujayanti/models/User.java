package in.edu.kristujayanti.models;

import org.bson.Document;

import java.util.Date;

public class User {
  private String email;
  private String name;
  private String passwordHash;
  private String role = "user";
  private Date createdAt = new Date();
  private String resetToken;
  private Date resetTokenExpiry;

  public User(String email, String name, String passwordHash) {
    this.email = email;
    this.name = name;
    this.passwordHash = passwordHash;
  }

  public Document toDocument() {
    Document doc = new Document("email", email)
      .append("name", name)
      .append("passwordHash", passwordHash)
      .append("role", role)
      .append("createdAt", createdAt);

    if (resetToken != null) {
      doc.append("resetToken", resetToken);
      doc.append("resetTokenExpiry", resetTokenExpiry);
    }

    return doc;
  }
}
