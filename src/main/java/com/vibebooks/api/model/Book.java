package com.vibebooks.api.model;

import com.vibebooks.api.dto.BookCreationDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(unique = true, length = 13)
    private String isbn;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public Book(BookCreationDTO data) {
        this.title = data.title();
        this.author = data.author();
        this.isbn = data.isbn();
        this.publicationYear = data.publicationYear();
        this.coverImageUrl = data.coverImageUrl();
    }

    public void updateInformation(BookCreationDTO data) {
        if (data.title() != null) {
            this.title = data.title();
        }
        if (data.author() != null) {
            this.author = data.author();
        }
        if (data.isbn() != null) {
            this.isbn = data.isbn();
        }
        if (data.publicationYear() != null) {
            this.publicationYear = data.publicationYear();
        }
        if (data.coverImageUrl() != null) {
            this.coverImageUrl = data.coverImageUrl();
        }
    }
}