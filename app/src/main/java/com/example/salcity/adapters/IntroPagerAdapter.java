package com.example.salcity.adapters;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: IntroPagerAdapter
 * LINGUAGEM: JAVA
 *
 * OBJETIVO:
 * Fornecer as três páginas de introdução
 * para o componente ViewPager2.
 *
 * RESPONSABILIDADES:
 * 1. Criar os componentes visuais de cada página.
 * 2. Definir a imagem correspondente.
 * 3. Configurar o botão Começar agora.
 * 4. Informar a quantidade de páginas.
 * =====================================================
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.salcity.R;

public class IntroPagerAdapter
        extends RecyclerView.Adapter<IntroPagerAdapter.IntroViewHolder> {

    /*
     * VETOR DE IMAGENS
     *
     * Cada posição corresponde a uma introdução.
     *
     * Posição 0: Introdução 1.
     * Posição 1: Introdução 2.
     * Posição 2: Introdução 3.
     */
    private final int[] imagens = {
            R.drawable.intro_1,
            R.drawable.intro_2,
            R.drawable.intro_3
    };

    /*
     * INTERFACE: OnComecarClickListener
     *
     * OBJETIVO:
     * Comunicar à Activity principal que o usuário
     * pressionou o botão Começar agora.
     *
     * Essa abordagem evita colocar a lógica
     * de navegação diretamente no Adapter.
     */
    public interface OnComecarClickListener {

        void onComecarClick();
    }

    // Referência para o evento de clique.
    private final OnComecarClickListener listener;

    /*
     * CONSTRUTOR: IntroPagerAdapter()
     *
     * Recebe o listener responsável por tratar
     * o clique no botão Começar agora.
     *
     * @param listener
     * Objeto que receberá o evento de clique.
     */
    public IntroPagerAdapter(OnComecarClickListener listener) {

        this.listener = listener;
    }

    /*
     * CLASSE INTERNA: IntroViewHolder
     *
     * OBJETIVO:
     * Armazenar as referências dos componentes
     * pertencentes a cada página.
     *
     * Isso evita procurar os mesmos componentes
     * repetidamente durante a navegação.
     */
    public static class IntroViewHolder
            extends RecyclerView.ViewHolder {

        // Imagem principal da página.
        ImageView imgIntroducao;

        // Área clicável do botão Começar agora.
        View btnComecarAgora;

        // Contêiner principal do item.
        FrameLayout layoutItemIntro;

        /*
         * CONSTRUTOR DO VIEWHOLDER
         *
         * Localiza os componentes do item_intro.xml.
         */
        public IntroViewHolder(@NonNull View itemView) {

            super(itemView);

            imgIntroducao =
                    itemView.findViewById(R.id.imgIntroducao);

            btnComecarAgora =
                    itemView.findViewById(R.id.btnComecarAgora);

            layoutItemIntro =
                    itemView.findViewById(R.id.layoutItemIntro);
        }
    }

    /*
     * MÉTODO: onCreateViewHolder()
     *
     * OBJETIVO:
     * Criar a interface de uma página.
     *
     * LayoutInflater:
     * Converte o XML item_intro.xml em objetos View.
     *
     * @param parent
     * Contêiner que receberá a página.
     *
     * @param viewType
     * Tipo do item.
     *
     * @return
     * Um novo IntroViewHolder.
     */
    @NonNull
    @Override
    public IntroViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_intro,
                        parent,
                        false
                );

        return new IntroViewHolder(view);
    }

    /*
     * MÉTODO: onBindViewHolder()
     *
     * OBJETIVO:
     * Configurar o conteúdo da página.
     *
     * @param holder
     * Componentes visuais da página.
     *
     * @param position
     * Posição da página no ViewPager2.
     */
    @Override
    public void onBindViewHolder(
            @NonNull IntroViewHolder holder,
            int position) {

        /*
         * Define a imagem correspondente
         * à posição atual.
         */
        holder.imgIntroducao.setImageResource(
                imagens[position]
        );

        /*
         * Configura uma descrição acessível
         * para a página.
         */
        holder.itemView.setContentDescription(
                "Introdução " + (position + 1) + " de 3"
        );

        /*
         * O botão Começar agora deve aparecer
         * somente na terceira introdução.
         */
        if (position == 2) {

            configurarBotaoComecar(holder);

        } else {

            /*
             * Nas duas primeiras introduções,
             * o botão não recebe eventos de toque.
             */
            holder.btnComecarAgora.setVisibility(View.GONE);

            holder.btnComecarAgora.setOnClickListener(null);
        }
    }

    /*
     * MÉTODO: configurarBotaoComecar()
     *
     * OBJETIVO:
     * Posicionar a área clicável sobre o botão
     * Começar agora da terceira imagem.
     *
     * O posicionamento utiliza a altura
     * do contêiner da página.
     *
     * ATENÇÃO:
     * As proporções foram estimadas com base
     * na imagem original e podem exigir ajustes
     * em dispositivos com proporções diferentes.
     */
    private void configurarBotaoComecar(
            @NonNull IntroViewHolder holder) {

        // Torna o botão disponível para interação.
        holder.btnComecarAgora.setVisibility(View.VISIBLE);

        /*
         * Configura o clique.
         *
         * A Activity será responsável por
         * abrir a tela de login.
         */
        holder.btnComecarAgora.setOnClickListener(v -> {

            if (listener != null) {

                listener.onComecarClick();
            }
        });

        /*
         * Aguarda o Android calcular
         * a altura do contêiner.
         */
        holder.layoutItemIntro.post(() -> {

            int altura = holder.layoutItemIntro.getHeight();

            // Evita cálculos antes da medição.
            if (altura <= 0) {
                return;
            }

            /*
             * A imagem original apresenta o botão
             * aproximadamente entre 83% e 89%
             * da altura total.
             */
            int posicaoSuperior = (int) (altura * 0.83f);

            int alturaBotao = (int) (altura * 0.06f);

            /*
             * Recupera os parâmetros atuais
             * da área clicável.
             */
            FrameLayout.LayoutParams parametros =
                    (FrameLayout.LayoutParams)
                            holder.btnComecarAgora.getLayoutParams();

            // Define a altura da área clicável.
            parametros.height = alturaBotao;

            // Define a posição vertical.
            parametros.topMargin = posicaoSuperior;

            // Aplica os parâmetros atualizados.
            holder.btnComecarAgora.setLayoutParams(parametros);
        });
    }

    /*
     * MÉTODO: getItemCount()
     *
     * OBJETIVO:
     * Informar ao ViewPager2 quantas páginas existem.
     *
     * @return
     * Quantidade de imagens cadastradas.
     */
    @Override
    public int getItemCount() {

        return imagens.length;
    }
}