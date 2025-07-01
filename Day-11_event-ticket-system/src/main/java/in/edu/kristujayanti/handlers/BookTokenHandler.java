package in.edu.kristujayanti.handlers;

import com.sun.net.httpserver.*;
import in.edu.kristujayanti.services.EmailService;
import in.edu.kristujayanti.services.EventService;
import in.edu.kristujayanti.utils.QRCodeGenerator;
import in.edu.kristujayanti.utils.TokenGenerator;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BookTokenHandler implements HttpHandler {
  private final EventService eventService;

  public BookTokenHandler(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
      exchange.sendResponseHeaders(405, -1);
      return;
    }

    InputStream is = exchange.getRequestBody();
    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
    JSONObject json = new JSONObject(body);

    String email = json.getString("email");
    String eventId = json.getString("eventId");

    boolean success = eventService.bookToken(eventId);

    JSONObject response = new JSONObject();
    if (success) {
      String token = TokenGenerator.generateToken();

      // Save booking record
      eventService.recordBooking(email, eventId, token);

      // Generate QR code for the token
      byte[] qrBytes = new byte[0];
      try {
        qrBytes = QRCodeGenerator.generateQRCodeImage(token, 250, 250);
      } catch (Exception e) {
        e.printStackTrace();
      }

      // Send token + QR code via email
      EmailService.send(email, "Event Booking Confirmation", "Your booking token is: " + token, qrBytes);

      // Send response to frontend
      response.put("status", "success");
      response.put("token", token);
      exchange.sendResponseHeaders(200, response.toString().length());
    } else {
      response.put("status", "failed");
      response.put("message", "No tokens left or invalid event ID.");
      exchange.sendResponseHeaders(400, response.toString().length());
    }



    OutputStream os = exchange.getResponseBody();
    os.write(response.toString().getBytes());
    os.close();
  }
}
