# VibeBooks API 📚✨

[![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.10-blue?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Frontend](https://img.shields.io/badge/Frontend-Angular-red)](https://github.com/fabrodiego/VibeBooks-frontend)

[Versão em Português (README.pt-br.md)](README.pt-br.md)

## 🚀 About The Project

This is a full-stack portfolio project, developed as part of academic studies.

VibeBooks was conceived with the goal of connecting people through the most personal dimension of reading: **feelings**. More than just a review platform, the idea is to create a community where users can discover new books based on the "vibe" and emotions that a work sparked in other readers. It functions like an "Instagram for books," where the focus is not just on *what* you read, but *how* you felt while reading it.

This repository contains the complete backend (API) for the application. The **Frontend (Angular)** can be found here: [github.com/fabrodiego/VibeBooks-frontend](https://github.com/fabrodiego/VibeBooks-frontend).

---

## ✨ Implemented Features

* **Security:** Complete registration and login flow using **JWT (JSON Web Tokens)** and Spring Security. All endpoints are protected based on roles (`USER_ROLE`).
* **Book Management (Google API Integration):**
    * Users can add new books to the platform by providing only an **ISBN**.
    * The API automatically queries the **Google Books API** to fetch and save book details (title, author, cover, etc.).
* **Discovery Feed:** A main paginated endpoint (`GET /api/feed`) for book discovery, optimized to prevent N+1 problems.
* **Community "Vibe" Aggregation:**
    * The API calculates and returns the total count for **each sentiment** (e.g., `INSPIRING: 12`, `TENSE: 5`) for every book.
    * This is the core feature that allows the frontend to show which books match the "Vibe" the user is looking for.
* **Social Interactions:**
    * **Personal Shelf:** Users can set a status (`WANT_TO_READ`, `READING`, `READ`) and a personal **Sentiment** for any book.
    * **Likes:** Users can like/unlike both books and comments.
    * **Comments:** Full comment system for books.
    * **Follower System:** Users can follow and unfollow other users.

## 🔮 Future Vision (Next Steps)

* **Moderator Management:** Implementation of a `MODERATOR` role with exclusive permissions to edit (`PUT`) or delete (`DELETE`) books, ensuring data quality.
* **Recommendation Algorithm:** Enhance the feed to suggest books based on the user's past sentiments and interactions.

---

## 🛠️ Tech Stack

* **Core:**
    * Java: `21`
    * Spring Boot: `3.5.5`
    * Spring Security & JWT
* **Database:**
    * Spring Data JPA & Hibernate
    * PostgreSQL: `16.10`
* **API & Docs:**
    * SpringDoc (Swagger/OpenAPI)
* **Build & Utilities:**
    * Maven
    * Lombok
* **Testing:**
    * JUnit 5, MockMvc, H2 Database

---

## 📖 API Documentation

The complete and interactive API documentation (Swagger) is available when the `dev` profile is running. It can be accessed at:

[http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---

## ⚙️ Running Locally

### 1. Prerequisites
* JDK 21 or higher.
* Maven 3.8 or higher.
* A running PostgreSQL instance.

### 2. Configuration
1.  Clone the repository.
2.  Create a file named `src/main/resources/application-dev.properties`.
3.  Fill this file with your database credentials and a JWT secret. This profile (`dev`) is configured to run on port `8081`.

    ```properties
    # Port Configuration (Dev Environment)
    server.port=8081

    # PostgreSQL Configuration
    DB_URL=jdbc:postgresql://localhost:5432/vibebooks_db
    DB_USERNAME=your_postgres_user
    DB_PASSWORD=your_postgres_password

    # Secret to sign JWT tokens
    JWT_SECRET=your_super_long_secret_key_for_jwt
    ```

### 3. Execution
* Open a terminal in the project root and run the command to activate the `dev` profile:
    ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
    ```
* The API will be available at `http://localhost:8081`. (The default/prod profile runs on port `8080`).
