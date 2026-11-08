package com.example.salcity.activities;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: IntroActivity
 * LINGUAGEM: JAVA
 *
 * OBJETIVO:
 * Controlar o fluxo completo das três introduções.
 *
 * RESPONSABILIDADES:
 * 1. Inicializar o ViewPager2.
 * 2. Configurar o Adapter.
 * 3. Controlar os indicadores.
 * 4. Permitir navegação por toque e deslizamento.
 * 5. Abrir a tela de login.
 * 6. Registrar a conclusão da apresentação.
 * =====================================================
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.salcity.adapters.IntroPagerAdapter;
import com.example.salcity.R;

public class IntroActivity extends AppCompatActivity {

    /*
     * =================================================
     * CONSTANTES
     * =================================================
     */

    // Nome do arquivo de preferências do aplicativo.
    private static final String PREFS_NAME = "SalCityPrefs";

    // Chave utilizada para registrar a conclusão.
    private static final String KEY_INTRO_CONCLUIDA =
            "intro_concluida";

    /*
     * =================================================
     * COMPONENTES
     * =================================================
     */

    // Componente que controla as três páginas.
    private ViewPager2 viewPagerIntro;

    // Adapter responsável pelo conteúdo das páginas.
    private IntroPagerAdapter adapter;

    // Botão que permite ignorar a apresentação.
    private TextView btnPular;

    // Indicadores inferiores.
    private View indicador1;
    private View indicador2;
    private View indicador3;

    /*
     * =================================================
     * MÉTODO: onCreate()
     * =================================================
     *
     * Executado quando a Activity é iniciada.
     *
     * Verifica se a introdução já foi concluída
     * antes de carregar a interface.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /*
         * Verifica se o usuário já concluiu
         * ou pulou as introduções anteriormente.
         */
        if (introducaoJaConcluida()) {

            abrirLoginSemRegistrar();

            return;
        }

        /*
         * Exibe a apresentação em tela cheia.
         *
         * Isso evita duplicar a barra de status
         * nas imagens que já contêm esses elementos.
         *
         * OBSERVAÇÃO:
         * É uma solução provisória para as artes
         * originais. Na versão final, utilizaremos
         * a barra de status real do Android.
         */
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Carrega a interface principal.
        setContentView(R.layout.activity_intro);

        // Localiza os componentes XML.
        inicializarComponentes();

        // Configura o ViewPager2.
        configurarViewPager();

        // Configura os indicadores.
        configurarIndicadores();

