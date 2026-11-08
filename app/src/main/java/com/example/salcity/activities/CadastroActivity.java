package com.example.salcity.activities;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: CadastroActivity
 * LINGUAGEM: JAVA
 *
 * OBJETIVO:
 * Controlar a tela de cadastro de usuários.
 *
 * RESPONSABILIDADES:
 *
 * 1. Inicializar os componentes XML.
 * 2. Validar nome e e-mail.
 * 3. Validar senha e confirmação.
 * 4. Mostrar e ocultar as senhas.
 * 5. Verificar a aceitação dos termos.
 * 6. Configurar links dos documentos legais.
 * 7. Controlar a navegação para o login.
 *
 * IMPORTANTE:
 * Ainda não existe banco de dados: o cadastro usa o
 * UsuarioRepositoryProvisorio, que valida e responde
 * sucesso, mas NÃO cria conta de verdade.
 * Ao criar o banco, troque a implementação do
 * UsuarioRepository no onCreate().
 * =====================================================
 */

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.salcity.R;
import com.example.salcity.data.UsuarioRepository;
import com.example.salcity.data.UsuarioRepositoryProvisorio;

public class CadastroActivity extends AppCompatActivity {

    /*
     * =================================================
     * COMPONENTES DA INTERFACE
     * =================================================
     */

    // Campo para o nome completo.
    private EditText edtNomeCadastro;

    // Campo para o e-mail.
    private EditText edtEmailCadastro;

    // Campo para a senha.
    private EditText edtSenhaCadastro;

    // Campo para confirmar a senha.
    private EditText edtConfirmarSenha;

    // Botões de visibilidade das senhas.
    private TextView btnMostrarSenhaCadastro;
    private TextView btnMostrarConfirmacao;

    // Caixa de aceitação dos termos.
    private CheckBox checkTermos;

    // Texto com links dos documentos legais.
    private TextView txtTermos;

    // Botão principal de cadastro.
    private Button btnCadastrarUsuario;

    // Botão de retorno ao login.
    private TextView btnEntrarCadastro;

    // Botão de voltar.
    private TextView btnVoltarCadastro;

    // Estado de visibilidade da senha.
    private boolean senhaVisivel = false;

    // Estado de visibilidade da confirmação.
    private boolean confirmacaoVisivel = false;

    // Camada de dados responsável por salvar o usuário.
    private UsuarioRepository usuarioRepository;


    /*
     * =================================================
     * MÉTODO: onCreate()
     * =================================================
     *
     * Executado quando a Activity é criada.
     *
     * RESPONSABILIDADES:
     * 1. Carregar o XML.
     * 2. Inicializar os componentes.
     * 3. Configurar os eventos.
     * 4. Configurar os links legais.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Carrega o layout da tela de cadastro.
        setContentView(R.layout.activity_cadastro);

        // Camada de dados PROVISÓRIA: não grava nada (ainda não há banco).
        // Quando o banco existir, troque por:
        // new UsuarioRepositoryRoom(AppDatabase.getInstance(this).usuarioDao())
        usuarioRepository = new UsuarioRepositoryProvisorio();

        // Localiza os componentes XML.
        inicializarComponentes();

        // Configura os cliques.
        configurarEventos();

        // Configura os links dos termos.
        configurarLinksTermos();
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

        edtNomeCadastro =
                findViewById(R.id.edtNomeCadastro);

        edtEmailCadastro =
                findViewById(R.id.edtEmailCadastro);

        edtSenhaCadastro =
                findViewById(R.id.edtSenhaCadastro);

        edtConfirmarSenha =
                findViewById(R.id.edtConfirmarSenha);

        btnMostrarSenhaCadastro =
                findViewById(R.id.btnMostrarSenhaCadastro);

        btnMostrarConfirmacao =
                findViewById(R.id.btnMostrarConfirmacao);

        checkTermos =
                findViewById(R.id.checkTermos);

        txtTermos =
                findViewById(R.id.txtTermos);

        btnCadastrarUsuario =
                findViewById(R.id.btnCadastrarUsuario);

        btnEntrarCadastro =
                findViewById(R.id.btnEntrarCadastro);

        btnVoltarCadastro =
                findViewById(R.id.btnVoltarCadastro);
    }


    /*
     * =================================================
     * MÉTODO: configurarEventos()
     * =================================================
     *
     * OBJETIVO:
     * Definir o comportamento dos componentes.
     */

