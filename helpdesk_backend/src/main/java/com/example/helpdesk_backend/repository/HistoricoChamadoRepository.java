package com.example.helpdesk_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.helpdesk_backend.model.HistoricoChamado;

public interface HistoricoChamadoRepository extends JpaRepository<HistoricoChamado, Long> {

    /**
     * Trilha em ordem cronologica.
     *
     * O desempate por id nao e enfeite: uma unica acao pode gravar dois eventos na mesma
     * transacao (assumir com relato grava ATRIBUICAO e ANOTACAO) e os dois saem com
     * praticamente o mesmo LocalDateTime. Sem o desempate a ordem entre eles fica a
     * criterio do banco, e o cliente monta a linha do tempo ao contrario.
     */
    List<HistoricoChamado> findByChamadoIdOrderByDataEventoAscIdAsc(Long chamadoId);
}
