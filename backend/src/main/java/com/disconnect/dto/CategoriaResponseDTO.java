package com.disconnect.dto;

import com.disconnect.domain.Modalidade;

public class CategoriaResponseDTO {

    private Integer id;
    private String nome;
    private String modalidade;

    public CategoriaResponseDTO() {
    }

    public CategoriaResponseDTO(Modalidade modalidade) {
        this.id = modalidade.getId();
        this.nome = modalidade.getCategoria();
        this.modalidade = modalidade.getNome();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }
}