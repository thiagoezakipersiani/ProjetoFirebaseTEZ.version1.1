package com.thiagoezaki.projetofirebasetez.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.activity.OpcoesActivity;
import com.thiagoezaki.projetofirebasetez.adapter.AdapterPostagem;
import com.thiagoezaki.projetofirebasetez.model.Postagem;

import java.util.ArrayList;
import java.util.List;

public class FragmentFeed extends Fragment {

    private RecyclerView recyclerView;
    private AdapterPostagem adapterPostagem;
    private List<Postagem> listPostagens;
    private List<String> listSeguindo;
    private ProgressBar progressBar;
    String nomePerfilToolbar;

    public FragmentFeed() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        recyclerView = view.findViewById(R.id.recycler_postagens_feed);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        Toolbar toolbar = view.findViewById(R.id.toolbarPrincipal);

        //for crate home button
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        toolbar.setTitle("T.PLAY");
        activity.setSupportActionBar(toolbar);


        recyclerView.setLayoutManager(linearLayoutManager);
        listPostagens = new ArrayList<>();
        adapterPostagem = new AdapterPostagem(getContext(), listPostagens);
        recyclerView.setAdapter(adapterPostagem);

        progressBar = view.findViewById(R.id.progressBarFeed);

        verificarSeguindo();

        return view;
    }

    public void verificarSeguindo() {
        listSeguindo = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("segue").
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("seguindo");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listSeguindo.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    listSeguindo.add(snapshot.getKey());
                    String id = snapshot.getKey();
                }
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                listSeguindo.add(firebaseUser.getUid());
                verificandoPublicacoes();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void verificandoPublicacoes() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("postagens");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listPostagens.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Postagem postagem = snapshot.getValue(Postagem.class);
                    for (String id : listSeguindo)  {
                        if (postagem.getIdUsuario().equals(id)) listPostagens.add(postagem);
                    }
                }
                adapterPostagem.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //inflater adicionar
        inflater.inflate(R.menu.menu_configuracoes, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_baseline_menu_branco:
                Intent intent = new Intent(getContext(), OpcoesActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}