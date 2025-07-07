package in.edu.kristujayanti.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import in.edu.kristujayanti.db.MongoConnection;
import in.edu.kristujayanti.models.User;
import in.edu.kristujayanti.utils.EmailUtil;
import in.edu.kristujayanti.utils.JwtUtil;
import in.edu.kristujayanti.utils.PasswordUtil;
import io.jsonwebtoken.Claims;
import org.bson.Document;
import org.json.JSONObject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.mongodb.client.model.Filters.eq;

public class UserService {

  private final MongoCollection<Document> userCollection;

  public UserService() {
    MongoDatabase db = MongoConnection.getDatabase();
    userCollection = db.getCollection("users");
  }

  public String registerUser(String email, String name) {
    // Check if email already exists
    Document existingUser = userCollection.find(eq("email", email)).first();
    if (existingUser != null) {
      return "User with this email already exists.";
    }

    // Generate random password
    String password = PasswordUtil.generateRandomPassword(10);

    // Hash password
    String hashedPassword = PasswordUtil.hashPassword(password);

    // Create user and insert into MongoDB
    User user = new User(email, name, hashedPassword);
    userCollection.insertOne(user.toDocument());

    // Send password via email asynchronously
    EmailUtil.sendPasswordEmailAsync(email, password);

    return "User registered successfully. Password sent to email.";
  }

  // ✅ Step 1.8 - Initiate Password Reset
  public String initiatePasswordReset(String email) {
    Document userDoc = userCollection.find(eq("email", email)).first();
    if (userDoc == null) {
      return "User not found.";
    }

    String resetToken = PasswordUtil.generateResetToken();
    Instant expiry = Instant.now().plus(15, ChronoUnit.MINUTES);

    userCollection.updateOne(
      eq("email", email),
      new Document("$set", new Document("resetToken", resetToken)
        .append("resetTokenExpiry", Date.from(expiry)))
    );

    String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;

    EmailUtil.sendEmailAsync(email, "Password Reset Request",
      "Click the link below to reset your password:\n" + resetLink);

    return "Password reset link sent to email.";
  }

  // ✅ Step 1.8 - Complete Password Reset
  public String completePasswordReset(String token, String newPassword) {
    Document userDoc = userCollection.find(eq("resetToken", token)).first();
    if (userDoc == null) {
      return "Invalid or expired token.";
    }

    Date expiryDate = userDoc.getDate("resetTokenExpiry");
    if (expiryDate == null || expiryDate.before(new Date())) {
      return "Token expired.";
    }

    String hashedNewPassword = PasswordUtil.hashPassword(newPassword);

    userCollection.updateOne(
      eq("resetToken", token),
      new Document("$set", new Document("passwordHash", hashedNewPassword))
        .append("$unset", new Document("resetToken", "").append("resetTokenExpiry", ""))
    );

    return "Password reset successfully.";
  }

  // ✅ Login - Generate tokens with role
  public String authenticateUser(String email, String password) {
    // ✅ Special case for hardcoded admin login
    if (email.equalsIgnoreCase("admin@domain.com") && password.equals("admin")) {
      JSONObject tokens = new JSONObject();
      tokens.put("accessToken", JwtUtil.generateAccessToken(email, "admin"));
      tokens.put("refreshToken", JwtUtil.generateRefreshToken(email, "admin"));
      return tokens.toString();
    }

    // ✅ Standard user login
    Document userDoc = userCollection.find(eq("email", email)).first();
    if (userDoc == null || !PasswordUtil.verifyPassword(password, userDoc.getString("passwordHash"))) {
      return null;
    }

    JSONObject tokens = new JSONObject();
    tokens.put("accessToken", JwtUtil.generateAccessToken(email, "user"));
    tokens.put("refreshToken", JwtUtil.generateRefreshToken(email, "user"));
    return tokens.toString();
  }

  public String refreshToken(String refreshToken) {
    try {
      Claims claims = JwtUtil.parseToken(refreshToken);
      String email = claims.getSubject();
      String role = claims.get("role", String.class);  // ✅ Extract role from old token
      return JwtUtil.generateAccessToken(email, role);
    } catch (Exception e) {
      return null;
    }
  }

  public boolean invalidateToken(String email) {
    return true; // handled by client deleting token
  }
}
