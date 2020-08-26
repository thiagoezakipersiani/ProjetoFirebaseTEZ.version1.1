package com.thiagoezaki.projetofirebasetez.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.helper.UsuarioFirebase;
import com.thiagoezaki.projetofirebasetez.model.Postagem;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.util.HashMap;

import static java.security.AccessController.getContext;

public class FotoEscolhidaActivity extends AppCompatActivity {

    private TextView comentarios, descricao, nomePerfil, curtidas;
    private ImageView imageViewPostagem,imageViewCurtida,imageViewComentario;
    private CircleImageView imagemPerfil;
    String nomePerfilToolbar;
    FirebaseUser firebaseUser;
     Postagem postagem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_escolhida);

        iniciarComponentes();

       firebaseUser = UsuarioFirebase.getUsuarioAtual();

        Intent intent = getIntent();
        String postagemId =  intent.getStringExtra("postagem");
        Usuario usuario = (Usuario) intent.getSerializableExtra("usuario");


        DatabaseReference databaseReferencePostagem = FirebaseDatabase.getInstance().getReference("postagens").child(postagemId);

        databaseReferencePostagem.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 postagem = dataSnapshot.getValue(Postagem.class);
                //trazer os dados da postagem
                Uri UriPostagem = Uri.parse(postagem.getCaminhoPostagem());
                Glide.with(FotoEscolhidaActivity.this).
                        load(UriPostagem).into(imageViewPostagem);

                if(postagem.getDescricao().equals("")){
                    descricao.setVisibility(View.GONE);
                } else {
                    descricao.setText(postagem.getDescricao());
                }
             }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //dados do usuário
        Uri url = Uri.parse(usuario.getCaminhoFoto());
        if (url != null)  {
            Glide.with(FotoEscolhidaActivity.this).
                    load(url).into(imagemPerfil);
        } else {
            imagemPerfil.setImageResource(R.drawable.avatar);
        }
        nomePerfil.setText(usuario.getApelido().toLowerCase());
        nomePerfilToolbar = usuario.getNome().toUpperCase();


        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle(nomePerfilToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_branco);

        like(postagemId, imageViewCurtida);
        nrLike(curtidas, postagemId);

        imageViewCurtida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageViewCurtida.getTag().equals("like")) {
                        FirebaseDatabase.getInstance().getReference().child("likes").child(postagem.getId()).child(firebaseUser.getUid()).setValue(true);
                        adicionarNotificacoes(postagem.getIdUsuario(),postagem.getId());
                    } else {
                    FirebaseDatabase.getInstance().getReference().child("likes").child(postagem.getId()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        pegaComentarios(postagemId,comentarios);

        imageViewComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FotoEscolhidaActivity.this, CommentActivity.class);
                intent.putExtra("idPostagem",postagem.getId());
                intent.putExtra("idUsuario", postagem.getIdUsuario() );
                startActivity(intent);
            }
        });

        comentarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FotoEscolhidaActivity.this, CommentActivity.class);
                intent.putExtra("idPostagem",postagem.getId());
                intent.putExtra("idUsuario", postagem.getIdUsuario() );
                startActivity(intent);
            }
        });

        curtidas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(FotoEscolhidaActivity.this, SeguidoresActivity.class);
                intent.putExtra("id", postagem.getId());
                intent.putExtra("titulo", "likes");
                startActivity(intent);
            }
        });

    }


    public void iniciarComponentes() {

        comentarios = findViewById(R.id.textViewComentariosPostagemEscolhida);
        descricao = findViewById(R.id.textViewDescricaoPostagemEscolhida);
        nomePerfil = findViewById(R.id.textViewApelidoPostagemEscolhida);
        curtidas = findViewById(R.id.textViewCurtidasPostagemEscolhida);
        imageViewPostagem = findViewById(R.id.imageViewPostagemEscolhida);
        imagemPerfil = findViewById(R.id.imagemPerfilPostagemEscolhida);
        imageViewComentario= findViewById(R.id.imageViewComentario);
        imageViewCurtida = findViewById(R.id.imageViewLike);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    private void like(String postagemId, final ImageView imagemView) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("likes").child(postagemId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    imagemView.setImageResource(R.drawable.ic_likes_feitos_foreground);
                    imagemView.setTag("liked");
                } else {
                    imagemView.setImageResource(R.drawable.ic_like_foreground);
                    imagemView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void nrLike(final TextView like, String postagemId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("likes").child(postagemId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                like.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void pegaComentarios(String postagemId, final TextView comentarios){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("comentarios").child(postagemId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comentarios.setText("Ver " + dataSnapshot.getChildrenCount() + " comentários" );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void adicionarNotificacoes(String usuarioId,String postagemId){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("notificacoes").child(usuarioId);

        HashMap<String,Object> hashMap= new HashMap();
        hashMap.put("UsuarioId",firebaseUser.getUid());
        hashMap.put("texto","Gostou da sua postagem");
        hashMap.put("postagemId",postagemId);
        hashMap.put("isPostagem","1");
        hashMap.put("tipoPostagem","1");
        databaseReference.push().setValue(hashMap);
    }


}