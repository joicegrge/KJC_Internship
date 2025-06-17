package com.day2.library;

public class FictionBook extends Book {
    private String genre;

    public FictionBook(String title, String author, int year, String genre) {
        super(title, author, year);
        this.genre = genre;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("Genre: " + genre);
    }
}
