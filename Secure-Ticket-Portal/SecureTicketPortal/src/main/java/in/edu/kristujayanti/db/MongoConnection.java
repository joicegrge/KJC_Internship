package in.edu.kristujayanti.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MongoConnection {
  private static MongoDatabase database;

  static {
    try {
      Properties props = new Properties();
      props.load(new FileInputStream("src/main/resources/config.properties"));
      String uri = props.getProperty("mongo.uri");
      String dbName = props.getProperty("mongo.db");

      MongoClient mongoClient = MongoClients.create(uri);
      database = mongoClient.getDatabase(dbName);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static MongoDatabase getDatabase() {
    return database;
  }
}
