# VibeBooks API 📚✨

Welcome to the VibeBooks API, a social network for book lovers! This is the complete backend for the application, built with Java and Spring Boot, designed to be robust, secure, and scalable.

## 🚀 About The Project

This is a full-stack portfolio project, developed as part of academic studies.

VibeBooks was conceived with the goal of connecting people through the most personal dimension of reading: **feelings**. More than just a review platform, the idea is to create a community where users can discover new books and new friends based on the "vibe" and emotions that a work sparked in other readers. It functions like an "Instagram for books," where the focus is not just on what you read, but how you felt while reading it.

---

## ✨ Implemented Features

* **Security & Authentication with Roles:**
    * **Current Logic:** The system features a complete registration and login flow using JWT Tokens. All registered users are assigned the default `USER` role.
    * **Future Vision:** A `MODERATOR` role will be implemented. This role will have exclusive permissions to manage the global book catalog, such as editing (`PUT`) and deleting (`DELETE`) any book, ensuring the platform's data quality.

* **Book Discovery Feed:**
    * **Current Logic:** The main `GET /api/feed` endpoint provides a paginated list of books for discovery, displaying the most recently added books and optimized to prevent the N+1 problem.
    * **Future Vision:** The logic will be enhanced to include a recommendation algorithm, suggesting books based on the user's categories, sentiments, and interactions.

* **Book Management (CRUD with Permissions):**
    * **Current Logic:** Currently, any authenticated user (`USER`) can perform all CRUD operations (create, update, and delete) on books.
    * **Future Vision:** Book management will be enhanced with the following rules:
        * **Creation (`POST`):** Any `USER` will be able to register a new book on the platform by providing only its **ISBN**. The API will integrate with the **Google Books API** to automatically fetch and populate all other details (title, author, year, cover).
        * **Update & Deletion (`PUT`, `DELETE`):** These operations will become restricted and executable only by users with the `MODERATOR` role.

* **Social Interactions:**
    * **Comments:** Users can comment on books and delete their own comments.
    * **Likes:** Users can like/unlike books and comments.
    * **Reading Shelf:** Users can mark books with a status (`WANT_TO_READ`, `READING`, `READ`) and associate a sentiment.
    * **Followers System:** Users can follow and unfollow others.

---

## 🛠️ Tech Stack

* **Backend:** Java 21, Spring Boot 3
* **Security:** Spring Security, JWT (JSON Web Tokens)
* **Database:** Spring Data JPA, Hibernate, PostgreSQL
* **Build:** Maven
* **API Documentation:** SpringDoc (Swagger/OpenAPI)
* **Testing:** JUnit 5, MockMvc, H2 Database
* **Utilities:** Lombok

---

## 📖 API Documentation

The complete and interactive API documentation, generated with Swagger, is available while the application is running locally at the following address:

[http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---

## ⚙️ How to Run Locally

1.  **Prerequisites:**
    * JDK 21 or higher.
    * Maven 3.8 or higher.
    * A running PostgreSQL instance.

2.  **Configuration:**
    * Clone the repository.
    * Create a file `src/main/resources/application-dev.properties`.
    * Fill this file with your local database credentials (the `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` variables).

3.  **Execution:**
    * Open a terminal in the project root and run the command:
        ```bash
        ./mvnw spring-boot:run
        ```
    * The API will be available at `http://localhost:8080` (or the port you configured).