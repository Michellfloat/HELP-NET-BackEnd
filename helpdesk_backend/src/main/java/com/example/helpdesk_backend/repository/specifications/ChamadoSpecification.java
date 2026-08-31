package com.example.helpdesk_backend.repository.specifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.example.helpdesk_backend.model.Chamado;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.Setor;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.model.enums.Urgencia;

import jakarta.persistence.criteria.Predicate;

public class ChamadoSpecification {
    public static Specification<Chamado>comFiltrosEVisibilisade(
        StatusChamado status,
        Urgencia urgencia,
        Setor setor,
        NivelAtendente nivelExigido,
        Long solicitanteId,
        Long responsavelId,
        Usuario usuarioLogado
    ){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //1. Aplicação da Regra de Visibilidade por Perfil (Como no AnexoService)
            if (usuarioLogado.getPerfil() == Perfil.USUARIO) {
                //Usuário comum só visualiza os chamados onde ele é o solicitante

                predicates.add(cb.equal(root.get("solicitante").get("id"), usuarioLogado.getId()));
            }else if (usuarioLogado.getPerfil() == Perfil.ATENDENTE) {
                //Atendente só visualiza chamados cujo nível exigido seja <= ao seu nível de atendente.

                if (usuarioLogado.getNivelAntendente() != null) {
                    List<NivelAtendente> niveisPermitidos = Arrays.stream(NivelAtendente.values()).filter(n -> n.ordinal() <= usuarioLogado.getNivelAntendente().ordinal()).toList();

                    predicates.add(root.get("nivelExigido").in(niveisPermitidos));
                }else{
                    predicates.add(cb.isNull(root.get("nivelExigido")));
                }
            }
            //ADMIN possui visibilidade total (nenhum predicado restritivo de perfil é adicionado)

            //2. Filtros opcionais enviados na requisição
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (urgencia != null) {
                predicates.add(cb.equal(root.get("urgencia"), urgencia));
            }

            if (setor != null) {
                predicates.add(cb.equal(root.get("setor"), setor));
            }
            if (nivelExigido != null) {
                predicates.add(cb.equal(root.get("nivelExigido"), nivelExigido));
            }
            if (solicitanteId != null) {
                predicates.add(cb.equal(root.get("solicitante").get("id"), solicitanteId));
            }
            if (responsavelId != null) {
                predicates.add(cb.equal(root.get("responsavel").get("id"), responsavelId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
