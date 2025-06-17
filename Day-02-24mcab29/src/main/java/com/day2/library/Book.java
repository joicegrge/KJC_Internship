package com.day2.library;

public class Book {
    protected String title;
    protected String author;
    protected int year;

    public Book(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }

    public void display() {
        System.out.println("Title: " + title + ", Author: " + author + ", Year: " + year);
    }
}
