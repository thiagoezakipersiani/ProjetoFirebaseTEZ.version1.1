package com.thiagoezaki.projetofirebasetez.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.thiagoezaki.projetofirebasetez.activity.MainActivity;
import com.thiagoezaki.projetofirebasetez.fragment.FragmentPerfilAmigo;
import com.thiagoezaki.projetofirebasetez.helper.ConfiguracaoFireBase;
import com.thiagoezaki.projetofirebasetez.helper.UsuarioFirebase;
import com.thiagoezaki.projetofirebasetez.model.Notificacao;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterPesquisa extends RecyclerView.Adapter<AdapterPesquisa.MyViewHolder> {

    private List <Usuario> listaUsuario;
    private Context context;
    private UsuarioFirebase firebaseUser;
    private boolean isFragment;

    public AdapterPesquisa(List<Usuario> l, Context c, boolean isFragment ) {
        this.listaUsuario = l;
        this.context = c;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista= LayoutInflater.from(parent.getContext()).inflate(R.layout.adpter_pesquisa_usuario,parent,false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

         firebaseUser.getUsuarioAtual();
        final Usuario usuario = listaUsuario.get(position);

        holder.btn_seguir.setVisibility(View.VISIBLE);

        holder.nome.setText(usuario.getNome());
        holder.apelido.setText(usuario.getApelido());

        if(usuario.getCaminhoFoto() !=null){
            Uri uri= Uri.parse(usuario.getCaminhoFoto());
            Glide.with(context).load(uri).into(holder.picture);
        }else {
            holder.picture.setImageResource(R.drawable.avatar);
        }

        seguindo(usuario.getId(),holder.btn_seguir);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFragment){
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("perfilid", usuario.getId());
                editor.apply();

                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.viewPager,new FragmentPerfilAmigo()).commit();
              } else {
                    Intent intent=  new Intent(context, MainActivity.class);
                    intent.putExtra("publicadorid", usuario.getId());
                    context.startActivity(intent);
                }
            }
        });

        holder.btn_seguir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.btn_seguir.getText().toString().equals("Seguir")){
                    ConfiguracaoFireBase.getFireBase()
                            .child("segue").child(firebaseUser.getIdentificadorUsuario())
                            .child("seguindo").child(usuario.getId()).setValue(true);
                    ConfiguracaoFireBase.getFireBase()
                            .child("segue").child(usuario.getId())
                            .child("seguidores").child(firebaseUser.getIdentificadorUsuario()).setValue(true);
                        adicionarNotificacoes(usuario.getId());
                } else {
                    ConfiguracaoFireBase.getFireBase()
                            .child("segue").child(firebaseUser.getIdentificadorUsuario())
                            .child("seguindo").child(usuario.getId()).removeValue();
                    ConfiguracaoFireBase.getFireBase()
                            .child("segue").child(usuario.getId())
                            .child("seguidores").child(firebaseUser.getIdentificadorUsuario()).removeValue();
                    removerNotificacoes(usuario.getId());
                 }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaUsuario.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
            CircleImageView picture;
            TextView apelido;
            TextView nome;
            Button btn_seguir;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                picture=itemView.findViewById(R.id.imagemPerfilPostagem);
                apelido=itemView.findViewById(R.id.textViewApelidoPesquisa);
                nome=itemView.findViewById(R.id.textViewNomePesquisa);
                btn_seguir=itemView.findViewById(R.id.buttonSeguirPesquisa);
            }
        }

    public void seguindo(final String usuarioId, final Button button){
        DatabaseReference firebaseRef =FirebaseDatabase.getInstance().getReference()
        .child("segue").child(firebaseUser.getIdentificadorUsuario())
        .child("seguindo");

        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(usuarioId).exists()){
                        button.setText("Seguindo");
                } else {
                       button.setText("Seguir");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void adicionarNotificacoes(String usuarioId){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("notificacoes").child(usuarioId);
        FirebaseUser usuarioFirebase = FirebaseAuth.getInstance().getCurrentUser();
        HashMap<String,Object> hashMap= new HashMap();
        hashMap.put("UsuarioId",usuarioFirebase.getUid());
        hashMap.put("texto","Começou a seguir você");
        hashMap.put("postagemId","");
        hashMap.put("isPostagem", "0");
        hashMap.put("tipoPostagem",null);
        databaseReference.push().setValue(hashMap);
    }

    private void removerNotificacoes(String usuarioId){
        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("notificacoes").child(usuarioId);
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
