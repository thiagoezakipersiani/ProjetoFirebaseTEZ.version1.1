package com.thiagoezaki.projetofirebasetez.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.fragment.FragmentPerfilAmigo;
import com.thiagoezaki.projetofirebasetez.helper.UsuarioFirebase;
import com.thiagoezaki.projetofirebasetez.model.Postagem;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.util.ArrayList;
import java.util.HashMap;

import static java.security.AccessController.getContext;

public class MusicaEscolhidaActivity extends AppCompatActivity {

    private JcPlayerView jcPlayerView;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();
    private TextView comentarios, descricao, nomePerfil, curtidas;
    private CircleImageView imagemPerfil;
    private ImageView imageViewCurtida, imageViewComentario;
    String nomePerfilToolbar;
    private Postagem postagem;
    FirebaseUser firebaseUser;
    String urlMusica;
    String nomeMusica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musica_escolhida);

        iniciarComponentes();
        Intent intent = getIntent();
        String postagemId = intent.getStringExtra("musica");
        Usuario usuario = (Usuario) intent.getSerializableExtra("usuario");

        DatabaseReference databaseReferencePostagem = FirebaseDatabase.getInstance().getReference("postagens").child(postagemId);

        databaseReferencePostagem.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postagem = dataSnapshot.getValue(Postagem.class);
                //trazer os dados da postagem
                descricao.setText(postagem.getDescricao());
                if (postagem.getDescricao().equals("")) {
                    descricao.setVisibility(View.GONE);
                } else {
                    descricao.setText(postagem.getDescricao());
                }
                preparaMusica(postagem.getNomeMusica(), postagem.getCaminhoPostagem());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //pegando usuário logado
        firebaseUser = UsuarioFirebase.getUsuarioAtual();

        //recuperarInformaçõesUsuario(IdUsuario);

        Uri url = Uri.parse(usuario.getCaminhoFoto());
        Glide.with(MusicaEscolhidaActivity.this).
                load(url).into(imagemPerfil);

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
                    adicionarNotificacoesMusica(postagem.getIdUsuario(), postagem.getId(), postagem);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("likes").child(postagem.getId()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        pegaComentarios(postagemId, comentarios);

        imageViewComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MusicaEscolhidaActivity.this, CommentActivity.class);
                intent.putExtra("idPostagem", postagem.getId());
                intent.putExtra("idUsuario", postagem.getIdUsuario());
                startActivity(intent);
            }
        });

        comentarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MusicaEscolhidaActivity.this, CommentActivity.class);
                intent.putExtra("idPostagem", postagem.getId());
                intent.putExtra("idUsuario", postagem.getIdUsuario());
                startActivity(intent);
            }
        });

        curtidas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MusicaEscolhidaActivity.this, SeguidoresActivity.class);
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
        imagemPerfil = findViewById(R.id.imagemPerfilPostagemEscolhida);
        jcPlayerView = findViewById(R.id.jcplayerMusicaEscolhida);
        imageViewComentario = findViewById(R.id.imageViewComentario);
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

    private void pegaComentarios(String postagemId, final TextView comentarios) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("comentarios").child(postagemId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comentarios.setText("Ver " + dataSnapshot.getChildrenCount() + " comentários");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void adicionarNotificacoesMusica(String usuarioId, String postagemId, Postagem postagem) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notificacoes").child(usuarioId);

        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("UsuarioId", firebaseUser.getUid());
        hashMap.put("texto", "Gostou da sua  música: " + postagem.getNomeMusica());
        hashMap.put("postagemId", postagemId);
        hashMap.put("isPostagem", "1");
        hashMap.put("tipoPostagem", "2");
        databaseReference.push().setValue(hashMap);
    }

    public void preparaMusica(String nomeMusica, String urlMusica) {

        jcAudios.clear();
        jcAudios.add(JcAudio.createFromURL(nomeMusica, urlMusica));
        jcPlayerView.initPlaylist(jcAudios, null);
        jcPlayerView.playAudio(jcAudios.get(0));
        jcPlayerView.createNotification();

    }

}