        // Configura o botão Pular.
        configurarBotaoPular();
    }

    /*
     * =================================================
     * MÉTODO: inicializarComponentes()
     * =================================================
     *
     * OBJETIVO:
     * Associar os componentes XML às variáveis Java.
     */
    private void inicializarComponentes() {

        viewPagerIntro =
                findViewById(R.id.viewPagerIntro);

        btnPular =
                findViewById(R.id.btnPular);

        indicador1 =
                findViewById(R.id.indicador1);

        indicador2 =
                findViewById(R.id.indicador2);

        indicador3 =
                findViewById(R.id.indicador3);
    }

    /*
     * =================================================
     * MÉTODO: configurarViewPager()
     * =================================================
     *
     * OBJETIVO:
     * Inicializar a navegação entre as três páginas.
     *
     * O ViewPager2 já reconhece gestos horizontais.
     * Portanto, não precisamos de GestureDetector.
     */
    private void configurarViewPager() {

        /*
         * Cria o Adapter.
         *
         * Quando o usuário tocar em Começar agora,
         * será executado o método finalizarIntroducao().
         */
        adapter = new IntroPagerAdapter(
                this::finalizarIntroducao
        );

        // Associa o Adapter ao ViewPager2.
        viewPagerIntro.setAdapter(adapter);

        // Define a orientação horizontal.
        viewPagerIntro.setOrientation(
                ViewPager2.ORIENTATION_HORIZONTAL
        );

        // Permite a navegação pelo dedo.
        viewPagerIntro.setUserInputEnabled(true);

        /*
         * Registra um observador para identificar
         * mudanças na página selecionada.
         */
        viewPagerIntro.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {

                    /*
                     * MÉTODO: onPageSelected()
                     *
                     * Executado quando uma página
                     * é selecionada.
                     *
                     * @param position
                     * Índice da página atual.
                     */
                    @Override
                    public void onPageSelected(int position) {

                        super.onPageSelected(position);

                        // Atualiza os indicadores.
                        atualizarIndicadores(position);
                    }
                }
        );

        // Define inicialmente a primeira página.
        viewPagerIntro.setCurrentItem(0, false);

        // Atualiza o indicador inicial.
        atualizarIndicadores(0);
    }

    /*
     * =================================================
     * MÉTODO: configurarIndicadores()
     * =================================================
     *
     * OBJETIVO:
     * Permitir que o usuário toque nos indicadores
     * para navegar diretamente entre as páginas.
     */
    private void configurarIndicadores() {

        /*
         * INDICADOR 1
         *
         * Navega para a introdução 1.
         */
        indicador1.setOnClickListener(v -> {

            viewPagerIntro.setCurrentItem(0, true);
        });

        /*
         * INDICADOR 2
         *
         * Navega para a introdução 2.
         */
        indicador2.setOnClickListener(v -> {

            viewPagerIntro.setCurrentItem(1, true);
        });

        /*
         * INDICADOR 3
         *
         * Navega para a introdução 3.
         */
        indicador3.setOnClickListener(v -> {

            viewPagerIntro.setCurrentItem(2, true);
        });
    }

    /*
     * =================================================
     * MÉTODO: atualizarIndicadores()
     * =================================================
     *
     * OBJETIVO:
     * Alterar a aparência dos indicadores
     * conforme a página selecionada.
     *
     * @param paginaAtual
     * Índice da página selecionada.
     */
    private void atualizarIndicadores(int paginaAtual) {

        /*
         * Inicialmente, todos os indicadores
         * recebem a aparência inativa.
         */
        indicador1.setBackgroundResource(
                R.drawable.indicador_inativo
        );

        indicador2.setBackgroundResource(
                R.drawable.indicador_inativo
        );

        indicador3.setBackgroundResource(
                R.drawable.indicador_inativo
        );

        /*
         * Depois, apenas o indicador da página
         * atual recebe a aparência ativa.
         */
        switch (paginaAtual) {

            case 0:

                indicador1.setBackgroundResource(
                        R.drawable.indicador_ativo
                );

                break;

            case 1:

                indicador2.setBackgroundResource(
                        R.drawable.indicador_ativo
                );

                break;

            case 2:

                indicador3.setBackgroundResource(
                        R.drawable.indicador_ativo
                );

                break;
        }
    }

    /*
     * =================================================
     * MÉTODO: configurarBotaoPular()
     * =================================================
     *
     * OBJETIVO:
     * Permitir que o usuário ignore as introduções.
     */
    private void configurarBotaoPular() {

        btnPular.setOnClickListener(v -> {

            finalizarIntroducao();
        });
    }

    /*
     * =================================================
     * MÉTODO: introducaoJaConcluida()
     * =================================================
     *
     * OBJETIVO:
     * Consultar as preferências do aplicativo.
     *
     * @return
     * true: introdução já concluída.
     * false: introdução ainda não concluída.
     */
    private boolean introducaoJaConcluida() {

        SharedPreferences preferencias =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );

        return preferencias.getBoolean(
                KEY_INTRO_CONCLUIDA,
                false
        );
    }

    /*
     * =================================================
     * MÉTODO: finalizarIntroducao()
     * =================================================
     *
     * OBJETIVO:
     * Registrar que o usuário concluiu ou pulou
     * a apresentação e abrir o login.
     *
     * SharedPreferences:
     * Armazena pequenas informações persistentes.
     */
    private void finalizarIntroducao() {

        // Acessa as preferências do SalCity.
        SharedPreferences preferencias =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );

        /*
         * Registra que a introdução foi concluída.
         *
         * apply():
         * Solicita o armazenamento da alteração.
         */
        preferencias.edit()
                .putBoolean(KEY_INTRO_CONCLUIDA, true)
                .apply();

        // Abre a tela de login.
        abrirLoginSemRegistrar();
    }

    /*
     * =================================================
     * MÉTODO: abrirLoginSemRegistrar()
     * =================================================
     *
     * OBJETIVO:
     * Abrir a tela de login sem alterar as preferências.
     *
     * É utilizado quando a introdução já foi concluída
     * ou após o registro da conclusão.
     */
    private void abrirLoginSemRegistrar() {

        Intent intent = new Intent(
                IntroActivity.this,
                LoginActivity.class
        );

        /*
         * Remove as Activities anteriores da tarefa
         * para impedir o retorno às introduções.
         */
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        // Inicia a tela de login.
        startActivity(intent);

        // Encerra a Activity atual.
        finish();
    }
}