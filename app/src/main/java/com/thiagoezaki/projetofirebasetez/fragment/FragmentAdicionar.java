package com.thiagoezaki.projetofirebasetez.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.activity.ContaConfiguracaoActivity;
import com.thiagoezaki.projetofirebasetez.activity.FiltroActivity;
import com.thiagoezaki.projetofirebasetez.activity.MainActivity;
import com.thiagoezaki.projetofirebasetez.helper.ConfiguracaoFireBase;
import com.thiagoezaki.projetofirebasetez.helper.Permissao;
import com.thiagoezaki.projetofirebasetez.helper.UsuarioFirebase;
import com.thiagoezaki.projetofirebasetez.model.Postagem;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.zip.Inflater;

import static android.app.Activity.RESULT_OK;


public class FragmentAdicionar extends Fragment {

    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private Button btnImagem, btnMusica;
    private int validarMusica = 0;
    private Uri uriMusica;
    private static final int SELECAO_GALERIA = 200;
    private String[] permissoes_necessarias = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private Usuario usuario;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    private String identificadorUsuario, nomeMusica, musicaUrl, urlStorage;
    private JcPlayerView jcPlayerView;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();
    private TextView descricao;
    private AlertDialog alertDialog;

    public FragmentAdicionar() {
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
        View view = inflater.inflate(R.layout.fragment_adicionar, container, false);
        btnImagem = view.findViewById(R.id.btnImagem);
        btnMusica = view.findViewById(R.id.btnMusica);
        jcPlayerView = view.findViewById(R.id.jcplayer);
        descricao = view.findViewById(R.id.textInputEditTextDescricaoMusica);
        Toolbar toolbar = view.findViewById(R.id.toolbarPrincipal);


        //for crate home button
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        toolbar.setTitle("Adicionar");

        //validar permissões
        Permissao.validarPermissoes(permissoes_necessarias, getActivity(), 1);

        usuario = UsuarioFirebase.getDadosUsuarioLogado();

        storageRef = ConfiguracaoFireBase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();

        btnImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });

        btnMusica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                escolherMusica();

            }
        });
        return view;
    }

    public void carregarDialogoCarregamento(String titulo) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(titulo);
        alert.setCancelable(false);

        alert.setView(R.layout.carregamento);

        alertDialog = alert.create();
        alertDialog.show();
    }

    public void escolherMusica() {
        Intent i_upload = new Intent();
        i_upload.setType("audio/*");
        i_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i_upload, 1);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap imagem = null;

        if (resultCode == getActivity().RESULT_OK) {
            //validar tipo de seleção de imagem
            try {
                switch (requestCode) {
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), localImagemSelecionada);
                        break;
                }

                //valida imagem selecionada
                if (imagem != null) {
                    //Recuperar os dados da imagem no firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //enviar imagem para aplicação de filtro
                    Intent intent = new Intent(getActivity(), FiltroActivity.class);
                    intent.putExtra("fotoescolhida", dadosImagem);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                uriMusica = data.getData();
                Cursor nCursor = getActivity().getContentResolver().query(uriMusica, null, null, null, null);
                int indexedname = nCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                nCursor.moveToFirst();
                nomeMusica = nCursor.getString(indexedname);
                nCursor.close();
                uploadMusicaParaFirebase();
            }
        }
    }


    public void uploadMusicaParaFirebase() {
        if (validarMusica == 0) {
            Postagem postagem = new Postagem();
            urlStorage = postagem.getId();

            StorageReference storageReference = storageRef.child("sons").child("postagens").child(postagem.getId());
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.show();
            storageReference.putFile(uriMusica).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isComplete()) ;
                    Uri urlSong = uriTask.getResult();
                    musicaUrl = urlSong.toString();
                    jcAudios.clear();
                    jcAudios.add(JcAudio.createFromURL(nomeMusica, musicaUrl));
                    progressDialog.dismiss();
                    jcPlayerView.initPlaylist(jcAudios, null);
                    jcPlayerView.playAudio(jcAudios.get(0));
                    jcPlayerView.setVisibility(View.VISIBLE);
                    jcPlayerView.createNotification();
                    descricao.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Erro ao fazer upload da musica!", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progres = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    int atualProgresso = (int) progres;
                    progressDialog.setMessage(atualProgresso + "%");
                }
            });
            validarMusica = 1;
        } else {
            StorageReference storageReference = storageRef.child("sons").child("postagens").child(urlStorage);
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.show();
            storageReference.putFile(uriMusica).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isComplete()) ;
                    Uri urlSong = uriTask.getResult();
                    musicaUrl = urlSong.toString();
                    jcAudios.clear();
                    jcAudios.add(JcAudio.createFromURL(nomeMusica, musicaUrl));
                    progressDialog.dismiss();
                    jcPlayerView.initPlaylist(jcAudios, null);
                    jcPlayerView.playAudio(jcAudios.get(0));
                    jcPlayerView.setVisibility(View.VISIBLE);
                    jcPlayerView.createNotification();
                    descricao.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Erro ao fazer upload da musica!", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progres = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    int atualProgresso = (int) progres;
                    progressDialog.setMessage(atualProgresso + "%");
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //inflater adicionar
        inflater.inflate(R.menu.menu_musica, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_salvar_musica:
                publicarPostagem();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void publicarPostagem() {
        carregarDialogoCarregamento("Salvando Postagem");
        Postagem postagem = new Postagem();
        //valida se o usuario selecionou uma música
        if (musicaUrl == null) {
            Toast.makeText(getActivity(), "Selecione uma música para postagem", Toast.LENGTH_LONG).show();
            alertDialog.cancel();
        } else {
            postagem.setIdUsuario(identificadorUsuario);
            postagem.setDescricao(descricao.getText().toString());
            postagem.setNomeMusica(nomeMusica);
            postagem.setTipoPostagem("2");
            postagem.setCaminhoPostagem(musicaUrl);

            jcPlayerView.isPaused();
            if (postagem.salvar()) {
                jcPlayerView.setVisibility(View.GONE);
                descricao.setVisibility(View.GONE);
                jcAudios.clear();
                descricao.setText("");
                alertDialog.cancel();
                Intent i = new Intent(getActivity(), MainActivity.class);
                startActivity(i);
            }
        }
    }
}