    private void configurarEventos() {

        /*
         * BOTÃO CADASTRAR
         *
         * Executa a validação dos dados.
         */
        btnCadastrarUsuario.setOnClickListener(v -> {

            validarCadastro();
        });

        /*
         * BOTÃO MOSTRAR SENHA
         *
         * Alterna a visibilidade da senha.
         */
        btnMostrarSenhaCadastro.setOnClickListener(v -> {

            senhaVisivel = !senhaVisivel;

            alterarVisibilidadeSenha(
                    edtSenhaCadastro,
                    senhaVisivel
            );

            btnMostrarSenhaCadastro.setContentDescription(
                    senhaVisivel ? "Ocultar senha" : "Mostrar senha"
            );
        });

        /*
         * BOTÃO MOSTRAR CONFIRMAÇÃO
         *
         * Alterna a visibilidade da confirmação.
         */
        btnMostrarConfirmacao.setOnClickListener(v -> {

            confirmacaoVisivel = !confirmacaoVisivel;

            alterarVisibilidadeSenha(
                    edtConfirmarSenha,
                    confirmacaoVisivel
            );

            btnMostrarConfirmacao.setContentDescription(
                    confirmacaoVisivel
                            ? "Ocultar confirmação de senha"
                            : "Mostrar confirmação de senha"
            );
        });

        /*
         * BOTÃO ENTRAR
         *
         * Retorna à tela de login.
         */
        btnEntrarCadastro.setOnClickListener(v -> {

            abrirLogin();
        });

        /*
         * BOTÃO VOLTAR
         *
         * Retorna à tela anterior.
         */
        btnVoltarCadastro.setOnClickListener(v -> {

            finish();
        });
    }


    /*
     * =================================================
     * MÉTODO: alterarVisibilidadeSenha()
     * =================================================
     *
     * OBJETIVO:
     * Mostrar ou ocultar os caracteres de um campo.
     *
     * @param campo
     * EditText que terá a visibilidade alterada.
     *
     * @param mostrar
     * true: exibe os caracteres.
     * false: oculta os caracteres.
     */

    private void alterarVisibilidadeSenha(
            EditText campo,
            boolean mostrar) {

        if (mostrar) {

            // Exibe os caracteres.
            campo.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

        } else {

            // Oculta os caracteres.
            campo.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );
        }

