package com.thiagoezaki.projetofirebasetez.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.adapter.AdapterComentario;
import com.thiagoezaki.projetofirebasetez.model.Comentario;
import com.thiagoezaki.projetofirebasetez.model.Postagem;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    EditText addComentario;
    ImageView imageViewPerfil;
    TextView textViewPostagem;
    String idPostagem, idUsuarioPublicador;
    FirebaseUser firebaseUser;
    private RecyclerView recyclerViewComentarios;
    private AdapterComentario adapterComentario;
    private List<Comentario> listComentario;
    private String tipoPostagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar toolbar = findViewById(R.id.toolbarComentario);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comentários");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        addComentario = findViewById(R.id.addComentario);
        textViewPostagem = findViewById(R.id.postagemComentario);
        imageViewPerfil = findViewById(R.id.imagemPerfil);

        recyclerViewComentarios = findViewById(R.id.recycler_comentarios);
        recyclerViewComentarios.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
        recyclerViewComentarios.setLayoutManager(linearLayoutManager);
        listComentario = new ArrayList<>();
        adapterComentario= new AdapterComentario(this,listComentario);
        recyclerViewComentarios.setAdapter(adapterComentario);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        idPostagem = intent.getStringExtra("idPostagem");
        idUsuarioPublicador = intent.getStringExtra("idUsuario");

        DatabaseReference databaseReferencePostagem = FirebaseDatabase.getInstance().getReference("postagens").child(idPostagem);

        databaseReferencePostagem.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Postagem postagemRevisao = dataSnapshot.getValue(Postagem.class);
                tipoPostagem= postagemRevisao.getTipoPostagem();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        textViewPostagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addComentario.getText().toString().equals("")) {
                    Toast.makeText(CommentActivity.this, "Você não pode enviar um comentário vazio", Toast.LENGTH_LONG).show();
                } else {
                    adicionarComentario();
                }
            }
        });

        pegarImagem();
        pegarComentarios();
    }

    private void adicionarComentario() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("comentarios").child(idPostagem);

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("comentario", addComentario.getText().toString());
        hashMap.put("publicador", firebaseUser.getUid());

        databaseReference.push().setValue(hashMap);
        addComentario.setText("");


        adicionarNotificacoes();

    }

    private void adicionarNotificacoes(){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("notificacoes").child(idUsuarioPublicador);

        HashMap<String,Object> hashMap= new HashMap();
        hashMap.put("UsuarioId",firebaseUser.getUid());
        hashMap.put("texto","Comentou em sua publicação" + addComentario.getText().toString());
        hashMap.put("postagemId",idPostagem);
        hashMap.put("isPostagem","1");
        hashMap.put("tipoPostagem",tipoPostagem);
        databaseReference.push().setValue(hashMap);


    }

    public void pegarImagem() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("usuarios").child(firebaseUser.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                Glide.with(getApplicationContext()).load(usuario.getCaminhoFoto()).into(imageViewPerfil);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void pegarComentarios(){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("comentarios").child(idPostagem);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listComentario.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comentario comentario = snapshot.getValue(Comentario.class);
                    listComentario.add(comentario);
                }
                adapterComentario.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}