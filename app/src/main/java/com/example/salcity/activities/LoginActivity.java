package com.example.salcity.activities;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: LoginActivity
 * LINGUAGEM: JAVA
 *
 * OBJETIVO:
 * Controlar a interface de autenticação.
 *
 * RESPONSABILIDADES:
 *
 * 1. Inicializar os componentes XML.
 * 2. Validar os campos obrigatórios.
 * 3. Mostrar e ocultar a senha.
 * 4. Controlar os eventos dos botões.
 * 5. Preparar a integração com autenticação real.
 * 6. Entrar com a conta Google (LoginGoogle).
 *
 * IMPORTANTE:
 * A autenticação passa pelo AutenticacaoRepository.
 * Hoje é usada a versão PROVISÓRIA (sem banco), que aceita
 * qualquer e-mail e senha preenchidos. Só para desenvolvimento.
 * =====================================================
 */

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.salcity.R;
import com.example.salcity.data.AutenticacaoRepository;
import com.example.salcity.data.AutenticacaoRepositoryProvisorio;
import com.example.salcity.data.GoogleConfig;
import com.example.salcity.data.LoginGoogle;
import com.example.salcity.data.SessaoUsuario;
import com.example.salcity.data.UsuarioLogado;

public class LoginActivity extends AppCompatActivity {

    /*
     * =================================================
     * DECLARAÇÃO DOS COMPONENTES
     * =================================================
     */

    // Campo de identificação do usuário.
    private EditText edtEmailLogin;

    // Campo responsável pela senha.
    private EditText edtSenhaLogin;

    // Botão principal de autenticação.
    private Button btnEntrar;

    // Controle de visibilidade da senha.
    private TextView btnMostrarSenha;

    // Acesso à recuperação de senha.
    private TextView btnEsqueceuSenha;

    // Botões de autenticação social.
    private TextView btnLoginGoogle;
    private TextView btnLoginFacebook;

    // Acesso ao cadastro.
    private TextView btnCadastrar;

    // Botão de retorno.
    private TextView btnVoltarLogin;

    // Indica se a senha está visível.
    private boolean senhaVisivel = false;

    // Camada de dados responsável por conferir o login.
    private AutenticacaoRepository autenticacaoRepository;


    /*
     * =================================================
     * MÉTODO: onCreate()
     * =================================================
     *
     * Executado quando a Activity é criada.
     *
     * 1. Carrega o XML.
     * 2. Localiza os componentes.
     * 3. Configura os eventos de clique.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Se já existe uma sessão ativa, pula o login.
        if (SessaoUsuario.estaLogado(this)) {
            abrirHome();
            return;
        }

        // Carrega a interface da tela de login.
        setContentView(R.layout.activity_login);

        // Camada de dados PROVISÓRIA: não confere conta (ainda não há banco).
        // Quando o banco existir, troque por:
        // new AutenticacaoRepositoryRoom(AppDatabase.getInstance(this).usuarioDao())
        autenticacaoRepository = new AutenticacaoRepositoryProvisorio();

        // Inicializa os componentes.
        inicializarComponentes();

        // Configura as interações.
        configurarEventos();
    }


    /*
     * =================================================
     * MÉTODO: inicializarComponentes()
     * =================================================
     *
     * OBJETIVO:
     * Associar as variáveis Java aos IDs do XML.
     */

    private void inicializarComponentes() {

        edtEmailLogin =
                findViewById(R.id.edtEmailLogin);

        edtSenhaLogin =
                findViewById(R.id.edtSenhaLogin);

        btnEntrar =
                findViewById(R.id.btnEntrar);

        btnMostrarSenha =
                findViewById(R.id.btnMostrarSenha);

        btnEsqueceuSenha =
                findViewById(R.id.btnEsqueceuSenha);

        btnLoginGoogle =
                findViewById(R.id.btnLoginGoogle);

        btnLoginFacebook =
                findViewById(R.id.btnLoginFacebook);

        btnCadastrar =
                findViewById(R.id.btnCadastrar);

        btnVoltarLogin =
                findViewById(R.id.btnVoltarLogin);
    }


