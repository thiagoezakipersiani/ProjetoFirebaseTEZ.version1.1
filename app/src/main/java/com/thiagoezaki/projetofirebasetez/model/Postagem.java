package com.thiagoezaki.projetofirebasetez.model;

import com.google.firebase.database.DatabaseReference;
import com.thiagoezaki.projetofirebasetez.helper.ConfiguracaoFireBase;

import java.io.Serializable;

public class Postagem implements Serializable {

    private String id;
    private String caminhoPostagem;
    private String tipoPostagem;
    private String descricao;
    private String nomeMusica;
    private String idUsuario;

    public Postagem() {
        DatabaseReference firebaseRef= ConfiguracaoFireBase.getFireBase();
        DatabaseReference postagemRef=firebaseRef.child("postagens");
        String idPostagem=postagemRef.push().getKey();
        setId(idPostagem);
    }

    public boolean salvar(){
        DatabaseReference firebaseRef= ConfiguracaoFireBase.getFireBase();
        DatabaseReference postagemRef= firebaseRef
                .child("postagens")
                .child(getId());
        postagemRef.setValue(this);
        return true;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaminhoPostagem() {
        return caminhoPostagem;
    }

    public void setCaminhoPostagem(String caminhoPostagem) {
        this.caminhoPostagem = caminhoPostagem;
    }



    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipoPostagem() {
        return tipoPostagem;
    }

    public void setTipoPostagem(String tipoPostagem) {
        this.tipoPostagem = tipoPostagem;
    }

    public String getNomeMusica() {
        return nomeMusica;
    }

    public void setNomeMusica(String nomeMusica) {
        this.nomeMusica = nomeMusica;
    }


    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

}
