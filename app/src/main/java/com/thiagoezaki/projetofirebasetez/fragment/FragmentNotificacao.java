package com.thiagoezaki.projetofirebasetez.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.adapter.AdapterNotificacao;
import com.thiagoezaki.projetofirebasetez.model.Notificacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentNotificacao extends Fragment {

    private RecyclerView recyclerViewNotificacao;
    private AdapterNotificacao adapterNotificacao;
    private List<Notificacao> listNotificacao;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notificacao, container, false);

        Toolbar toolbar=view.findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Notificações");

        recyclerViewNotificacao=view.findViewById(R.id.recycle_view_notificacoes);
        recyclerViewNotificacao.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext());
        recyclerViewNotificacao.setLayoutManager(linearLayoutManager);
        listNotificacao = new ArrayList<>();
        adapterNotificacao= new AdapterNotificacao(getContext(),listNotificacao);
        recyclerViewNotificacao.setAdapter(adapterNotificacao);

        preparaNotificacao();

      return view;
    }

    private void preparaNotificacao() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("notificacoes").child(firebaseUser.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
             @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 listNotificacao.clear();
                 for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Notificacao notificacao= snapshot.getValue(Notificacao.class);
                    listNotificacao.add(notificacao);
                }
                Collections.reverse(listNotificacao);
                adapterNotificacao.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}