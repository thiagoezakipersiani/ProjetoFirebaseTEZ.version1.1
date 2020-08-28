package com.thiagoezaki.projetofirebasetez.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thiagoezaki.projetofirebasetez.activity.CommentActivity;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.activity.SeguidoresActivity;
import com.thiagoezaki.projetofirebasetez.fragment.FragmentPerfilAmigo;
import com.thiagoezaki.projetofirebasetez.helper.DoubleClickListener;
import com.thiagoezaki.projetofirebasetez.model.Postagem;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterPostagem extends RecyclerView.Adapter<AdapterPostagem.ViewHolder> {


    private Context context;
    private List<Postagem> listPostagem;
    private FirebaseUser firebaseUser;
    Handler seekHandler = new Handler();
    Runnable run;


    public AdapterPostagem(Context context, List<Postagem> listPostagem) {
        this.context = context;
        this.listPostagem = listPostagem;
    }


    @NonNull
    @Override
    public AdapterPostagem.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_lista_musica, parent, false);
        return new AdapterPostagem.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterPostagem.ViewHolder holder, final int position) {


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final Postagem postagem = listPostagem.get(position);


        if (postagem.getTipoPostagem().equals("1")) {
            holder.imageViewPostagem.setVisibility(View.VISIBLE);
            Glide.with(context).load(postagem.getCaminhoPostagem()).into(holder.imageViewPostagem);
            holder.imageViewPausa.setVisibility(View.GONE);
            holder.seekBar.setVisibility(View.GONE);
            holder.nomeMusica.setVisibility(View.GONE);
            holder.textTotalDuration.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);

        } else if  (postagem.getTipoPostagem().equals("2")) {

            final MediaPlayer mediaPlayer = new MediaPlayer();

            holder.nomeMusica.setText(postagem.getNomeMusica());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(postagem.getCaminhoPostagem());
                mediaPlayer.prepare();// might take long for buffering.
            } catch (IOException e) {
                e.printStackTrace();
            }



            holder.seekBar.setMax(mediaPlayer.getDuration());
            holder.seekBar.setTag(position);
            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mediaPlayer != null && fromUser) {
                        mediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });


            holder.textTotalDuration.setText("0:00/" + calculateDuration(mediaPlayer.getDuration()));

            holder.imageViewPausa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        holder.imageViewPausa.setImageResource(R.drawable.ic_baseline_pause_24);
                        run = new Runnable() {
                            @Override
                            public void run() {
                                // Updateing SeekBar every 100 miliseconds
                                holder.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                seekHandler.postDelayed(run, 100);
                                //For Showing time of audio(inside runnable)
                                int miliSeconds = mediaPlayer.getCurrentPosition();
                                if (miliSeconds != 0) {
                                    //if audio is playing, showing current time;
                                    long minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds);
                                    long seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds);
                                    if (minutes == 0) {
                                        holder.textTotalDuration.setText("0:" + seconds + "/" + calculateDuration(mediaPlayer.getDuration()));
                                    } else {
                                        if (seconds >= 60) {
                                            long sec = seconds - (minutes * 60);
                                            holder.textTotalDuration.setText(minutes + ":" + sec + "/" + calculateDuration(mediaPlayer.getDuration()));
                                        }
                                    }
                                } else {
                                    //Displaying total time if audio not playing
                                    int totalTime = mediaPlayer.getDuration();
                                    long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime);
                                    long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime);
                                    if (minutes == 0) {
                                        holder.textTotalDuration.setText("0:" + seconds);
                                    } else {
                                        if (seconds >= 60) {
                                            long sec = seconds - (minutes * 60);
                                            holder.textTotalDuration.setText(minutes + ":" + sec);
                                        }
                                    }
                                }
                            }

                        };
                        run.run();
                    } else {
                        mediaPlayer.pause();
                        holder.imageViewPausa.setImageResource(R.drawable.ic_baseline_play_24);
                    }
                }
            });
            holder.imageViewPostagem.setVisibility(View.GONE);
            holder.imageViewPausa.setVisibility(View.VISIBLE);
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.nomeMusica.setVisibility(View.VISIBLE);
            holder.textTotalDuration.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        }

        if (postagem.getDescricao().equals("")) {
            holder.descricao.setVisibility(View.GONE);
        } else {
            holder.descricao.setVisibility(View.VISIBLE);
            holder.descricao.setText(postagem.getDescricao());
        }

        holder.comentarios.setVisibility(View.VISIBLE);
        holder.curtidas.setVisibility(View.VISIBLE);

        informacoesPublicador(holder.imagemPerfil, holder.nomePerfil, postagem.getIdUsuario());

        like(postagem.getId(), holder.imageViewCurtida);
        nrLike(holder.curtidas, postagem.getId());

        holder.imageViewCurtida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.imageViewCurtida.getTag().equals("like")) {
                    if (postagem.getTipoPostagem().equals("1")){
                        FirebaseDatabase.getInstance().getReference().child("likes").child(postagem.getId()).child(firebaseUser.getUid()).setValue(true);
                        adicionarNotificacoes(postagem.getIdUsuario(),postagem.getId(),postagem.getTipoPostagem());
                    } else {
                        FirebaseDatabase.getInstance().getReference().child("likes").child(postagem.getId()).child(firebaseUser.getUid()).setValue(true);
                        adicionarNotificacoesMusica(postagem.getIdUsuario(),postagem.getId(),postagem);
                    }
                } else {
                    FirebaseDatabase.getInstance().getReference().child("likes").child(postagem.getId()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        pegaComentarios(postagem.getId(),holder.comentarios);

        holder.imageViewComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("idPostagem",postagem.getId());
                intent.putExtra("idUsuario", postagem.getIdUsuario() );
                context.startActivity(intent);
            }
        });

        holder.imagemPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("perfilid", postagem.getIdUsuario());
                editor.apply();

                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.viewPager,new FragmentPerfilAmigo()).commit();
            }
        });

        holder.nomePerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("perfilid", postagem.getIdUsuario());
                editor.apply();

                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.viewPager,new FragmentPerfilAmigo()).commit();
            }
        });

        holder.comentarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("idPostagem",postagem.getId());
                intent.putExtra("idUsuario", postagem.getIdUsuario() );
                context.startActivity(intent);
            }
        });

        holder.curtidas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(context, SeguidoresActivity.class);
                intent.putExtra("id", postagem.getId());
                intent.putExtra("titulo", "likes");
                context.startActivity(intent);
            }
        });

        holder.imageViewPostagem.setOnClickListener(new DoubleClickListener() {

            @Override
            public void onSingleClick(View v) {

            }

            @Override
            public void onDoubleClick(View v) {
                if (holder.imageViewCurtida.getTag().equals("like")) {
                    if (postagem.getTipoPostagem().equals("1")){
                        FirebaseDatabase.getInstance().getReference().child("likes").child(postagem.getId()).child(firebaseUser.getUid()).setValue(true);
                        adicionarNotificacoes(postagem.getIdUsuario(),postagem.getId(),postagem.getTipoPostagem());
                    } else {
                        FirebaseDatabase.getInstance().getReference().child("likes").child(postagem.getId()).child(firebaseUser.getUid()).setValue(true);
                        adicionarNotificacoesMusica(postagem.getIdUsuario(),postagem.getId(),postagem);
                    }
                } else {
                    FirebaseDatabase.getInstance().getReference().child("likes").child(postagem.getId()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listPostagem.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageViewPostagem, imageViewCurtida, imageViewComentario, imageViewPausa;

        private TextView comentarios, descricao, nomePerfil, curtidas, nomeMusica, textTotalDuration;

        private CircleImageView imagemPerfil;
        private SeekBar seekBar;
        private ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            comentarios = itemView.findViewById(R.id.textViewComentariosPostagem);
            descricao = itemView.findViewById(R.id.textViewDescricaoPostagem);
            nomePerfil = itemView.findViewById(R.id.textViewApelidoPostagem);
            curtidas = itemView.findViewById(R.id.textViewCurtidas);
            imagemPerfil = itemView.findViewById(R.id.imagemPerfilPostagem);
            imageViewComentario = itemView.findViewById(R.id.imageViewComentario);
            imageViewCurtida = itemView.findViewById(R.id.imageViewLike);
            imageViewPostagem = itemView.findViewById(R.id.imageViewPostagem);
            imageViewPausa = itemView.findViewById(R.id.imagemPlayPause);
            textTotalDuration = itemView.findViewById(R.id.textViewTotalDuracao);
            nomeMusica = itemView.findViewById(R.id.textViewNomeMusicaPostagem);
            seekBar = itemView.findViewById(R.id.seekBarMusica);
            progressBar = itemView.findViewById(R.id.progressBarPostagem);
        }
    }

    private void adicionarNotificacoes(String usuarioId,String postagemId, String tipoPostagem){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("notificacoes").child(usuarioId);

        HashMap<String,Object> hashMap= new HashMap();
        hashMap.put("UsuarioId",firebaseUser.getUid());
        hashMap.put("texto","Gostou da sua postagem");
        hashMap.put("postagemId",postagemId);
        hashMap.put("isPostagem","1");
        hashMap.put("tipoPostagem", "1");
        databaseReference.push().setValue(hashMap);
    }

    private void adicionarNotificacoesMusica(String usuarioId,String postagemId, Postagem postagem){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("notificacoes").child(usuarioId);

        HashMap<String,Object> hashMap= new HashMap();
        hashMap.put("UsuarioId",firebaseUser.getUid());
        hashMap.put("texto","Gostou da sua  música: " + postagem.getNomeMusica());
        hashMap.put("postagemId",postagemId);
        hashMap.put("isPostagem","1");
        hashMap.put("tipoPostagem", "2");
        databaseReference.push().setValue(hashMap);
    }

    private String calculateDuration(int duration) {
        String finalDuration = "";
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        if (minutes == 0) {
            finalDuration = "0:" + seconds;
        } else {
            if (seconds >= 60) {
                long sec = seconds - (minutes * 60);
                finalDuration = minutes + ":" + sec;
            }
        }
        return finalDuration;
    }

    private void informacoesPublicador(final ImageView imagem_perfil, final TextView apelido, String id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("usuarios").child(id);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                Glide.with(context).load(usuario.getCaminhoFoto()).into(imagem_perfil);
                apelido.setText(usuario.getApelido());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    private void pegaComentarios(String postagemId, final TextView comentarios){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("comentarios").child(postagemId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comentarios.setText("Ver " + dataSnapshot.getChildrenCount() + " comentários" );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
