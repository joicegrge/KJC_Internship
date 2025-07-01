package in.edu.kristujayanti.utils;

import java.util.UUID;

public class TokenGenerator {
  public static String generateToken() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
  }
}
