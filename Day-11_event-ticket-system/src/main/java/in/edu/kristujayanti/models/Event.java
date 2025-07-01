package in.edu.kristujayanti.models;

public class Event {
  public String id;
  public String title;
  public String date;
  public int availableTokens;

  public Event(String id, String title, String date, int availableTokens) {
    this.id = id;
    this.title = title;
    this.date = date;
    this.availableTokens = availableTokens;
  }
}
