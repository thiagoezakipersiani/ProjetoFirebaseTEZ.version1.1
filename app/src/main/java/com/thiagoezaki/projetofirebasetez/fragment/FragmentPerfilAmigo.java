package com.thiagoezaki.projetofirebasetez.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jean.jcplayer.model.JcAudio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.activity.ContaConfiguracaoActivity;
import com.thiagoezaki.projetofirebasetez.activity.FiltroActivity;
import com.thiagoezaki.projetofirebasetez.activity.FotoEscolhidaActivity;
import com.thiagoezaki.projetofirebasetez.activity.MusicaEscolhidaActivity;
import com.thiagoezaki.projetofirebasetez.activity.SeguidoresActivity;
import com.thiagoezaki.projetofirebasetez.adapter.AdapterMinhaFoto;
import com.thiagoezaki.projetofirebasetez.helper.ConfiguracaoFireBase;
import com.thiagoezaki.projetofirebasetez.helper.RecyclerItemClickListener;
import com.thiagoezaki.projetofirebasetez.helper.UsuarioFirebase;
import com.thiagoezaki.projetofirebasetez.model.Notificacao;
import com.thiagoezaki.projetofirebasetez.model.Postagem;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FragmentPerfilAmigo extends Fragment {

    private CircleImageView imageViewAmigo;
    private ImageButton button_fotos, button_musicas;
    private TextView numero_seguidores, numero_seguindo, apelido, postagem, seguidores,seguindo;
    private Button btnSeguir;
    private UsuarioFirebase firebaseUser;
    private String idUsuarioPerfil;
    private DatabaseReference firebaseRef;
    private RecyclerView recyclerViewListagemImagem;
    private List<Postagem> listaImagem;
    private AdapterMinhaFoto minhaFotoAdapter;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();
    ListView listViewMusica;
    ArrayList<String> arrayListNomeMusica = new ArrayList<>();
    ArrayList<String> arrayListUrlMusica = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    String nomeMusicaEscolhida;
    String urlMusicaEscolhida;
    List<Postagem> listaMusica;
    Usuario usuario;


    public FragmentPerfilAmigo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil_amigo, container, false);
        firebaseUser.getUsuarioAtual();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        idUsuarioPerfil = prefs.getString("perfilid", "apelido");

        imageViewAmigo = view.findViewById(R.id.imageViewAmigoPerfil);
        apelido = view.findViewById(R.id.textViewApelidoAmigo);
        btnSeguir = view.findViewById(R.id.btnSeguir);
        numero_seguidores = view.findViewById(R.id.textViewNrSeguidoresAmigo);
        numero_seguindo = view.findViewById(R.id.textViewNrSeguindoAmigo);
        postagem = view.findViewById(R.id.textViewPostagem);
        button_fotos = view.findViewById(R.id.id_barra_foto);
        button_musicas = view.findViewById(R.id.id_barra_musica);
        listViewMusica = view.findViewById(R.id.lista_musica);
        seguidores = view.findViewById(R.id.textViewAmigoSeguidores);
        seguindo  = view.findViewById(R.id.textViewAmigoSeguindo);
        final Toolbar toolbar = view.findViewById(R.id.toolbarPerfilAmigo);
        btnSeguir.setText("Carregando");

        recyclerViewListagemImagem = view.findViewById(R.id.recycler_imagem);
        recyclerViewListagemImagem.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new GridLayoutManager(getContext(), 4);
        recyclerViewListagemImagem.setLayoutManager(linearLayout);
        listaImagem = new ArrayList<>();
        listaMusica = new ArrayList<>();
        minhaFotoAdapter = new AdapterMinhaFoto(getContext(), listaImagem);
        recyclerViewListagemImagem.setAdapter(minhaFotoAdapter);


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("usuarios").child(idUsuarioPerfil);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }
                usuario = dataSnapshot.getValue(Usuario.class);
                Glide.with(getContext()).load(usuario.getCaminhoFoto()).into(imageViewAmigo);
                apelido.setText(usuario.getApelido().toLowerCase());
                recuperarPostagens();
                toolbar.setTitle(usuario.getNome());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        verificarSeguindo();
        contarSeguidores();
        minhaFotos();
        button_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewListagemImagem.setVisibility(View.VISIBLE);
                listViewMusica.setVisibility(View.GONE);
                minhaFotos();
            }
        });

        button_musicas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewListagemImagem.setVisibility(View.GONE);
                listViewMusica.setVisibility(View.VISIBLE);
                minhasMusicas();
            }

        });
        btnSeguir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String btn = btnSeguir.getText().toString();

                if (btn.equals("Seguir")) {
                    ConfiguracaoFireBase.getFireBase().child("segue").child(firebaseUser.getIdentificadorUsuario()).child("seguindo").child(idUsuarioPerfil).setValue(true);
                    ConfiguracaoFireBase.getFireBase().child("segue").child(idUsuarioPerfil).child("seguidores").child(firebaseUser.getIdentificadorUsuario()).setValue(true);
                    adicionarNotificacoes();
                } else if (btn.equals("Seguindo")) {
                    ConfiguracaoFireBase.getFireBase().child("segue").child(firebaseUser.getIdentificadorUsuario()).child("seguindo").child(idUsuarioPerfil).removeValue();
                    ConfiguracaoFireBase.getFireBase().child("segue").child(idUsuarioPerfil).child("seguidores").child(firebaseUser.getIdentificadorUsuario()).removeValue();
                    removerNotificacoes();
                }
            }
        });

        //passa a url e o nome da musica seleciona para a tela de publicação
        listViewMusica.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Postagem postagemMusica = listaMusica.get(i);
                //      callback.onArticleSelected(nomeMusicaEscolhida,urlMusicaEscolhida);
                Intent intent = new Intent(getActivity(), MusicaEscolhidaActivity.class);
                intent.putExtra("musica", postagemMusica.getId());
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            }
        });

        //passa a url e o nome da musica seleciona para a tela de publicação
        recyclerViewListagemImagem.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerViewListagemImagem, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Postagem postagem = listaImagem.get(position);
                Intent intent = new Intent(getActivity(), FotoEscolhidaActivity.class);
                intent.putExtra("postagem", postagem.getId());
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        }));

        seguindo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(getContext(), SeguidoresActivity.class);
                intent.putExtra("id",usuario.getId());
                intent.putExtra("titulo", "seguindo");
                startActivity(intent);
            }
        });

        seguidores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(getContext(), SeguidoresActivity.class);
                intent.putExtra("id",usuario.getId());
                intent.putExtra("titulo", "seguidores");
                startActivity(intent);
            }
        });

        return view;
    }


    public void verificarSeguindo() {
        DatabaseReference databaseReference = ConfiguracaoFireBase.getFireBase().child("segue").child(firebaseUser.getIdentificadorUsuario()).child("seguindo");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(idUsuarioPerfil).exists()) {
                    btnSeguir.setText("Seguindo");
                } else {
                    btnSeguir.setText("Seguir");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void contarSeguidores() {
        DatabaseReference Ref = ConfiguracaoFireBase.getFireBase().child("segue").child(idUsuarioPerfil).child("seguindo");

        Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numero_seguindo.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference RefSeguidor = ConfiguracaoFireBase.getFireBase().child("segue").child(idUsuarioPerfil).child("seguidores");

        RefSeguidor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numero_seguidores.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void recuperarPostagens() {
        DatabaseReference Ref = ConfiguracaoFireBase.getFireBase().child("postagens");

        Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i= 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Postagem postagemContador = snapshot.getValue(Postagem.class);
                   if (postagemContador.getIdUsuario().equals(idUsuarioPerfil)) {
                      i++;
                    }
                    Collections.reverse(listaImagem);
                    minhaFotoAdapter.notifyDataSetChanged();
                }
                postagem.setText("Postagens: " + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void minhaFotos() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("postagens");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaImagem.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Postagem postagem = snapshot.getValue(Postagem.class);

                    if (postagem.getIdUsuario().equals(idUsuarioPerfil)) {
                        if (postagem.getTipoPostagem().equals("1")) {
                            listaImagem.add(postagem);
                        }
                    }
                    Collections.reverse(listaImagem);
                    minhaFotoAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void minhasMusicas() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("postagens");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayListUrlMusica.clear();
                arrayListNomeMusica.clear();
                listaMusica.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Postagem postagem = snapshot.getValue(Postagem.class);

                    if (postagem.getIdUsuario().equals(idUsuarioPerfil)) {
                        if (postagem.getTipoPostagem().equals("2")) {
                            listaMusica.add(postagem);
                            arrayListNomeMusica.add(postagem.getNomeMusica());
                            arrayListUrlMusica.add(postagem.getCaminhoPostagem());
                        }
                    }
                    arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1, arrayListNomeMusica) {

                        @NonNull
                        @Override
                        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView textView = (TextView) view.findViewById(android.R.id.text1);
                            textView.setSingleLine();
                            textView.setMaxLines(1);

                            return view;
                        }
                    };
                    listViewMusica.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void adicionarNotificacoes(){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("notificacoes").child(idUsuarioPerfil);
        FirebaseUser usuarioFirebase = FirebaseAuth.getInstance().getCurrentUser();
        HashMap<String,Object> hashMap= new HashMap();
        hashMap.put("UsuarioId",usuarioFirebase.getUid());
        hashMap.put("texto","Começou a seguir você");
        hashMap.put("postagemId","");
        hashMap.put("isPostagem", null);
        hashMap.put("tipoPostagem", null);
        databaseReference.push().setValue(hashMap);
    }


    private void removerNotificacoes(){
        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("notificacoes").child(idUsuarioPerfil);
        final FirebaseUser usuarioFirebase = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Notificacao notificacao= snapshot.getValue(Notificacao.class);
                    if (notificacao.getUsuarioId().equals(usuarioFirebase.getUid())){
                        if (notificacao.getTexto().equals("Começou a seguir você")){
                            databaseReference.removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}