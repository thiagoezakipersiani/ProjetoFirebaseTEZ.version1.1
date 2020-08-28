package com.thiagoezaki.projetofirebasetez.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.fragment.FragmentPerfil;
import com.thiagoezaki.projetofirebasetez.helper.ConfiguracaoFireBase;
import com.thiagoezaki.projetofirebasetez.helper.Permissao;
import com.thiagoezaki.projetofirebasetez.helper.UsuarioFirebase;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

import java.io.ByteArrayOutputStream;

public class ContaConfiguracaoActivity extends AppCompatActivity {

    private TextView campoAdicionarFoto;
    private ImageView imagemPerfilNovo;
    private EditText campoNomeCadastro, campoEmailCadastro, campoApelidoCadastro, campoCelularCadastro, campoSenhaCadastro;
    private RadioButton campoMusico, campoUsuario, campoBanda;
    private String tipoUsuario;
    private Button buttonAtualizar;
    private Usuario usuario;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    private String identificadorUsuario;
    private String tipoUser;
    private String [] permissoes_necessarias= new String []{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conta_configuracao);
        iniciarComponentes();

        //validar permissões
        Permissao.validarPermissoes(permissoes_necessarias,this,1);

        usuario= UsuarioFirebase.getDadosUsuarioLogado();

        storageRef = ConfiguracaoFireBase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();

        Toolbar toolbar=findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Editar Perfil");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_branco);

        //recuperar dados do usuário
        FirebaseUser userPerfil= UsuarioFirebase.getUsuarioAtual();
        campoNomeCadastro.setText(userPerfil.getDisplayName());
        campoEmailCadastro.setText(userPerfil.getEmail());
        Uri url=userPerfil.getPhotoUrl();
        if (url !=null){
            Glide.with(ContaConfiguracaoActivity.this)
                    .load(url)
                    .into(imagemPerfilNovo);
        }else {
            imagemPerfilNovo.setImageResource(R.drawable.avatar);
        }

        databaseRef = ConfiguracaoFireBase.getFireBase();

        DatabaseReference databaseReferenceApelido=databaseRef.child("usuarios").child(identificadorUsuario);

        databaseReferenceApelido.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                campoApelidoCadastro.setText(usuario.getApelido());

                if((usuario.getPerfilUsuario()).equals("1")){
                    campoUsuario.setChecked(true);
                }
                else if((usuario.getPerfilUsuario()).equals("2")){
                    campoMusico.setChecked(true);
                }
                else if ((usuario.getPerfilUsuario()).equals("3")) {
                    campoBanda.setChecked(true);
                }

                campoCelularCadastro.setText(usuario.getCelular());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }

        });

        //salvar dados
        buttonAtualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperarDados();
                usuario.atualizar();
                Toast.makeText(ContaConfiguracaoActivity.this, "Dados alterados com sucesso!",
                        Toast.LENGTH_SHORT).show();
                Intent i=new Intent( ContaConfiguracaoActivity.this, MainActivity.class);
                startActivity(i);
            }
        });


          //Adicionar foto
        campoAdicionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;

            try {
                //Selecionando apenas da galeria
                switch (requestCode) {
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                //Casos seja selecionado uma imagem
                if (imagem != null) {
                    //configura imagem na tela do usuário
                    imagemPerfilNovo.setImageBitmap(imagem);

                    //Recuperar os dados da imagem no firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();
                    //Salvar imagem no firebase
                    StorageReference imagemRef = storageRef.child("imagens").child("perfil").child(identificadorUsuario + ".jpeg");
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ContaConfiguracaoActivity.this, "Erro ao fazer upload da imagem!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                          //  Toast.makeText(ContaConfiguracaoActivity.this, "Sucesso ao fazer upload da imagem!", Toast.LENGTH_SHORT).show();
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            atualizarFotoUsuario( uri );
                                        }
                                    });
                                }
                            }
                        }});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void atualizarFotoUsuario (Uri url){
        //atualizar foto de perfil
        UsuarioFirebase.atualizarFotoUsuario(url);
        //atualizar foto no firebase
        usuario.setCaminhoFoto(url.toString());
        Toast.makeText(ContaConfiguracaoActivity.this, "Sua foto foi atualizada com sucesso!", Toast.LENGTH_SHORT).show();
     }
    public void iniciarComponentes() {
        //editText
        campoApelidoCadastro = findViewById(R.id.editApelidoAtualizar);
        campoCelularCadastro = findViewById(R.id.editCelularAtualizar);
        campoNomeCadastro = findViewById(R.id.editNomeAtualizar);
        campoEmailCadastro = findViewById(R.id.editEmailAtualizar);

        //RadioButton
        campoBanda = findViewById(R.id.radioButtonBandaAtualizar);
        campoMusico = findViewById(R.id.radioButtonMusicoAtualizar);
        campoUsuario = findViewById(R.id.radioButtonUsuarioAtualizar);

        //buttton
        buttonAtualizar = findViewById(R.id.buttonSalvarAlteracao);

        //imagem
        imagemPerfilNovo=findViewById(R.id.imageViewPerfilNovo);

        //text view
        campoAdicionarFoto=findViewById(R.id.textViewAlterarFoto);
        campoEmailCadastro.setFocusable(false);

    }

    public void recuperarDados(){
        String nomeAtualizado= campoNomeCadastro.getText().toString();
        String apelidoAtualizado=campoApelidoCadastro.getText().toString();
        String celularAtualizado=campoCelularCadastro.getText().toString();
        String perfil_tipo_Atualizado;

        if (campoUsuario.isChecked()) {
            perfil_tipo_Atualizado = "1";
        } else if (campoMusico.isChecked()) {
            perfil_tipo_Atualizado = "2";
        } else {
            perfil_tipo_Atualizado = "3";
        }

        UsuarioFirebase.atualizarNomeUsuario(nomeAtualizado);
        usuario.setApelido(apelidoAtualizado);
        usuario.setPerfilUsuario(perfil_tipo_Atualizado);
        usuario.setCelular(celularAtualizado);
        usuario.setNome(nomeAtualizado);
        usuario.setApelidoMaiusculo(apelidoAtualizado.toUpperCase());
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}