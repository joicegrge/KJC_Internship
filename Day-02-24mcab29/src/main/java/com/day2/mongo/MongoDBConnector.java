package com.day2.mongo;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

public class MongoDBConnector {
    public static void main(String[] args) {
        String connectionString = "mongodb://localhost:27017";

        try (MongoClient client = MongoClients.create(connectionString)) {
            MongoDatabase db = client.getDatabase("LibraryDB");
            MongoCollection<Document> collection = db.getCollection("Books");

            Document book = new Document("title", "The Alchemist")
                    .append("author", "Paulo Coelho")
                    .append("year", 1988)
                    .append("genre", "Fiction");

            collection.insertOne(book);
            System.out.println("Book inserted into MongoDB!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
