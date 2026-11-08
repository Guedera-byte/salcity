package com.example.salcity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.salcity.R;

/**
 * Tela de perfil do SalCity (front-end).
 * Dados demonstrativos até a integração com autenticação/back-end.
 */
public class PerfilActivity extends AppCompatActivity {

    private TextView txtNome, txtEmail, txtTelefone, txtIniciais;

    /** Inicializa o layout, os campos e as ações da tela. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        associarComponentes();
        preencherDadosDemonstrativos();
        configurarBotoes();
    }

    /** Associa os elementos XML às variáveis Java. */
    private void associarComponentes() {
        txtNome = findViewById(R.id.txtNomePerfil);
        txtEmail = findViewById(R.id.txtEmailPerfil);
        txtTelefone = findViewById(R.id.txtTelefonePerfil);
        txtIniciais = findViewById(R.id.txtAvatarPerfil);
    }

    /**
     * Mostra dados de demonstração. Caso a Activity receba extras explícitos,
     * usa-os na interface. Não presume que esses dados sejam autenticados.
     */
    private void preencherDadosDemonstrativos() {
        Intent dados = getIntent();
        String nome = dados.getStringExtra("perfil_nome");
        String email = dados.getStringExtra("perfil_email");
        String telefone = dados.getStringExtra("perfil_telefone");

        if (nome == null || nome.trim().isEmpty()) nome = "Usuário SalCity";
        if (email == null || email.trim().isEmpty()) email = "E-mail não disponível";
        if (telefone == null || telefone.trim().isEmpty()) telefone = "Não informado";

        txtNome.setText(nome);
        txtEmail.setText(email);
        txtTelefone.setText(telefone);
        txtIniciais.setText(obterIniciais(nome));
    }

    /** Retorna até duas iniciais para o avatar, sem exigir imagem de perfil. */
    private String obterIniciais(String nome) {
        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase(java.util.Locale.getDefault());
    }

    /** Configura voltar, Meus Roteiros e o diálogo já existente de saída. */
    private void configurarBotoes() {
        findViewById(R.id.btnVoltarPerfil).setOnClickListener(v -> finish());
        findViewById(R.id.btnMeusRoteirosPerfil).setOnClickListener(v ->
                startActivity(new Intent(this, MeusRoteirosActivity.class)));
        findViewById(R.id.btnSairPerfil).setOnClickListener(v ->
                ConfirmacaoSaida.mostrar(this, R.drawable.logo_salcity));
    }
}
