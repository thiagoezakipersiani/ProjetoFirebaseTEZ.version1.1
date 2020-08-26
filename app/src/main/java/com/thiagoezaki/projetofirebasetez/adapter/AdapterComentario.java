package com.thiagoezaki.projetofirebasetez.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.thiagoezaki.projetofirebasetez.activity.MainActivity;
import com.thiagoezaki.projetofirebasetez.helper.UsuarioFirebase;
import com.thiagoezaki.projetofirebasetez.model.Comentario;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterComentario extends RecyclerView.Adapter<AdapterComentario.ViewHolder> {

    private Context context;
    private List<Comentario> listComentario;
    private FirebaseUser firebaseUser;

    public AdapterComentario(Context context, List<Comentario> listComentario) {
        this.context = context;
        this.listComentario = listComentario;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.comentarios_item,parent,false);
        return new AdapterComentario.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            firebaseUser = UsuarioFirebase.getUsuarioAtual();

            final Comentario comentario=listComentario.get(position);

            holder.comentario.setText(comentario.getComentario());
        informacoesUsuario(holder.imagemPerfil,holder.nomeUsuario,comentario.getPublicador());

        holder.comentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(context, MainActivity.class);
                intent.putExtra("publicadorId",comentario.getPublicador());
                context.startActivity(intent);
            }
        });

        holder.imagemPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(context, MainActivity.class);
                intent.putExtra("publicadorid",comentario.getPublicador());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listComentario.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imagemPerfil;
        TextView nomeUsuario,comentario;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imagemPerfil=itemView.findViewById(R.id.imagemPerfilComentarios);
            nomeUsuario=itemView.findViewById(R.id.nomeUsuario);
            comentario=itemView.findViewById(R.id.comentario);
        }
    }

    private void informacoesUsuario(final ImageView imageViewPerfil, final TextView apelidoUsuario, String publicadorId){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("usuarios").child(publicadorId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario= dataSnapshot.getValue(Usuario.class);
                Glide.with(context).load(usuario.getCaminhoFoto()).into(imageViewPerfil);
                apelidoUsuario.setText(usuario.getApelido().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
