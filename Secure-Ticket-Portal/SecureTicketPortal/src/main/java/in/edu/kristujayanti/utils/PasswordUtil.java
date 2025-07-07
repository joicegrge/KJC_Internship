  package in.edu.kristujayanti.utils;

  import org.mindrot.jbcrypt.BCrypt;

  import java.security.SecureRandom;
  import java.util.Base64;

  public class PasswordUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateRandomPassword(int length) {
      String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < length; i++) {
        sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
      }

      return sb.toString();
    }

    public static String hashPassword(String plainPassword) {
      return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
      System.out.println("Verifying password:");
      System.out.println("Plain: " + plainPassword);
      System.out.println("Hashed: " + hashedPassword);
      boolean result = BCrypt.checkpw(plainPassword, hashedPassword);
      System.out.println("Verification result: " + result);
      return result;
    }

    // 🔐 Generate secure reset token
    public static String generateResetToken() {
      byte[] randomBytes = new byte[24];
      secureRandom.nextBytes(randomBytes);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
  }
