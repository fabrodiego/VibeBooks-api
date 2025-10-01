package com.vibebooks.api.model;

import com.vibebooks.api.dto.BookCreationDTO;
import com.vibebooks.api.dto.google.VolumeInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    private static final Logger log = LoggerFactory.getLogger(Book.class);

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

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

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

    public static Book fromGoogleVolumeInfo(VolumeInfo volumeInfo, String isbn) {
        var book = new Book();
        book.setTitle(volumeInfo.title());

        if (volumeInfo.authors() != null && !volumeInfo.authors().isEmpty()) {
            book.setAuthor(volumeInfo.authors().getFirst());
        } else {
            book.setAuthor("Autor desconhecido");
        }

        book.setIsbn(isbn);

        if (volumeInfo.publishedDate() != null && !volumeInfo.publishedDate().isEmpty()) {
            try {
                book.setPublicationYear(Integer.parseInt(volumeInfo.publishedDate().substring(0, 4)));
            } catch (Exception e) {
                log.warn("Could not parse publication year '{}' for ISBN {}", volumeInfo.publishedDate(), isbn);
            }
        }

        if (volumeInfo.imageLinks() != null && volumeInfo.imageLinks().thumbnail() != null) {
            book.setCoverImageUrl(volumeInfo.imageLinks().thumbnail());
        } else {
            book.setCoverImageUrl("https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg");
        }

        return book;
    }
}