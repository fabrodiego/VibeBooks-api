# VibeBooks API 📚✨

Bem-vindo à API do VibeBooks, uma rede social para amantes de livros! Este projeto é o backend completo da aplicação, construído com Java e Spring Boot, projetado para ser robusto, seguro e escalável.

## 🚀 Sobre o Projeto

Este é um projeto full-stack de portfólio, desenvolvido como parte de estudos acadêmicos.

O VibeBooks foi concebido com o objetivo de conectar pessoas através da dimensão mais pessoal da leitura: os **sentimentos**. Mais do que apenas uma plataforma de resenhas, a ideia é criar uma comunidade onde os usuários possam descobrir novos livros e novos amigos com base na "vibe" e nas emoções que uma obra despertou em outros leitores. Funciona como um "Instagram para livros", onde o foco não é apenas o que você leu, mas como você se sentiu lendo.

---

## ✨ Features Implementadas

* **Segurança e Autenticação com Papéis (Roles):**
    * **Lógica Atual:** O sistema possui um fluxo completo de cadastro e login com Tokens JWT. Todos os usuários cadastrados recebem o papel padrão de `USER`.
    * **Visão Futura:** Será implementado o papel de `MODERATOR`. Este papel terá permissões exclusivas para gerenciar o catálogo global de livros, como a edição (`PUT`) e a exclusão (`DELETE`) de qualquer livro, garantindo a qualidade dos dados da plataforma.

* **Feed de Descoberta de Livros:**
    * **Lógica Atual:** O endpoint principal `GET /api/feed` apresenta uma lista paginada de livros para descoberta, exibindo os livros mais recentemente adicionados à plataforma, já otimizado para não causar o problema N+1.
    * **Visão Futura:** A lógica será aprimorada para incluir um algoritmo de recomendação, sugerindo livros com base nas categorias, sentimentos e interações do usuário.

* **Gerenciamento de Livros (CRUD com Permissões):**
    * **Lógica Atual:** Atualmente, qualquer usuário autenticado (`USER`) pode realizar todas as operações de CRUD (criar, atualizar e deletar) sobre os livros.
    * **Visão Futura:** A gestão de livros será aprimorada com as seguintes regras:
        * **Criação (`POST`):** Qualquer usuário `USER` poderá cadastrar um novo livro na plataforma informando apenas seu **ISBN**. A API irá se integrar com o **Google Books** para buscar e preencher automaticamente todos os outros detalhes (título, autor, ano, capa).
        * **Atualização e Deleção (`PUT`, `DELETE`):** Estas operações se tornarão restritas e poderão ser executadas apenas por usuários com o papel `MODERATOR`.

* **Interações Sociais:**
    * **Comentários:** Usuários podem comentar em livros e deletar os próprios comentários.
    * **Curtidas (Likes):** Usuários podem curtir/descurtir livros e comentários.
    * **Estante de Leitura:** Usuários podem marcar livros com status (`WANT_TO_READ`, `READING`, `READ`) e associar um sentimento.
    * **Sistema de Seguidores:** Usuários podem seguir e deixar de seguir outros usuários.
    * 
---

## 🛠️ Tecnologias Utilizadas

* **Backend:** Java 21, Spring Boot 3
* **Segurança:** Spring Security, JWT (JSON Web Tokens)
* **Banco de Dados:** Spring Data JPA, Hibernate, PostgreSQL
* **Build:** Maven
* **Documentação:** SpringDoc (Swagger/OpenAPI)
* **Testes:** JUnit 5, MockMvc, H2 Database
* **Utilitários:** Lombok

---

## 📖 Documentação da API

A documentação completa e interativa da API, gerada com Swagger, está disponível quando a aplicação está rodando localmente no seguinte endereço:

[http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---

## ⚙️ Como Rodar Localmente

1.  **Pré-requisitos:**
    * JDK 21 ou superior.
    * Maven 3.8 ou superior.
    * Uma instância do PostgreSQL rodando.

2.  **Configuração:**
    * Clone o repositório.
    * Crie um arquivo `src/main/resources/application-dev.properties`.
    * Preencha este arquivo com suas credenciais de banco de dados locais (as variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`).

3.  **Execução:**
    * Abra um terminal na raiz do projeto e execute o comando:
      ```bash
      ./mvnw spring-boot:run
      ```
    * A API estará disponível em `http://localhost:8080` (ou a porta que você configurou).