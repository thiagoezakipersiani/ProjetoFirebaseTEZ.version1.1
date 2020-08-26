package com.thiagoezaki.projetofirebasetez.helper;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import androidx.annotation.NonNull;

public class UsuarioFirebase {


    public static FirebaseUser getUsuarioAtual() {
        FirebaseAuth usuario = ConfiguracaoFireBase.getFirebaseAutenticacao();
        return usuario.getCurrentUser();
    }

    public static void atualizarNomeUsuario(String nome) {
        try {

            //identificar usuário logado no aplicativo
            FirebaseUser userLogado = getUsuarioAtual();

            //configurar objeto de alteração do perfil
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(nome).build();

            userLogado.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Log.d("Perfil", "Erro ao atualizar o nome do perfil");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void atualizarFotoUsuario(Uri url) {
        try {

            //identificar usuário logado no aplicativo
            FirebaseUser userLogado = getUsuarioAtual();

            //configurar objeto de alteração do perfil
            UserProfileChangeRequest profile = new UserProfileChangeRequest.
                    Builder().
                    setPhotoUri(url).
                    build();

            userLogado.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Log.d("Perfil", "Erro ao atualizar a foto de perfil");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Usuario getDadosUsuarioLogado() {

        FirebaseUser firebaseUser = getUsuarioAtual();

        Usuario usuario = new Usuario();
        usuario.setId(firebaseUser.getUid());
        usuario.setEmail(firebaseUser.getEmail());
        usuario.setNome(firebaseUser.getDisplayName());

        if (firebaseUser.getPhotoUrl() == null) {
            usuario.setCaminhoFoto("");
        } else {
            usuario.setCaminhoFoto(firebaseUser.getPhotoUrl().toString());
        }

        return usuario;
    }

    public static String getIdentificadorUsuario() {
        return getUsuarioAtual().getUid();
    }
}
