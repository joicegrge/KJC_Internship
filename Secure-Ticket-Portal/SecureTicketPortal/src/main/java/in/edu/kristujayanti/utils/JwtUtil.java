package in.edu.kristujayanti.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtUtil {
  private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
  private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15 minutes
  private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

  public static String generateAccessToken(String email,  String role) {
    System.out.println("Generating token for: " + email); // Debug log
    return Jwts.builder()
      .setSubject(email)
      .claim("role", role)
      .setIssuedAt(new Date())
      .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
      .signWith(SECRET_KEY)
      .compact();
  }

  public static boolean isAdmin(String token) {
    Claims claims = parseToken(token);
    return "admin".equals(claims.get("role", String.class));
  }


  public static String generateRefreshToken(String email, String role) {
    return Jwts.builder()
      .setSubject(email)
      .setIssuedAt(new Date())
      .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
      .signWith(SECRET_KEY)
      .compact();
  }

  public static String getEmailFromToken(String token) {
    Claims claims = parseToken(token);
    return claims.getSubject(); // the subject is the email
  }


  public static Claims parseToken(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(SECRET_KEY)
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  public static boolean isTokenExpired(String token) {
    return parseToken(token).getExpiration().before(new Date());
  }

  public static String extractUserRole(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token)
        .getBody();
      return claims.get("role", String.class);
    } catch (Exception e) {
      return null;
    }
  }


}
