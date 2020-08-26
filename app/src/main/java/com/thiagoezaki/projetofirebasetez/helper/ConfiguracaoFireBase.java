package com.thiagoezaki.projetofirebasetez.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFireBase {

    private static DatabaseReference referenciaFirebase;
    private static FirebaseAuth referenciaAutenticacao;
    private  static StorageReference storage;


    //retorna a referencia do database
    public static  DatabaseReference getFireBase(){
        if (referenciaFirebase==null){
            referenciaFirebase= FirebaseDatabase.getInstance().getReference();
        }
        return referenciaFirebase;
    }

    //retorna a instancia firebase
    public static FirebaseAuth getFirebaseAutenticacao(){
        if (referenciaAutenticacao==null){
            referenciaAutenticacao=FirebaseAuth.getInstance();

        }
        return referenciaAutenticacao;
    }

     //retorna a instancia do storage
    public static StorageReference getFirebaseStorage(){
        if (storage==null){
            storage= FirebaseStorage.getInstance().getReference();
        }
        return storage;
    }


}
