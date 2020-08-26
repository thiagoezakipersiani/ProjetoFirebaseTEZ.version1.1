package com.thiagoezaki.projetofirebasetez.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jean.jcplayer.model.JcAudio;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.activity.ContaConfiguracaoActivity;
import com.thiagoezaki.projetofirebasetez.activity.FotoEscolhidaActivity;
import com.thiagoezaki.projetofirebasetez.activity.MusicaEscolhidaActivity;
import com.thiagoezaki.projetofirebasetez.activity.SeguidoresActivity;
import com.thiagoezaki.projetofirebasetez.adapter.AdapterMinhaFoto;
import com.thiagoezaki.projetofirebasetez.helper.ConfiguracaoFireBase;
import com.thiagoezaki.projetofirebasetez.helper.RecyclerItemClickListener;
import com.thiagoezaki.projetofirebasetez.helper.UsuarioFirebase;
import com.thiagoezaki.projetofirebasetez.model.Postagem;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FragmentPerfil extends Fragment {

    CircleImageView imagem_perfil;
    ImageButton button_fotos, button_musicas, button_videos;
    TextView numero_seguidores, numero_seguindo, apelido, postagem, seguidores, seguindo;
    Button button_edit_perfil;
    ProgressBar progressBar;
    private Usuario usuario;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    private String identificadorUsuario;
    private String tipoUser;
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


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        //Configurações Android Componentes
        button_fotos = view.findViewById(R.id.id_barra_foto);
        button_musicas = view.findViewById(R.id.id_barra_musica);
        numero_seguidores = view.findViewById(R.id.textViewNrSeguidores);
        numero_seguindo = view.findViewById(R.id.textViewNrSeguindo);
        button_edit_perfil = view.findViewById(R.id.buttonEditarPerfil);
        imagem_perfil = view.findViewById(R.id.imageViewPerfil);
        apelido = view.findViewById(R.id.textViewApelido);
        progressBar = view.findViewById(R.id.progressBarPerfil);
        postagem = view.findViewById(R.id.textViewPostagemPerfil);
        listViewMusica = view.findViewById(R.id.lista_musica);
        seguidores = view.findViewById(R.id.textViewSeguidores);
        seguindo = view.findViewById(R.id.textViewSeguindo);

        final Toolbar toolbar = view.findViewById(R.id.toolbarFeed);

        //Tela Editar Perfil
        button_edit_perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ContaConfiguracaoActivity.class);
                startActivity(intent);
            }
        });

        //trazer dados do usuário
        databaseRef = ConfiguracaoFireBase.getFireBase();

        DatabaseReference databaseReferenceApelido = databaseRef.child("usuarios").child(identificadorUsuario);

        databaseReferenceApelido.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usuario = dataSnapshot.getValue(Usuario.class);
                apelido.setText(usuario.getApelido());
                toolbar.setTitle(usuario.getNome());
                contarSeguidores();
                recuperarPostagens();
                //recuperar dados do usuário
                FirebaseUser userPerfil = UsuarioFirebase.getUsuarioAtual();
                Uri url = userPerfil.getPhotoUrl();
                if (url != null) {
                    Glide.with(FragmentPerfil.this).load(url).into(imagem_perfil);
                } else {
                    imagem_perfil.setImageResource(R.drawable.avatar);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }

        });


        recyclerViewListagemImagem = view.findViewById(R.id.recycler_imagem);
        recyclerViewListagemImagem.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new GridLayoutManager(getContext(), 4);
        recyclerViewListagemImagem.setLayoutManager(linearLayout);
        listaImagem = new ArrayList<>();
        listaMusica = new ArrayList<>();
        minhaFotoAdapter = new AdapterMinhaFoto(getContext(), listaImagem);
        recyclerViewListagemImagem.setAdapter(minhaFotoAdapter);

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


        //passa a url e o nome da musica seleciona para a tela de publicação
        listViewMusica.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Postagem postagemMusica = listaMusica.get(i);
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
                Intent intent = new Intent(getContext(), SeguidoresActivity.class);
                intent.putExtra("id", usuario.getId());
                intent.putExtra("titulo", "seguindo");
                startActivity(intent);
            }
        });

        seguidores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SeguidoresActivity.class);
                intent.putExtra("id", usuario.getId());
                intent.putExtra("titulo", "seguidores");
                startActivity(intent);
            }
        });

        return view;

    }


    public void contarSeguidores() {
        DatabaseReference Ref = ConfiguracaoFireBase.getFireBase().child("segue").child(identificadorUsuario).child("seguindo");

        Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numero_seguindo.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference RefSeguidor = ConfiguracaoFireBase.getFireBase().child("segue").child(identificadorUsuario).child("seguidores");

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
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Postagem postagemContador = snapshot.getValue(Postagem.class);
                    if (postagemContador.getIdUsuario().equals(identificadorUsuario)) {
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

                    if (postagem.getIdUsuario().equals(identificadorUsuario)) {
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

                    if (postagem.getIdUsuario().equals(identificadorUsuario)) {
                        if (postagem.getTipoPostagem().equals("2")) {
//                        jcAudios.add(JcAudio.createFromURL(postagem.getNomeMusica(), postagem.getCaminhoPostagem()));
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


}