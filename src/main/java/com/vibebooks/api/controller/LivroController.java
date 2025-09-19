package com.vibebooks.api.controller;

import com.vibebooks.api.dto.LivroCadastroDTO;
import com.vibebooks.api.dto.LivroDetalhesDTO;
import com.vibebooks.api.model.Livro;
import com.vibebooks.api.repository.LivroRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    @Autowired
    private LivroRepository livroRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<LivroDetalhesDTO> cadastrarLivro(@RequestBody @Valid LivroCadastroDTO dados, UriComponentsBuilder uriBuilder) {
        // 1. Converte o DTO recebido para a entidade Livro
        var livro = new Livro(dados);

        // 2. Salva a nova entidade no db
        livroRepository.save(livro);

        // 3. Cria a URI de retorno
        var uri = uriBuilder.path("/api/livros/{id}").buildAndExpand(livro.getId()).toUri();

        // 4. Retorna o status 201 Created com a URI e os do livro recentemente cadastrado
        return ResponseEntity.created(uri).body(new LivroDetalhesDTO(livro));
    }

    @GetMapping
    public ResponseEntity<List<LivroDetalhesDTO>> listarLivros() {
        // 1. Busca todos os livros no repositório
        var livros = livroRepository.findAll();

        // 2. Converte a lista de entidades Livro para uma lista de DTOs de detalhes
        var dtos = livros.stream().map(LivroDetalhesDTO::new).toList();

        // 3. Retorna a lista com status 200 OK
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivroDetalhesDTO> detalharLivro(@PathVariable UUID id) {
        // 1. Busca o livro no repositório pelo ID
        var livroOptional = livroRepository.findById(id);

        // 2. Se o livro não for encontrado, retorna o status 404 Not Found
        if (livroOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 3. Se encontrado, converte para o DTO de detalhes e retorna com status 200 OK
        var livro = livroOptional.get();
        return ResponseEntity.ok(new LivroDetalhesDTO(livro));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<LivroDetalhesDTO> atualizarLivro(@PathVariable UUID id, @RequestBody @Valid LivroCadastroDTO dados){
        // 1. Busca o livro que queremos atualizar no db
        var livroOptional = livroRepository.findById(id);

        // 2. Se não encontrar, retorna 404 Not Found
        if (livroOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 3. Se encontrado, atualiza os dados do livro com as informações recebidas no DTO
        var livro = livroOptional.get();
        livro.atualizarInformacoes(dados);

        // 4. O JPA já entende que o objeto foi modificado dentro da transação
        // e vai salvar as alterações automaticamente no db quando terminar o fluxo do método.

        // 5. Retorna 200 OK com os dados atualizado do livro
        return ResponseEntity.ok(new LivroDetalhesDTO(livro));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletarLivro(@PathVariable UUID id) {
        // 1. Antes de deletar, verificamos se o livro realmente existe
        if (!livroRepository.existsById(id)) {
            // 2. Se não existir, retornamos 404 Not Found
            return ResponseEntity.notFound().build();
        }

        // 3. Se o livro existe, mandamos o repositório excluir
        livroRepository.deleteById(id);

        // 4. Retornamos o status 204 No Content
        return ResponseEntity.noContent().build();
    }
}
