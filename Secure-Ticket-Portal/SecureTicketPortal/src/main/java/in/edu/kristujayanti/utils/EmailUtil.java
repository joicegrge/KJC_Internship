package in.edu.kristujayanti.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.FileInputStream;
import java.util.Properties;

public class EmailUtil {

  private static final Properties emailConfig = new Properties();

  static {
    try {
      emailConfig.load(new FileInputStream("src/main/resources/config.properties"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // ✅ Used for sending registration password
  public static void sendPasswordEmailAsync(String toEmail, String password) {
    new Thread(() -> sendEmail(toEmail, "Welcome to SecureTicketPortal!",
      "Your account has been created.\n\nLogin Email: " + toEmail +
        "\nPassword: " + password +
        "\n\nPlease change your password after first login.")).start();
  }

  // ✅ New generic async method for things like password reset
  public static void sendEmailAsync(String toEmail, String subject, String body) {
    new Thread(() -> sendEmail(toEmail, subject, body)).start();
  }

  // ✅ Shared private method used by both async methods
  private static void sendEmail(String toEmail, String subject, String body) {
    String fromEmail = emailConfig.getProperty("smtp.username");
    String password = emailConfig.getProperty("smtp.password");

    Properties props = new Properties();
    props.put("mail.smtp.auth", true);
    props.put("mail.smtp.starttls.enable", true);
    props.put("mail.smtp.host", emailConfig.getProperty("smtp.host"));
    props.put("mail.smtp.port", emailConfig.getProperty("smtp.port"));

    Session session = Session.getInstance(props, new Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(fromEmail, password);
      }
    });

    try {
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(fromEmail));
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
      msg.setSubject(subject);
      msg.setText(body);

      Transport.send(msg);
      System.out.println("Email sent to " + toEmail);
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }
}
