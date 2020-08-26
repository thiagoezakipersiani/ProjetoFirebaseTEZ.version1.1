package com.thiagoezaki.projetofirebasetez.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.adapter.AdapterPesquisa;
import com.thiagoezaki.projetofirebasetez.helper.ConfiguracaoFireBase;
import com.thiagoezaki.projetofirebasetez.helper.UsuarioFirebase;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.util.ArrayList;
import java.util.List;


public class FragmentPesquisa extends Fragment {

    private SearchView searchView;
    private RecyclerView recyclerViewPesquisaUsuarios;
    private List<Usuario> listaUsuarios;
    private DatabaseReference databaseUsuarioRef;
    private AdapterPesquisa adapterPesquisa;
    private String idUsuarioLogado;

    public FragmentPesquisa() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pesquisa, container, false);

        searchView = view.findViewById(R.id.searchViewPesquisar);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
        recyclerViewPesquisaUsuarios = view.findViewById(R.id.recyclerViewPesquisaUsuarios);

        //Configurações iniciais
        listaUsuarios = new ArrayList<>();
        databaseUsuarioRef = ConfiguracaoFireBase.getFireBase().child("usuarios");
        idUsuarioLogado= UsuarioFirebase.getIdentificadorUsuario();
        //configuração RecyclerView
        recyclerViewPesquisaUsuarios.setHasFixedSize(true);
        recyclerViewPesquisaUsuarios.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapterPesquisa = new AdapterPesquisa(listaUsuarios, getActivity(),true);
        recyclerViewPesquisaUsuarios.setAdapter(adapterPesquisa);


        //Configurar evento de clique
//        recyclerViewPesquisaUsuarios.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(),
////                recyclerViewPesquisaUsuarios, new RecyclerItemClickListener.OnItemClickListener() {
////            @Override
////            public void onItemClick(View view, int position) {
//////                    Usuario usuarioSelecionado= listaUsuarios.get(position);
//////                Intent intent= new Intent(getActivity(), FragmentPerfilAmigo.class);
//////                intent.putExtra("usuarioSelecionado", usuarioSelecionado);
//////                startActivity(intent);
////
////            }
////
////            @Override
////            public void onLongItemClick(View view, int position) {
////
////            }
////
////            @Override
////            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
////
////            }
////        }));

        //configurar searchView
        searchView.setQueryHint("Buscar usuários");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String nextText) {
                String textoDigitado = nextText.toUpperCase();
                pesquisarUsuarios(textoDigitado);
                return true;
            }
        });
        return view;
    }

    private void pesquisarUsuarios(String texto) {
        //limpar lista
        listaUsuarios.clear();

        //pesquisar os usuários caso tenha algum texto ao pesquisar
        if (texto.length() > 0) {

            Query query = databaseUsuarioRef.orderByChild("apelidoMaiusculo").startAt(texto).endAt(texto + "\uf8ff");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //limpar lista
                    listaUsuarios.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        //Remover da lista o usuário logado
                        Usuario usuario=ds.getValue(Usuario.class);
                        if (idUsuarioLogado.equals(usuario.getId()))
                            continue;
                            listaUsuarios.add(usuario);
                    }


                    adapterPesquisa.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }



}