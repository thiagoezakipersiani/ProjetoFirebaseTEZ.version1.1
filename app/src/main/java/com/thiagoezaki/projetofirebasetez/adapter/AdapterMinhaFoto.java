package com.thiagoezaki.projetofirebasetez.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.model.Postagem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterMinhaFoto extends RecyclerView.Adapter<AdapterMinhaFoto.ViewHolder> {

    private Context context;
    private List<Postagem> listPostagem;

    public AdapterMinhaFoto(Context context, List<Postagem> listPostagem) {
        this.context = context;
        this.listPostagem = listPostagem;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.feed_item_imagem,parent,false);
        return new AdapterMinhaFoto.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Postagem postagem=listPostagem.get(position);

            if (postagem.getTipoPostagem().equals("1")){
                Glide.with(context).load(postagem.getCaminhoPostagem()).into(holder.imagem_postagem);
            }
    }

    @Override
    public int getItemCount() {
        return listPostagem.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imagem_postagem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imagem_postagem=itemView.findViewById(R.id.imagemViewPublicacao);
        }
    }


}
