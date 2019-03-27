package com.example.andamas.andamas.modelo;

import java.sql.Timestamp;

public class Incidente {

    private int id;
    private int atendente;
    private int cliente;
    private String descricao;
    private String status;
    private Timestamp creation_time;

    public Incidente() {

    }

    public Incidente(int id, int atendente, int cliente, String descricao, String status, Timestamp creation_time) {
        this.id = id;
        this.atendente = atendente;
        this.cliente = cliente;
        this.descricao = descricao;
        this.status = status;
        this.creation_time = creation_time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAtendente() {
        return atendente;
    }

    public void setAtendente(int atendente) {
        this.atendente = atendente;
    }

    public int getCliente() {
        return cliente;
    }

    public void setCliente(int cliente) {
        this.cliente = cliente;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreation_time() {
        return creation_time;
    }

    public void setCreation_time(Timestamp creation_time) {
        this.creation_time = creation_time;
    }
}
