package in.edu.kristujayanti.utils;

import java.util.Random;

public class PasswordGenerator {
  public static String generate(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    Random rand = new Random();
    StringBuilder password = new StringBuilder();
    for (int i = 0; i < length; i++) {
      password.append(chars.charAt(rand.nextInt(chars.length())));
    }
    return password.toString();
  }
}
