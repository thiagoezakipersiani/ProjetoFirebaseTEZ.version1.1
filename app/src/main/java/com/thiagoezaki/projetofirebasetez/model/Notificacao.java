package com.thiagoezaki.projetofirebasetez.model;


public class Notificacao {

    private String UsuarioId;
    private String texto;
    private String postagemId;
    private String isPostagem;
    private String tipoPostagem;

    public Notificacao(String usuarioId, String texto, String postagemId, String isPostagem, String tipoPostagem) {
        UsuarioId = usuarioId;
        this.texto = texto;
        this.postagemId = postagemId;
        this.isPostagem = isPostagem;
        this.tipoPostagem = tipoPostagem;
    }

    public Notificacao() {
    }

    public String getUsuarioId() {
        return UsuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        UsuarioId = usuarioId;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getPostagemId() {
        return postagemId;
    }

    public void setPostagemId(String postagemId) {
        this.postagemId = postagemId;
    }

    public String getIsPostagem() {
        return isPostagem;
    }

    public void setIsPostagem(String isPostagem) {
        this.isPostagem = isPostagem;
    }

    public String getTipoPostagem() {
        return tipoPostagem;
    }

    public void setTipoPostagem(String tipoPostagem) {
        this.tipoPostagem = tipoPostagem;
    }
}