    /*
     * =================================================
     * MÉTODO: configurarEventos()
     * =================================================
     *
     * OBJETIVO:
     * Configurar as ações dos componentes clicáveis.
     */

    private void configurarEventos() {

        // Valida os campos ao tocar em Entrar.
        btnEntrar.setOnClickListener(v -> validarLogin());

        // Alterna a visibilidade da senha.
        btnMostrarSenha.setOnClickListener(
                v -> alternarVisibilidadeSenha()
        );

        // Prepara o acesso à recuperação de senha.
        btnEsqueceuSenha.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Recuperação de senha será implementada.",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Entrar com a conta Google.
        btnLoginGoogle.setOnClickListener(v -> entrarComGoogle());

        // Prepara a autenticação com Facebook.
        btnLoginFacebook.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Login com Facebook ainda não configurado.",
                    Toast.LENGTH_SHORT
            ).show();
        });

        /*
         * =====================================================
         * BOTÃO: CADASTRE-SE
         * =====================================================
         *
         * OBJETIVO:
         * Abrir a tela de cadastro de novos usuários.
         *
         * A Activity de cadastro será adicionada
         * à pilha de navegação do Android.
         */

        btnCadastrar.setOnClickListener(v -> {

            // Cria a intenção de abrir o cadastro.
            Intent intent = new Intent(
                    LoginActivity.this,
                    CadastroActivity.class
            );

            // Inicia a tela de cadastro.
            startActivity(intent);
        });

        // Retorna à apresentação inicial.
        btnVoltarLogin.setOnClickListener(v -> {

            voltarIntroducao();
        });
    }


    /*
     * =================================================
     * MÉTODO: validarLogin()
     * =================================================
     *
     * OBJETIVO:
     * Verificar se os campos obrigatórios
     * foram preenchidos.
     *
     * IMPORTANTE:
     * A validação local não confirma credenciais.
     * A autenticação real será implementada depois.
     */

    private void validarLogin() {

        // Obtém o identificador informado.
        String identificador = edtEmailLogin
                .getText()
                .toString()
                .trim();

        // Obtém a senha informada.
        String senha = edtSenhaLogin
                .getText()
                .toString();

        /*
         * VALIDAÇÃO 1:
         * Verifica se o identificador está vazio.
         */
        if (identificador.isEmpty()) {

            edtEmailLogin.setError(
                    "Informe seu e-mail ou usuário."
            );

            edtEmailLogin.requestFocus();

            return;
        }

        /*
         * VALIDAÇÃO 2:
         * Verifica se a senha está vazia.
         */
        if (senha.isEmpty()) {

            edtSenhaLogin.setError(
                    "Informe sua senha."
            );

            edtSenhaLogin.requestFocus();

            return;
        }

        /*
         * VALIDAÇÃO CONCLUÍDA:
         *
         * Envia os dados ao AutenticacaoRepository.
         * O resultado volta na thread principal.
         */

        // Evita toques repetidos enquanto o login é processado.
        btnEntrar.setEnabled(false);

        autenticacaoRepository.entrar(
                identificador,
                senha,
                new AutenticacaoRepository.LoginCallback() {

                    @Override
                    public void onSucesso(UsuarioLogado usuario) {

                        // Evita mexer na tela se ela já foi fechada.
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        // Lembra quem está logado.
                        SessaoUsuario.iniciar(LoginActivity.this, usuario);

                        abrirHome();
                    }

                    @Override
                    public void onErro(String mensagem) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        // Libera o botão para uma nova tentativa.
                        btnEntrar.setEnabled(true);

                        Toast.makeText(
                                LoginActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    /*
     * =================================================
     * MÉTODO: entrarComGoogle()
     * =================================================
     *
     * OBJETIVO:
     * Abrir a seleção de contas do Google e, se o
     * usuário escolher uma conta, iniciar a sessão.
     *
     * Enquanto GoogleConfig.WEB_CLIENT_ID estiver
     * vazio, apenas um aviso é exibido.
     */

    private void entrarComGoogle() {

        if (!GoogleConfig.estaConfigurado()) {

            Toast.makeText(
                    this,
                    "Login com Google ainda não configurado.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Evita toques repetidos enquanto a janela do Google está aberta.
        btnLoginGoogle.setEnabled(false);

        LoginGoogle.entrar(this, new LoginGoogle.Callback() {

            @Override
            public void onSucesso(UsuarioLogado usuario, String idToken) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                // Lembra quem está logado.
                SessaoUsuario.iniciar(LoginActivity.this, usuario);

                abrirHome();
            }

            @Override
            public void onCancelado() {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                // O usuário desistiu: apenas libera o botão.
                btnLoginGoogle.setEnabled(true);
            }

            @Override
            public void onErro(String mensagem) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnLoginGoogle.setEnabled(true);

                Toast.makeText(
                        LoginActivity.this,
                        mensagem,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }


    /*
     * =================================================
     * MÉTODO: abrirHome()
     * =================================================
     *
     * OBJETIVO:
     * Abrir a tela inicial após o login.
     *
     * Limpa a pilha de telas para que o botão Voltar
     * não retorne ao login.
     */

    private void abrirHome() {

        Intent intent = new Intent(
                LoginActivity.this,
                HomeActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }


    /*
     * =================================================
     * MÉTODO: alternarVisibilidadeSenha()
     * =================================================
     *
     * OBJETIVO:
     * Mostrar ou ocultar os caracteres da senha.
     *
     * PasswordTransformationMethod:
     * Oculta os caracteres digitados.
     *
     * HideReturnsTransformationMethod:
     * Exibe os caracteres digitados.
     */

    private void alternarVisibilidadeSenha() {

        // Inverte o estado atual.
        senhaVisivel = !senhaVisivel;

        if (senhaVisivel) {

            // Mostra os caracteres.
            edtSenhaLogin.setTransformationMethod(
                    HideReturnsTransformationMethod
                            .getInstance()
            );

            // Atualiza a indicação acessível.
            btnMostrarSenha.setContentDescription(
                    "Ocultar senha"
            );

        } else {

            // Oculta os caracteres.
            edtSenhaLogin.setTransformationMethod(
                    PasswordTransformationMethod
                            .getInstance()
            );

            btnMostrarSenha.setContentDescription(
                    "Mostrar senha"
            );
        }

        /*
         * Mantém o cursor no final do texto.
         */
        edtSenhaLogin.setSelection(
                edtSenhaLogin.getText().length()
        );
    }


    /*
     * =================================================
     * MÉTODO: voltarIntroducao()
     * =================================================
     *
     * OBJETIVO:
     * Retornar à apresentação inicial do SalCity.
     *
     * A navegação explícita permite rever as
     * introduções mesmo que já tenham sido concluídas.
     */

    private void voltarIntroducao() {

        /*
         * A IntroActivity utiliza SharedPreferences
         * para decidir se deve exibir as introduções.
         *
         * Para permitir revê-las, removemos a marca
         * de conclusão antes de abrir essa Activity.
         *
         * Esta é uma solução inicial de navegação.
         */
        getSharedPreferences(
                "SalCityPrefs",
                MODE_PRIVATE
        ).edit()
                .putBoolean("intro_concluida", false)
                .apply();

        // Cria a intenção de abrir as introduções.
        Intent intent = new Intent(
                LoginActivity.this,
                IntroActivity.class
        );

        // Abre a primeira introdução.
        startActivity(intent);

        // Encerra o login atual.
        finish();
    }
}