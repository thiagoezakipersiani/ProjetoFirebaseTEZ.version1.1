package com.thiagoezaki.projetofirebasetez.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.adapter.AdapterPesquisa;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class SeguidoresActivity extends AppCompatActivity {

    String id;
    String titulo;
    List<String> listId;
    RecyclerView recyclerViewSeguidores;
    AdapterPesquisa adapterPesquisa;
    List<Usuario> listUsuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguidores);

        //Recebendo os dados dos seguidores
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        titulo = intent.getStringExtra("titulo");

        Toolbar toolbar = findViewById(R.id.toolbarSeguidores);
        toolbar.setTitle(titulo);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_branco);

        recyclerViewSeguidores = findViewById(R.id.recycler_seguidores);
        recyclerViewSeguidores.setHasFixedSize(true);
        recyclerViewSeguidores.setLayoutManager(new LinearLayoutManager(this));
        listUsuario = new ArrayList<>();
        adapterPesquisa = new AdapterPesquisa(listUsuario, this,false);
        recyclerViewSeguidores.setAdapter(adapterPesquisa);

        listId = new ArrayList<>();

        switch (titulo) {
            case "likes":
                getLikes();
                break;
            case "seguindo":
                getSeguindo();
                break;
            case "seguidores":
                getSeguidores();
                break;

        }

    }

    private void getLikes() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("likes").child(id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    listId.add(snapshot.getKey());
                }
                mostrarUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSeguindo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("segue").child(id).child("seguindo");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    listId.add(snapshot.getKey());
                }
                mostrarUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSeguidores() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("segue").child(id).child("seguidores");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    listId.add(snapshot.getKey());
                }
                mostrarUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void mostrarUsuarios() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listUsuario.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    for (String id : listId) {
                        if (usuario.getId().equals(id)) {
                            listUsuario.add(usuario);
                        }

                    }
                }
                adapterPesquisa.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }


}