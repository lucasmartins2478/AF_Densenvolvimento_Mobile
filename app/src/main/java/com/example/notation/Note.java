package com.example.notation;

public class Note {
    private String id;

    private String tarefa;
    private String descricao;
    private boolean concluida;

    private String prioridade;


    public Note(String tarefa, String descricao, boolean concluida, String prioridade) {
        this.tarefa = tarefa;
        this.descricao = descricao;
        this.concluida = concluida;
        this.prioridade = prioridade;
    }
    public Note(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getTarefa() {
        return tarefa;
    }

    public void setTarefa(String tarefa) {
        this.tarefa = tarefa;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }
}
