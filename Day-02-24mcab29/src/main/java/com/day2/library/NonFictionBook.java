package com.day2.library;

public class NonFictionBook extends Book {
    private String subject;

    public NonFictionBook(String title, String author, int year, String subject) {
        super(title, author, year);
        this.subject = subject;
    }

    @Override
    public void display() {
        super.display();
        System.out.println("Subject: " + subject);
    }
}
