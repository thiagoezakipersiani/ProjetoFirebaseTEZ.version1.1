package com.thiagoezaki.projetofirebasetez.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.thiagoezaki.projetofirebasetez.R;
import com.thiagoezaki.projetofirebasetez.fragment.FragmentAdicionar;
import com.thiagoezaki.projetofirebasetez.fragment.FragmentFeed;
import com.thiagoezaki.projetofirebasetez.fragment.FragmentNotificacao;
import com.thiagoezaki.projetofirebasetez.fragment.FragmentPerfil;
import com.thiagoezaki.projetofirebasetez.fragment.FragmentPerfilAmigo;
import com.thiagoezaki.projetofirebasetez.fragment.FragmentPesquisa;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //navegation view
        configurarBottomView();


        Bundle bundle= getIntent().getExtras();
        if(bundle !=null){
          String publicador=bundle.getString("publicadorid");

            SharedPreferences.Editor editor= getSharedPreferences("PREFS",MODE_PRIVATE).edit();
            editor.putString("perfilid",publicador);
            editor.apply();
            FragmentManager  fragmentManagerIntent = getSupportFragmentManager();
            FragmentTransaction fragmentTransactionIntent=fragmentManagerIntent.beginTransaction();
            fragmentTransactionIntent.replace(R.id.viewPager,new FragmentPerfilAmigo()).commit();
        } else {
            FragmentManager  fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.viewPager,new FragmentFeed()).commit();
        }

    }

    private void configurarBottomView(){
        BottomNavigationViewEx bottomNavigationViewEx=findViewById(R.id.bottomNavigation);

        //Configurações
        bottomNavigationViewEx.enableAnimation(true);
        bottomNavigationViewEx.enableItemShiftingMode(true);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(true);

        //Habilitar Navegação
        habilitaNavegacao(bottomNavigationViewEx);
    }

    public void habilitaNavegacao(BottomNavigationViewEx viewEx){
        viewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                FragmentManager  fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();

                switch (menuItem.getItemId()){
                    case R.id.ic_home :
                        fragmentTransaction.replace(R.id.viewPager,new FragmentFeed()).commit();
                        return true;
                    case R.id.ic_pesquisa :
                        fragmentTransaction.replace(R.id.viewPager,new FragmentPesquisa()).commit();
                        return true;
                    case R.id.ic_perfil :
                        fragmentTransaction.replace(R.id.viewPager,new FragmentPerfil()).commit();
                        return true;
                    case R.id.ic_adicionar :
                        fragmentTransaction.replace(R.id.viewPager,new FragmentAdicionar()).commit();
                        return true;
                    case R.id.ic_notificacao:
                        fragmentTransaction.replace(R.id.viewPager,new FragmentNotificacao()).commit();
                        return true;
                }
                return false;
            }
        });
    }

}