package com.example.salcity.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.salcity.R;

/**
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: RoteirosSugeridosActivity
 * LINGUAGEM: JAVA
 * =====================================================
 *
 * OBJETIVO:
 * Apresentar os roteiros turísticos sugeridos
 * de acordo com a interface do aplicativo.
 *
 * RESPONSABILIDADES:
 * 1. Exibir os roteiros demonstrativos.
 * 2. Configurar os cartões.
 * 3. Permitir favoritar roteiros.
 * 4. Armazenar favoritos localmente.
 * 5. Abrir a tela de detalhes do roteiro.
 * 6. Permitir retornar à tela anterior.
 *
 * OBSERVAÇÃO:
 * Nesta etapa, estamos desenvolvendo apenas
 * o front-end do aplicativo.
 *
 * Os roteiros são demonstrativos.
 * A geração personalizada será responsabilidade
 * do back-end.
 * =====================================================
 */

public class RoteirosSugeridosActivity extends AppCompatActivity {

    /*
     * =================================================
     * CONSTANTES
     * =================================================
     */

    // Nome do arquivo utilizado para armazenar favoritos.
    private static final String PREFERENCIAS =
            "salcity_roteiros_favoritos";

    /*
     * =================================================
     * ATRIBUTOS
     * =================================================
     */

    // Armazena localmente os roteiros favoritos.
    private SharedPreferences preferencias;

    /*
     * =================================================
     * MÉTODO: onCreate()
     * =================================================
     *
     * Executado quando a Activity é iniciada.
     *
     * Responsável por:
     * - Carregar o layout XML.
     * - Inicializar as preferências.
     * - Configurar o botão voltar.
     * - Configurar os cartões dos roteiros.
     * - Exibir o texto informativo.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Carrega o layout da tela.
        setContentView(R.layout.activity_roteiros_sugeridos);

        // Inicializa o armazenamento local.
        preferencias = getSharedPreferences(
                PREFERENCIAS,
                MODE_PRIVATE
        );

        /*
         * BOTÃO VOLTAR
         *
         * Retorna à tela anterior.
         */
        findViewById(R.id.btnVoltarRoteiros)
                .setOnClickListener(v -> finish());

        /*
         * CARTÕES DOS ROTEIROS
         *
         * Cada cartão possui seus próprios
         * botões de favorito e detalhes.
         */

        View essencial = findViewById(
                R.id.cartaoEssencial
        );

        View solMar = findViewById(
                R.id.cartaoSolMar
        );

        /*
         * Configura os cartões individualmente.
         */

        configurarCartao(
                essencial,
                "essencial",
                "Salvador Essencial"
        );

        configurarCartao(
                solMar,
                "sol_mar",
                "Sol e Mar"
        );

        /*
         * TEXTO INFORMATIVO
         */

        TextView resumo = findViewById(
                R.id.txtResumoRoteiros
        );

        resumo.setText(
                "Com base nos seus filtros, encontramos " +
                        "2 opções incríveis para você!"
        );
    }

    /*
     * =================================================
     * MÉTODO: configurarCartao()
     * =================================================
     *
     * OBJETIVO:
     * Configurar os botões existentes em cada cartão.
     *
     * PARÂMETROS:
     *
     * @param cartao
     * Componente visual do roteiro.
     *
     * @param codigo
     * Identificador do roteiro.
     *
     * @param titulo
     * Nome do roteiro.
     */

    private void configurarCartao(
            View cartao,
            String codigo,
            String titulo
    ) {

        /*
         * Localiza o botão favorito
         * dentro do cartão específico.
         */

        ImageButton favorito = cartao.findViewById(
                R.id.btnFavorito
        );

        /*
         * Localiza o botão Ver detalhes
         * dentro do cartão específico.
         */

        TextView detalhes = cartao.findViewById(
                R.id.btnDetalhes
        );

        /*
         * Atualiza a aparência inicial
         * do botão favorito.
         */

        atualizarFavorito(
                favorito,
                codigo,
                titulo
        );

        /*
         * EVENTO: FAVORITAR
         */

        favorito.setOnClickListener(v -> {

            // Consulta o estado atual do favorito.
            boolean favoritoAtual = preferencias.getBoolean(
                    codigo,
                    false
            );

            // Inverte o estado.
            boolean novoValor = !favoritoAtual;

            // Salva o novo estado localmente.
            preferencias.edit()
                    .putBoolean(codigo, novoValor)
                    .apply();

            // Atualiza a aparência do botão.
            atualizarFavorito(
                    favorito,
                    codigo,
                    titulo
            );

            // Exibe uma mensagem de confirmação.
            String mensagem = novoValor
                    ? "Adicionado aos favoritos"
                    : "Removido dos favoritos";

            Toast.makeText(
                    RoteirosSugeridosActivity.this,
                    mensagem,
                    Toast.LENGTH_SHORT
            ).show();
        });

        /*
         * EVENTO: VER DETALHES
         *
         * Envia o código e o título do roteiro
         * para a próxima Activity.
         */

        detalhes.setOnClickListener(v -> {

            abrirDetalhes(
                    codigo,
                    titulo
            );

        });
    }

    /*
     * =================================================
     * MÉTODO: atualizarFavorito()
     * =================================================
     *
     * OBJETIVO:
     * Atualizar a aparência do botão favorito.
     *
     * O estado é recuperado do SharedPreferences.
     */

    private void atualizarFavorito(
            ImageButton botao,
            String codigo,
            String titulo
    ) {

        // Consulta se o roteiro está favoritado.
        boolean salvo = preferencias.getBoolean(
                codigo,
                false
        );

        /*
         * Se estiver favoritado:
         * aplica a cor vermelha.
         *
         * Caso contrário:
         * remove o filtro de cor.
         */

        if (salvo) {

            botao.setColorFilter(
                    getColor(android.R.color.holo_red_dark)
            );

        } else {

            botao.clearColorFilter();

        }

        /*
         * Define uma descrição acessível
         * para leitores de tela.
         */

        botao.setContentDescription(
                (salvo
                        ? "Remover dos favoritos: "
                        : "Adicionar aos favoritos: ")
                        + titulo
        );

        // Atualiza o estado selecionado.
        botao.setSelected(salvo);
    }

    /*
     * =================================================
     * MÉTODO: abrirDetalhes()
     * =================================================
     *
     * OBJETIVO:
     * Abrir a tela de detalhes do roteiro.
     *
     * PARÂMETROS:
     *
     * @param codigo
     * Identificador do roteiro selecionado.
     *
     * @param titulo
     * Nome do roteiro selecionado.
     *
     * FUNCIONAMENTO:
     * 1. Cria uma Intent.
     * 2. Adiciona informações do roteiro.
     * 3. Abre a RoteiroDetalhadoActivity.
     *
     * OBSERVAÇÃO:
     * O envio dos dados está preparado para
     * permitir a futura personalização da tela.
     */

    private void abrirDetalhes(
            String codigo,
            String titulo
    ) {

        /*
         * Cria a Intent para abrir
         * a tela de detalhes.
         */

        Intent intent = new Intent(
                RoteirosSugeridosActivity.this,
                RoteiroDetalhadoActivity.class
        );

        /*
         * Envia o identificador do roteiro.
         */

        intent.putExtra(
                "codigo_roteiro",
                codigo
        );

        /*
         * Envia o nome do roteiro.
         */

        intent.putExtra(
                "titulo_roteiro",
                titulo
        );

        /*
         * Abre a tela de detalhes.
         */

        startActivity(intent);
    }
}