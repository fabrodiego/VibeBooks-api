package com.vibebooks.api.controller;

import com.vibebooks.api.dto.ComentarioCadastroDTO;
import com.vibebooks.api.dto.LivroCurtidaResponseDTO;
import com.vibebooks.api.dto.ComentarioDetalhesDTO;
import com.vibebooks.api.dto.LivroCadastroDTO;
import com.vibebooks.api.dto.LivroDetalhesDTO;
import com.vibebooks.api.model.Comentario;
import com.vibebooks.api.model.Livro;
import com.vibebooks.api.model.Usuario;
import com.vibebooks.api.model.UsuarioLivroStatus;
import com.vibebooks.api.model.UsuarioLivroStatusId;
import com.vibebooks.api.repository.UsuarioLivroStatusRepository;
import com.vibebooks.api.repository.ComentarioRepository;
import com.vibebooks.api.repository.LivroRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroRepository livroRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioLivroStatusRepository usuarioLivroStatusRepository;

    public LivroController(LivroRepository livroRepository, ComentarioRepository comentarioRepository, UsuarioLivroStatusRepository usuarioLivroStatusRepository) {
        this.livroRepository = livroRepository;
        this.comentarioRepository = comentarioRepository;
        this.usuarioLivroStatusRepository = usuarioLivroStatusRepository;
    }

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
        // 1. Busca o livro no repositório pelo “ID”
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

    @PostMapping("/{id}/comentarios")
    @Transactional
    public ResponseEntity<ComentarioDetalhesDTO> adicionarComentario(
            @PathVariable UUID id,
            @RequestBody @Valid ComentarioCadastroDTO dados,
            Authentication authentication,
            UriComponentsBuilder uriBuilder
    ) {
        var livro = livroRepository.getReferenceById(id);

        Usuario usuarioLogado = (Usuario)  authentication.getPrincipal();

        var comentario = new Comentario(dados.texto(), usuarioLogado, livro);

        var comentarioSalvo = comentarioRepository.saveAndFlush(comentario);

        var uri = uriBuilder.path("/api/comentarios/{id}").buildAndExpand(comentarioSalvo.getId()).toUri();

        return ResponseEntity.created(uri).body(new ComentarioDetalhesDTO(comentarioSalvo));
    }

    /**
     * Endpoint para listar todos os comentários de um livro específico.
     *
     * @param id O "ID" do livro.
     * @return Uma lista de DTOs de detalhes dos comentários, ou 404 Not Found se o livro não existir.
     */
    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<ComentarioDetalhesDTO>> listarComentariosPorLivro(@PathVariable UUID id) {
        if (!livroRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        var comentarios = comentarioRepository.findAllByLivroId(id);

        var dtos = comentarios.stream().map(ComentarioDetalhesDTO::new).toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/curtir")
    @Transactional
    public ResponseEntity<LivroCurtidaResponseDTO> curtirOuDescurtirLivro(@PathVariable UUID id, Authentication authentication) {
        var usuarioLogado = (Usuario) authentication.getPrincipal();

        var livro = livroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));

        var statusId = new UsuarioLivroStatusId(usuarioLogado.getId(), livro.getId());

        var status = usuarioLivroStatusRepository.findById(statusId).orElseGet(() -> {
            var novoStatus = new UsuarioLivroStatus();
            novoStatus.setId(statusId);
            novoStatus.setUsuario(usuarioLogado);
            novoStatus.setLivro(livro);
            return novoStatus;
        });

        status.setCurtido(!status.isCurtido());
        usuarioLivroStatusRepository.saveAndFlush(status);

        long totalCurtidas = usuarioLivroStatusRepository.countByLivroIdAndCurtidoIsTrue(livro.getId());

        return ResponseEntity.ok(new LivroCurtidaResponseDTO(status.isCurtido(), totalCurtidas));
    }
}