        // Mantém o cursor no final do texto.
        campo.setSelection(campo.getText().length());
    }


    /*
     * =================================================
     * MÉTODO: validarCadastro()
     * =================================================
     *
     * OBJETIVO:
     * Validar os dados antes de enviar uma
     * solicitação de criação de conta.
     *
     * VALIDAÇÕES:
     *
     * 1. Nome obrigatório.
     * 2. E-mail obrigatório e válido.
     * 3. Senha obrigatória.
     * 4. Senha com pelo menos 8 caracteres.
     * 5. Confirmação obrigatória.
     * 6. Senhas iguais.
     * 7. Aceitação dos termos.
     *
     * Depois de validar, os dados são enviados ao
     * UsuarioRepository (hoje a versão provisória, sem banco).
     */

    private void validarCadastro() {

        // Obtém o nome informado.
        String nome = edtNomeCadastro
                .getText()
                .toString()
                .trim();

        // Obtém o e-mail informado.
        String email = edtEmailCadastro
                .getText()
                .toString()
                .trim();

        // Obtém a senha informada.
        String senha = edtSenhaCadastro
                .getText()
                .toString();

        // Obtém a confirmação da senha.
        String confirmacao = edtConfirmarSenha
                .getText()
                .toString();

        /*
         * VALIDAÇÃO 1:
         * Nome completo obrigatório.
         */
        if (TextUtils.isEmpty(nome)) {

            edtNomeCadastro.setError(
                    "Informe seu nome completo."
            );

            edtNomeCadastro.requestFocus();

            return;
        }

        /*
         * VALIDAÇÃO 2:
         * E-mail obrigatório.
         */
        if (TextUtils.isEmpty(email)) {

            edtEmailCadastro.setError(
                    "Informe seu e-mail."
            );

            edtEmailCadastro.requestFocus();

            return;
        }

        /*
         * VALIDAÇÃO 3:
         * Formato do e-mail.
         */
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            edtEmailCadastro.setError(
                    "Informe um e-mail válido."
            );

            edtEmailCadastro.requestFocus();

            return;
        }

        /*
         * VALIDAÇÃO 4:
         * Senha obrigatória.
         */
        if (senha.isEmpty()) {

            edtSenhaCadastro.setError(
                    "Informe uma senha."
            );

            edtSenhaCadastro.requestFocus();

            return;
        }

        /*
         * VALIDAÇÃO 5:
         * Tamanho mínimo da senha.
         */
        if (senha.length() < 8) {

            edtSenhaCadastro.setError(
                    "A senha deve ter pelo menos 8 caracteres."
            );

            edtSenhaCadastro.requestFocus();

            return;
        }

        /*
         * VALIDAÇÃO 6:
         * Confirmação obrigatória.
         */
        if (confirmacao.isEmpty()) {

            edtConfirmarSenha.setError(
                    "Confirme sua senha."
            );

            edtConfirmarSenha.requestFocus();

            return;
        }

        /*
         * VALIDAÇÃO 7:
         * As senhas devem ser iguais.
         */
        if (!senha.equals(confirmacao)) {

            edtConfirmarSenha.setError(
                    "As senhas não coincidem."
            );

            edtConfirmarSenha.requestFocus();

            return;
        }

        /*
         * VALIDAÇÃO 8:
         * Aceitação dos termos.
         */
        if (!checkTermos.isChecked()) {

            Toast.makeText(
                    this,
                    "Você precisa aceitar os termos para continuar.",
                    Toast.LENGTH_LONG
            ).show();

            checkTermos.requestFocus();

            return;
        }

        /*
         * TODOS OS CAMPOS FORAM VALIDADOS.
         *
         * Envia os dados ao UsuarioRepository.
         * O resultado volta na thread principal,
         * então é seguro mexer na interface.
         */

        // Evita toques repetidos enquanto o cadastro é processado.
        btnCadastrarUsuario.setEnabled(false);

        usuarioRepository.cadastrar(
                nome,
                email,
                senha,
                new UsuarioRepository.CadastroCallback() {

                    @Override
                    public void onSucesso(long id) {

                        // Evita mexer na tela se ela já foi fechada.
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                CadastroActivity.this,
                                "Conta criada com sucesso! Faça seu login.",
                                Toast.LENGTH_LONG
                        ).show();

                        // Volta para a tela de login.
                        abrirLogin();
                    }

                    @Override
                    public void onErro(String mensagem) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        // Libera o botão para uma nova tentativa.
                        btnCadastrarUsuario.setEnabled(true);

                        Toast.makeText(
                                CadastroActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    /*
     * =================================================
     * MÉTODO: configurarLinksTermos()
     * =================================================
     *
     * OBJETIVO:
     * Transformar os nomes dos documentos legais
     * em áreas clicáveis.
     *
     * Nesta etapa, os links exibem avisos.
     *
     * Antes de disponibilizar o cadastro real,
     * será necessário publicar os documentos.
     */

    private void configurarLinksTermos() {

        String texto =
                "Concordo com os Termos de Uso e Política de Privacidade";

        SpannableString spannable = new SpannableString(texto);

        /*
         * LINK: TERMOS DE USO
         */
        String termos = "Termos de Uso";

        int inicioTermos = texto.indexOf(termos);

        if (inicioTermos >= 0) {

            spannable.setSpan(
                    new ClickableSpan() {

                        @Override
                        public void onClick(@NonNull View widget) {

                            Toast.makeText(
                                    CadastroActivity.this,
                                    "Termos de Uso ainda não disponíveis.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    },
                    inicioTermos,
                    inicioTermos + termos.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        /*
         * LINK: POLÍTICA DE PRIVACIDADE
         */
        String politica = "Política de Privacidade";

        int inicioPolitica = texto.indexOf(politica);

        if (inicioPolitica >= 0) {

            spannable.setSpan(
                    new ClickableSpan() {

                        @Override
                        public void onClick(@NonNull View widget) {

                            Toast.makeText(
                                    CadastroActivity.this,
                                    "Política de Privacidade ainda não disponível.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    },
                    inicioPolitica,
                    inicioPolitica + politica.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        // Aplica o texto com links.
        txtTermos.setText(spannable);

        // Habilita o reconhecimento dos links.
        txtTermos.setMovementMethod(
                LinkMovementMethod.getInstance()
        );

        // Remove o destaque ao tocar nos links.
        txtTermos.setHighlightColor(
                android.graphics.Color.TRANSPARENT
        );
    }


    /*
     * =================================================
     * MÉTODO: abrirLogin()
     * =================================================
     *
     * OBJETIVO:
     * Retornar à tela de autenticação.
     *
     * O botão Entrar do cadastro utiliza
     * este método.
     */

    private void abrirLogin() {

        Intent intent = new Intent(
                CadastroActivity.this,
                LoginActivity.class
        );

        /*
         * Reutiliza a tela de login existente
         * quando ela já estiver na tarefa.
         */
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);

        // Encerra a tela de cadastro.
        finish();
    }
}