package musicasstream.bibliotecademusicas.controller.Banda;

import jakarta.validation.Valid;
import musicasstream.bibliotecademusicas.domain.Avaliacao.AvaliacaoDeBanda;
import musicasstream.bibliotecademusicas.domain.Avaliacao.DadosAvaliacaoBanda;
import musicasstream.bibliotecademusicas.domain.Banda.*;
import musicasstream.bibliotecademusicas.infra.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/bandas")
public class BandaController {

    @Autowired
    private BandaRepository repository;

    @GetMapping
    public ResponseEntity<Page<DadosListagemBanda>> listaDeBandas(Pageable paginacao) {


        var page = repository.findAllByExcluidoFalse(paginacao).map(DadosListagemBanda::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity bandaEspecifica(@PathVariable Long id) {
        Banda banda = repository.findById(id).orElseThrow(() 
                -> new ResourceNotFoundException("Banda não encontrada"));
        return ResponseEntity.ok(new DadosDetalhamentoBanda(banda));
    }

    @PostMapping
    @Transactional
    public ResponseEntity criarBanda(@RequestBody @Valid DadosCadastroBanda dados,
                                     UriComponentsBuilder uriBuilder) {
        var banda = new Banda(dados);
        repository.save(banda);

        var uri = uriBuilder.path("/bandas/{id}")
                .buildAndExpand(banda.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoBanda(banda));
    }

    @PostMapping("/avaliacao/{id}")
    @Transactional
    public ResponseEntity AvaliarBanda(@PathVariable Long id, @RequestBody @Valid 
                                           DadosAvaliacaoBanda dados) {
        
        var banda = repository.findById(id)
                .orElseThrow(()
                        -> new ResourceNotFoundException
                        ("Banda não encontrada"));
        // precisamos passar o id da banda, se não encontrar, retorna o erro falando que não foi encontrada.
        
        var avaliacao = new AvaliacaoDeBanda();
        avaliacao.setNota(dados.nota());
        banda.adicionarAvaliacao(avaliacao);
        repository.save(banda);

        return ResponseEntity.ok(new DadosDetalhamentoBanda(banda));
    }


    @PutMapping
    @Transactional
    public ResponseEntity atualizar(@RequestBody DadosAtualizarBanda dados) {
        var banda = repository.getReferenceById(dados.id());
        banda.atualizarBanda(dados);

        return ResponseEntity.ok(new DadosDetalhamentoBanda(banda));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity excluir(@PathVariable Long id) {
        var banda = repository.getReferenceById(id);

        banda.excluirBanda();

        return ResponseEntity.noContent().build();
    }

}
