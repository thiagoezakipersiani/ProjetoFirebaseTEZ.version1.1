package com.thiagoezaki.projetofirebasetez.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.activity.CommentActivity;
import com.thiagoezaki.projetofirebasetez.activity.FotoEscolhidaActivity;
import com.thiagoezaki.projetofirebasetez.activity.MusicaEscolhidaActivity;
import com.thiagoezaki.projetofirebasetez.fragment.FragmentPerfilAmigo;
import com.thiagoezaki.projetofirebasetez.helper.UsuarioFirebase;
import com.thiagoezaki.projetofirebasetez.model.Notificacao;
import com.thiagoezaki.projetofirebasetez.model.Postagem;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterNotificacao extends RecyclerView.Adapter<AdapterNotificacao.ViewHolder> {

    private Context context;
    private List<Notificacao> listNotificacao;
    FirebaseUser firebaseUser;
    Usuario usuarioPostagem;

    public AdapterNotificacao(Context context, List<Notificacao> listNotificacao) {
        this.context = context;
        this.listNotificacao = listNotificacao;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notificacao_item, parent, false);
        return new AdapterNotificacao.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  final ViewHolder holder, final int position) {

           final Notificacao notificacao = listNotificacao.get(position);
        //pegando usuário logado
        firebaseUser = UsuarioFirebase.getUsuarioAtual();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("usuarios").child(notificacao.getUsuarioId());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                String url = usuario.getCaminhoFoto();
                if (url != null) {
                    Glide.with(context).load(usuario.getCaminhoFoto()).into(holder.imagemPerfil);
                } else {
                    holder.imagemPerfil.setImageResource(R.drawable.avatar);
                }
                holder.apelido.setText(usuario.getApelido());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if ( notificacao.getIsPostagem().equals("1")) {

        DatabaseReference databaseReferencePostagem = FirebaseDatabase.getInstance().getReference("postagens").child(notificacao.getPostagemId());

            databaseReferencePostagem.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Postagem postagemRevisao = dataSnapshot.getValue(Postagem.class);
                if (postagemRevisao.getTipoPostagem().equals("1")) {
                    Glide.with(context).load(postagemRevisao.getCaminhoPostagem()).into(holder.imagemPostagem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        } else {
            holder.imagemPostagem.setVisibility(View.GONE);
        }

        DatabaseReference databaseReferenceUsuario = FirebaseDatabase.getInstance().getReference().child("usuarios").child(firebaseUser.getUid());

        databaseReferenceUsuario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usuarioPostagem = dataSnapshot.getValue(Usuario.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.texto.setText(notificacao.getTexto() );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notificacao.getIsPostagem().equals("1")) {
                    if (notificacao.getTipoPostagem().equals("2")){
                        Intent intent = new Intent(context, MusicaEscolhidaActivity.class);
                        intent.putExtra("musica", notificacao.getPostagemId());
                        intent.putExtra("usuario", usuarioPostagem);
                        context.startActivity(intent);
                    } else if (notificacao.getTipoPostagem().equals("1")) {
                        Intent intent = new Intent(context, FotoEscolhidaActivity.class);
                    intent.putExtra("postagem", notificacao.getPostagemId());
                    intent.putExtra("usuario", usuarioPostagem);
                    context.startActivity(intent);
                  }
                } else {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("perfilid", notificacao.getUsuarioId());
                    editor.apply();

                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.viewPager, new FragmentPerfilAmigo()).commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listNotificacao.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView apelido, texto;
        public ImageView imagemPerfil, imagemPostagem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            apelido = itemView.findViewById(R.id.apelidoNotificacao);
            imagemPerfil = itemView.findViewById(R.id.imagemPerfilNotificacao);
            imagemPostagem = itemView.findViewById(R.id.imagemPostagemNotificacao);
            texto = itemView.findViewById(R.id.textoNotificacao);
        }
    }

}
