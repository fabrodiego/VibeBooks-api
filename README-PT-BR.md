# VibeBooks API 📚✨

[![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.10-blue?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Frontend](https://img.shields.io/badge/Frontend-Angular-red)](https://github.com/fabrodiego/VibeBooks-frontend)

[English Version (README.md)](README.md)

## 🚀 Sobre o Projeto

Este é um projeto de portfólio full-stack, desenvolvido como parte de estudos acadêmicos.

VibeBooks foi concebido com o objetivo de conectar pessoas através da dimensão mais pessoal da leitura: os **sentimentos**. Mais do que uma plataforma de resenhas, a ideia é criar uma comunidade onde usuários possam descobrir novos livros com base na "vibe" e nas emoções que uma obra despertou em outros leitores. Funciona como um "Instagram de livros", onde o foco não é apenas *o que* você leu, mas *como* você se sentiu lendo.

Este repositório contém o backend (API) completo da aplicação. O **Frontend (Angular)** pode ser encontrado aqui: [github.com/fabrodiego/VibeBooks-frontend](https://github.com/fabrodiego/VibeBooks-frontend).

---

## ✨ Funcionalidades Implementadas

* **Segurança:** Fluxo completo de registro e login usando **JWT (JSON Web Tokens)** e Spring Security. Todos os endpoints são protegidos com base em papéis (`USER_ROLE`).
* **Gestão de Livros (Integração com Google API):**
    * Usuários podem adicionar novos livros à plataforma fornecendo apenas um **ISBN**.
    * A API consulta automaticamente a **Google Books API** para buscar e salvar os detalhes do livro (título, autor, capa, etc.).
* **Feed de Descoberta:** Um endpoint principal (`GET /api/feed`) paginado para a descoberta de livros, otimizado para evitar problemas de N+1.
* **Agregação da "Vibe" da Comunidade:**
    * A API calcula e retorna a contagem total para **cada sentimento** (ex: `INSPIRING: 12`, `TENSE: 5`) para cada livro.
    * Esta é a feature central que permite ao frontend mostrar quais livros combinam com a "Vibe" que o usuário procura.
* **Interações Sociais:**
    * **Estante Pessoal:** Usuários podem definir um status (`WANT_TO_READ`, `READING`, `READ`) e um **Sentimento** pessoal para qualquer livro.
    * **Curtidas:** Usuários podem curtir/descurtir tanto livros quanto comentários.
    * **Comentários:** Sistema completo de comentários nos livros.
    * **Sistema de Seguidores:** Usuários podem seguir e deixar de seguir outros usuários.

## 🔮 Visão Futura (Próximos Passos)

* **Gestão por Moderadores:** Implementação de um papel `MODERATOR` com permissões exclusivas para editar (`PUT`) ou deletar (`DELETE`) livros, garantindo a qualidade dos dados.
* **Algoritmo de Recomendação:** Melhorar o feed para sugerir livros com base nos sentimentos e interações passadas do usuário.

---

## 🛠️ Stack Tecnológica

* **Core:**
    * Java: `21`
    * Spring Boot: `3.5.5`
    * Spring Security & JWT
* **Database:**
    * Spring Data JPA & Hibernate
    * PostgreSQL: `16.10`
* **API & Docs:**
    * SpringDoc (Swagger/OpenAPI)
* **Build & Utilitários:**
    * Maven
    * Lombok
* **Testes:**
    * JUnit 5, MockMvc, H2 Database

---

## 📖 Documentação da API

A documentação interativa completa da API (Swagger) fica disponível quando o perfil `dev` está em execução. Ela pode ser acessada em:

[http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---

## ⚙️ Executando Localmente

### 1. Pré-requisitos
* JDK 21 ou superior.
* Maven 3.8 ou superior.
* Uma instância do PostgreSQL em execução.

### 2. Configuração
1.  Clone o repositório.
2.  Crie um arquivo chamado `src/main/resources/application-dev.properties`.
3.  Preencha este arquivo com suas credenciais de banco de dados e um segredo JWT. Este perfil (`dev`) está configurado para rodar na porta `8081`.

    ```properties
    # Configuração da Porta (Ambiente de Dev)
    server.port=8081

    # Configuração do PostgreSQL
    DB_URL=jdbc:postgresql://localhost:5432/vibebooks_db
    DB_USERNAME=seu_usuario_postgres
    DB_PASSWORD=sua_senha_postgres

    # Segredo para assinar os tokens JWT
    JWT_SECRET=sua_chave_secreta_super_longa_para_jwt
    ```


### 3. Execução
* Abra um terminal na raiz do projeto e execute o comando para ativar o perfil `dev`:
    ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
    ```

* A API estará disponível em `http://localhost:8081`. (O perfil padrão/prod roda na porta `8080`).
