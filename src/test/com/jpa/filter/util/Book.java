package com.jpa.filter.util;

import java.time.LocalDate;

public class Book {
    private String name;
    private Author author;

    private LocalDate publishingDate;

    public LocalDate getPublishingDate() {
        return publishingDate;
    }

    public void setPublishingDate(LocalDate publishingDate) {
        this.publishingDate = publishingDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public Author getAuthor() {
        return author;
    }
}
