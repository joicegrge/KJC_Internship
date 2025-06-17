package com.day2.library;

public class LibraryMain {
    public static void main(String[] args) {
        Book fiction = new FictionBook("Harry Potter", "J.K. Rowling", 1997, "Fantasy");
        Book nonFiction = new NonFictionBook("Brief History of Time", "Stephen Hawking", 1988, "Science");

        fiction.display();
        System.out.println("--------");
        nonFiction.display();
    }
}
