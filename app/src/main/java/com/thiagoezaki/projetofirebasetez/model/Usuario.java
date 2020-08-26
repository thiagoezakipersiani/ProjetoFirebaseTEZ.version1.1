package com.thiagoezaki.projetofirebasetez.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.thiagoezaki.projetofirebasetez.helper.ConfiguracaoFireBase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Usuario implements Serializable {

    private String id, nome, email, senha, celular, perfilUsuario, caminhoFoto, apelido, apelidoMaiusculo;
    private int postagem = 0;

    public Usuario() {

    }

    public void salvar() {
        DatabaseReference firebaseRef = ConfiguracaoFireBase.getFireBase();
        DatabaseReference usuariosRef = firebaseRef.child("usuarios").child(getId());
        usuariosRef.setValue(this);
    }

    public void atualizar() {
        DatabaseReference firebaseRef = ConfiguracaoFireBase.getFireBase();
        Map objeto= new HashMap();
        objeto.put("/usuarios/" + getId() + "/nome",getNome());
        objeto.put("/usuarios/" + getId() + "/apelido",getApelido());
        objeto.put("/usuarios/" + getId() + "/apelidoMaiusculo",getApelidoMaiusculo());
        objeto.put("/usuarios/" + getId() + "/celular",getCelular());
        objeto.put("/usuarios/" + getId() + "/caminhoFoto",getCaminhoFoto());
        objeto.put("/usuarios/" + getId() + "/perfilUsuario",getPerfilUsuario());

//        DatabaseReference usuariosRef = firebaseRef.child("usuarios").child(getId());
//        Map<String, Object> valoresUsuario = converterParaMap();
        firebaseRef.updateChildren(objeto);
    }

    public Map<String, Object> converterParaMap() {
        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("email", getEmail());
        usuarioMap.put("apelido", getApelido());
        usuarioMap.put("celular", getCelular());
        usuarioMap.put("nome", getNome());
        usuarioMap.put("id", getId());
        usuarioMap.put("caminhoFoto", getCaminhoFoto());
        usuarioMap.put("perfilUsuario", getPerfilUsuario());
        usuarioMap.put("apelidoMaiusculo", getApelido().toUpperCase());
        usuarioMap.put("postagem", getPostagem());

        return usuarioMap;
    }

    public void consultar() {
        DatabaseReference firebaseRef = ConfiguracaoFireBase.getFireBase();
        DatabaseReference usuariosRef = firebaseRef.child("usuarios").child(apelido);
    }


    public int getPostagem() {
        return postagem;
    }

    public void setPostagem(int postagem) {
        this.postagem = postagem;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {

        this.apelido = apelido;
        this.apelidoMaiusculo = apelido.toUpperCase();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getPerfilUsuario() {
        return perfilUsuario;
    }

    public void setPerfilUsuario(String perfilUsuario) {
        this.perfilUsuario = perfilUsuario;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }

    public String getApelidoMaiusculo() {
        return apelidoMaiusculo;
    }

    public void setApelidoMaiusculo(String apelidoMaiusculo) {
        this.apelidoMaiusculo = apelidoMaiusculo;
    }
}
