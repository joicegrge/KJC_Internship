package in.edu.kristujayanti.services;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.util.Properties;

public class EmailService {
  private static final String FROM = "joicegrge2002@gmail.com";
  private static final String PASSWORD = "slvl douk must ikja";

  // Modified send method
  public static void send(String to, String subject, String content, byte[] qrImageBytes) {
    Properties props = new Properties();
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");

    Session session = Session.getInstance(props, new Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(FROM, PASSWORD);
      }
    });


    try {
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(FROM));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
      message.setSubject(subject);

      MimeBodyPart textPart = new MimeBodyPart();
      textPart.setText(content);

      Multipart multipart = new MimeMultipart();
      multipart.addBodyPart(textPart);

      if (qrImageBytes != null && qrImageBytes.length > 0) {
        MimeBodyPart imagePart = new MimeBodyPart();
        DataSource source = new ByteArrayDataSource(qrImageBytes, "image/png");
        imagePart.setDataHandler(new DataHandler(source));
        imagePart.setFileName("token.png");
        multipart.addBodyPart(imagePart);
      }

      message.setContent(multipart);
      Transport.send(message);
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }

  // Existing password email (optional fallback)
  public static void send(String to, String subject, String content) {
    send(to, subject, content, null);
  }
}
