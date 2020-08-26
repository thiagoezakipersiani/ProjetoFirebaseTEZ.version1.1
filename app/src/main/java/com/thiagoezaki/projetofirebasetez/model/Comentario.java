package com.thiagoezaki.projetofirebasetez.model;

public class Comentario {

    private String comentario;
    private String publicador;

    public Comentario(String comentario, String publicador) {
        this.comentario = comentario;
        this.publicador = publicador;
    }

    public Comentario() {
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getPublicador() {
        return publicador;
    }

    public void setPublicador(String publicador) {
        this.publicador = publicador;
    }
}
