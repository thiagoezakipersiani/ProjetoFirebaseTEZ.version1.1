package com.thiagoezaki.projetofirebasetez.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.storage.StorageReference;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.helper.ConfiguracaoFireBase;
import com.thiagoezaki.projetofirebasetez.helper.UsuarioFirebase;
import com.thiagoezaki.projetofirebasetez.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNomeCadastro, campoEmailCadastro, campoApelidoCadastro, campoCelularCadastro, campoSenhaCadastro;
    private RadioButton campoMusico, campoUsuario, campoBanda;
    private ProgressBar progressBarCadastro;
    private String tipoUsuario;
    private Button buttonCadastrar;
    private Usuario usuario;
    private FirebaseAuth autenticacao;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageRef;
    private String tipoUsuario1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        iniciarComponentes();

        storageRef = ConfiguracaoFireBase.getFirebaseStorage();

        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioButton();
                String textoNome = campoNomeCadastro.getText().toString();
                String textoSenha = campoSenhaCadastro.getText().toString();
                String textoEmail = campoEmailCadastro.getText().toString();
                String textoCelular = campoCelularCadastro.getText().toString();
                String textoApelido = campoApelidoCadastro.getText().toString();
                String perfilCadastro = tipoUsuario;


                if (!textoNome.isEmpty()) {
                    if (!textoEmail.isEmpty()) {
                        if (!textoSenha.isEmpty()) {
                            if (!textoApelido.isEmpty()) {
                                if (!perfilCadastro.isEmpty()) {
                                    usuario = new Usuario();
                                    usuario.setNome(textoNome);
                                    usuario.setEmail(textoEmail);
                                    usuario.setCelular(textoCelular);
                                    usuario.setPerfilUsuario(perfilCadastro);
                                    usuario.setSenha(textoSenha);
                                    usuario.setApelido(textoApelido);
                                    usuario.setApelidoMaiusculo(textoApelido.toUpperCase());
                                    cadastroUsuario(usuario);
                                    progressBarCadastro.setVisibility(View.VISIBLE);
                                } else {
                                    Toast.makeText(CadastroActivity.this, "Preencha o tipo de perfil!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(CadastroActivity.this, "Preencha o Apelido!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CadastroActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CadastroActivity.this, "Preencha o Email!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CadastroActivity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
                }
            }

        });

    }

    public void cadastroUsuario(final Usuario usuario) {
        autenticacao = ConfiguracaoFireBase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    try {
                        //Cadastro usuarios
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId(idUsuario);
                        usuario.salvar();

                        //Salvar dados no profile do Firebase
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                        progressBarCadastro.setVisibility(View.GONE);

                        Toast.makeText(CadastroActivity.this, "Castrado com sucesso", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        progressBarCadastro.setVisibility(View.GONE);
                    }

                } else {
                    String erroExcessao = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        progressBarCadastro.setVisibility(View.GONE);
                        erroExcessao = "Digite uma senha mais forte";

                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        progressBarCadastro.setVisibility(View.GONE);
                        erroExcessao = "Digite um e-mail válido";
                    } catch (FirebaseAuthUserCollisionException e) {
                        progressBarCadastro.setVisibility(View.GONE);
                        erroExcessao = "Esta conta já foi cadastrada";
                    } catch (Exception e) {
                        progressBarCadastro.setVisibility(View.GONE);
                        erroExcessao = "Ao cadastrar o usuário" + e.getMessage();
                        e.printStackTrace();
                    }
                    progressBarCadastro.setVisibility(View.GONE);
                    Toast.makeText(CadastroActivity.this, "Erro" + erroExcessao, Toast.LENGTH_SHORT).show();
                 }
            }
        });

    }

    public void iniciarComponentes() {
        //editText
        campoApelidoCadastro = findViewById(R.id.editApelidoCadastro);
        campoCelularCadastro = findViewById(R.id.editCelularCadastro);
        campoNomeCadastro = findViewById(R.id.editNomeCadastro);
        campoEmailCadastro = findViewById(R.id.editEmailCadastro);
        campoSenhaCadastro = findViewById(R.id.editSenhaCadastro);

        //RadioButton
        campoBanda = findViewById(R.id.radioButtonBandaAtualizar);
        campoMusico = findViewById(R.id.radioButtonMusicoAtualizar);
        campoUsuario = findViewById(R.id.radioButtonUsuario);

        //buttton
        buttonCadastrar = findViewById(R.id.buttonCadastrar);

        //progresBar
        progressBarCadastro = findViewById(R.id.progressBarCadastro);

        campoEmailCadastro.requestFocus();
    }

    public void radioButton() {
        if (campoUsuario.isChecked()) {
            tipoUsuario = "1";
        } else if (campoMusico.isChecked()) {
            tipoUsuario = "2";
        } else {
            tipoUsuario = "3";
        }
    }

}