
        package com.example.salcity.activities;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Button;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.salcity.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * SALCITY — RoteiroDetalhadoActivity (somente front-end).
 * Exibe o roteiro demonstrativo Salvador Essencial com três abas,
 * favoritos e botão de adição simulados localmente.
 * Não utiliza banco de dados, back-end nem geração automática de roteiros.
 */
public class RoteiroDetalhadoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int AZUL = Color.rgb(7, 51, 110);
    private static final int AZUL_DESTAQUE = Color.rgb(0, 119, 245);
    private static final String PREFS = "salcity_roteiros_ui";
    private static final String CHAVE_FAVORITO = "favorito_salvador_essencial";


    // Conteúdo ilustrativo do protótipo, NÃO proveniente de um serviço de rotas.
    private static final String[] HORARIOS = {"08:00", "09:30", "11:30", "13:00", "15:00", "17:00"};
    private static final String[] NOMES = {
            "Elevador Lacerda", "Mercado Modelo", "Pelourinho", "Almoço",
            "Igreja do Bonfim", "Pôr do sol no Farol da Barra"
    };
    private static final String[] DESCRICOES = {
            "Vista panorâmica da cidade", "Artesanato e cultura local",
            "Centro histórico e cultural", "Restaurante recomendado",
            "Patrimônio histórico", "Encerramento do roteiro"
    };
    private static final int[] FOTOS = {
            R.drawable.roteiro_elevador, R.drawable.roteiro_mercado,
            R.drawable.roteiro_pelourinho, R.drawable.roteiro_almoco,
            R.drawable.roteiro_bonfim, R.drawable.roteiro_farol
    };
    // Coordenadas aproximadas para demonstração visual do mapa.
    private static final LatLng[] PONTOS = {
            new LatLng(-12.9741, -38.5137),
            new LatLng(-12.9718, -38.5147),
            new LatLng(-12.9730, -38.5090),
            new LatLng(-12.9730, -38.5090), // Almoço: posição ilustrativa.
            new LatLng(-12.9237, -38.5087),
            new LatLng(-13.0106, -38.5325)
    };

    private LinearLayout conteudoRoteiro;
    private FrameLayout conteudoMapa;
    private TextView conteudoDetalhes, abaRoteiro, abaMapa, abaDetalhes;
    private ImageButton btnFavorito;
    private Button btnAdicionar;
    private Button btnSalvar;

    // Mesmo armazenamento da CriarNovoRoteiroActivity e MeusRoteirosActivity.
    private static final String PREFS_ROTEIROS = "salcity_roteiros_criados_demo";
    private static final String CHAVE_LISTA = "roteiros_json";
    private static final String ID_ESSENCIAL = "sugerido_salvador_essencial";
    private SharedPreferences preferencias;
    private boolean mapaCriado = false;

    /*
     * =====================================================
     * MÉTODO: onCreate()
     * =====================================================
     *
     * OBJETIVO:
     * Inicializar a tela de detalhes do roteiro.
     *
     * RESPONSABILIDADES:
     * 1. Carregar o layout XML.
     * 2. Inicializar o SharedPreferences.
     * 3. Associar os componentes.
     * 4. Configurar os botões.
     * 5. Construir a linha do tempo.
     * 6. Configurar a descrição.
     * 7. Selecionar a aba inicial.
     * 8. Atualizar o botão favorito.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Carrega o layout da tela.
        setContentView(R.layout.activity_roteiro_detalhado);

        // Inicializa as preferências locais.
        preferencias = getSharedPreferences(
                PREFS,
                MODE_PRIVATE
        );

        // Associa os componentes do XML.
        associarComponentes();

        // Configura os eventos dos botões.
        configurarBotoes();

        // Constrói a linha do tempo.
        construirLinhaDoTempo();

        // Configura o conteúdo da aba Detalhes.
        configurarDescricao();

        // Exibe inicialmente a aba Roteiro.
        selecionarAba(0);

        // Atualiza a aparência do favorito.
        atualizarFavorito();
        atualizarBotaoSalvar();
    }

    /** Obtém as referências dos elementos definidos no layout XML. */
    private void associarComponentes() {
        conteudoRoteiro = findViewById(R.id.conteudoRoteiro);
        conteudoMapa = findViewById(R.id.conteudoMapa);
        conteudoDetalhes = findViewById(R.id.conteudoDetalhes);
        abaRoteiro = findViewById(R.id.abaRoteiro);
        abaMapa = findViewById(R.id.abaMapa);
        abaDetalhes = findViewById(R.id.abaDetalhes);
        btnFavorito = findViewById(R.id.btnFavoritoDetalhe);
        btnAdicionar = findViewById(R.id.btnAdicionarRoteiro);
        btnSalvar = findViewById(R.id.btnSalvarRoteiro);
    }

    /** Liga os controles visuais a ações de front-end. */
    private void configurarBotoes() {
        findViewById(R.id.btnVoltarDetalhe).setOnClickListener(v -> finish());
        findViewById(R.id.btnNotificacaoDetalhe).setOnClickListener(v ->
                Toast.makeText(this, "Notificações disponíveis em uma etapa futura", Toast.LENGTH_SHORT).show());
        abaRoteiro.setOnClickListener(v -> selecionarAba(0));
        abaMapa.setOnClickListener(v -> selecionarAba(1));
        abaDetalhes.setOnClickListener(v -> selecionarAba(2));
        btnFavorito.setOnClickListener(v -> {
            boolean atual = preferencias.getBoolean(CHAVE_FAVORITO, false);
            preferencias.edit().putBoolean(CHAVE_FAVORITO, !atual).apply();
            atualizarFavorito();
        });
        /*
         * =====================================================
         * BOTÃO: ADICIONAR AO MEU ROTEIRO
         * =====================================================
         *
         * OBJETIVO:
         * Abrir a tela AdicionarRoteiroActivity.
         *
         * FUNCIONAMENTO:
         * 1. Identifica o clique do usuário.
         * 2. Cria uma Intent para a próxima Activity.
         * 3. Envia informações do roteiro selecionado.
         * 4. Abre a tela de adição de locais.
         *
         * OBSERVAÇÃO:
         * Esta funcionalidade pertence ao front-end.
         * Não realiza operações no banco de dados.
         */

        btnSalvar.setOnClickListener(v -> salvarRoteiro());

        btnAdicionar.setOnClickListener(v -> {

            // Cria a Intent para abrir a próxima tela.
            Intent intent = new Intent(
                    RoteiroDetalhadoActivity.this,
                    AdicionarRoteiroActivity.class
            );

            // Envia o identificador do roteiro.
            intent.putExtra(
                    "codigo_roteiro",
                    "essencial"
            );

            // Envia o nome do roteiro.
            intent.putExtra(
                    "titulo_roteiro",
                    "Salvador Essencial"
            );

            // Inicia a tela Adicionar ao Roteiro.
            startActivity(intent);
        });
    }

    /** Atualiza o botão sempre que a tela volta a ficar visível (inclusive após exclusões). */
    @Override
    protected void onResume() {
        super.onResume();
        if (btnSalvar != null) atualizarBotaoSalvar();
    }

    /** Recupera a lista compartilhada com Meus Roteiros, sem sobrescrever registros. */
    private JSONArray lerRoteirosSalvos() throws JSONException {
        String dados = getSharedPreferences(PREFS_ROTEIROS, MODE_PRIVATE)
                .getString(CHAVE_LISTA, "[]");
        return new JSONArray(dados);
    }

    /** Identifica apenas este roteiro sugerido pelo ID fixo, sem confundir nomes iguais. */
    private boolean roteiroJaSalvo(JSONArray lista) {
        for (int i = 0; i < lista.length(); i++) {
            JSONObject item = lista.optJSONObject(i);
            if (item != null && ID_ESSENCIAL.equals(item.optString("id"))) return true;
        }
        return false;
    }

    /** Atualiza a aparência de acordo com os dados realmente persistidos. */
    private void atualizarBotaoSalvar() {
        try {
            boolean salvo = roteiroJaSalvo(lerRoteirosSalvos());
            btnSalvar.setText(salvo ? "Roteiro salvo ✓" : "Salvar roteiro");
            btnSalvar.setEnabled(!salvo);
            btnSalvar.setAlpha(salvo ? 0.72f : 1f);
        } catch (JSONException erro) {
            btnSalvar.setText("Salvar roteiro");
            btnSalvar.setEnabled(true);
        }
    }

    /**
     * Salva o roteiro demonstrativo no mesmo JSON utilizado por Meus Roteiros.
     * Não substitui a lista existente, não chama o back-end e impede duplicação por ID.
     */
    private void salvarRoteiro() {
        try {
            JSONArray lista = lerRoteirosSalvos();
            if (roteiroJaSalvo(lista)) {
                atualizarBotaoSalvar();
                Toast.makeText(this, "Este roteiro já está salvo", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject roteiro = new JSONObject();
            roteiro.put("id", ID_ESSENCIAL);
            roteiro.put("origem", "sugerido");
            roteiro.put("nome", "Salvador Essencial");
            roteiro.put("inicio", "");
            roteiro.put("fim", "");
            roteiro.put("orcamento", 0);
            roteiro.put("descricao", "Roteiro demonstrativo de 1 dia, com 6 paradas " +
                    "e duração aproximada de 8 horas. Horários e trajetos ilustrativos.");

            JSONArray tags = new JSONArray();
            tags.put("Cultura");
            tags.put("História");
            roteiro.put("tags", tags);

            // Preserva a ordem e os dados dos roteiros que já foram criados.
            lista.put(roteiro);
            boolean gravou = getSharedPreferences(PREFS_ROTEIROS, MODE_PRIVATE)
                    .edit().putString(CHAVE_LISTA, lista.toString()).commit();
            if (gravou) {
                atualizarBotaoSalvar();
                Toast.makeText(this, "Roteiro salvo com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Não foi possível salvar o roteiro", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException erro) {
            Toast.makeText(this, "Erro ao ler ou salvar os roteiros locais", Toast.LENGTH_LONG).show();
        }
    }

    /** Atualiza o coração usando a preferência local (sem sincronização). */
    private void atualizarFavorito() {
        boolean favorito = preferencias.getBoolean(CHAVE_FAVORITO, false);
        btnFavorito.setImageTintList(ColorStateList.valueOf(favorito ? Color.RED : AZUL));
        btnFavorito.setContentDescription(favorito ? "Remover dos favoritos" : "Adicionar aos favoritos");
    }

    /** Alterna entre a linha do tempo, o mapa e o texto de detalhes. */
    private void selecionarAba(int indice) {
        conteudoRoteiro.setVisibility(indice == 0 ? View.VISIBLE : View.GONE);
        conteudoMapa.setVisibility(indice == 1 ? View.VISIBLE : View.GONE);
        conteudoDetalhes.setVisibility(indice == 2 ? View.VISIBLE : View.GONE);
        TextView[] abas = {abaRoteiro, abaMapa, abaDetalhes};
        for (int i = 0; i < abas.length; i++) {
            abas[i].setTextColor(i == indice ? AZUL_DESTAQUE : AZUL);
            abas[i].setTypeface(null, i == indice ? Typeface.BOLD : Typeface.NORMAL);
            abas[i].setPaintFlags(i == indice
                    ? (abas[i].getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG)
                    : (abas[i].getPaintFlags() & ~android.graphics.Paint.UNDERLINE_TEXT_FLAG));
            abas[i].setSelected(i == indice);
        }
        if (indice == 1 && !mapaCriado) inicializarMapa();
    }

    /** Cria as seis paradas de forma responsiva, sem depender de RecyclerView. */
    private void construirLinhaDoTempo() {
        conteudoRoteiro.removeAllViews();
        for (int i = 0; i < NOMES.length; i++) {
            LinearLayout linha = new LinearLayout(this);
            linha.setGravity(Gravity.CENTER_VERTICAL);
            linha.setOrientation(LinearLayout.HORIZONTAL);
            linha.setPadding(0, dp(7), 0, dp(7));
            linha.setMinimumHeight(dp(100));

            // Coluna de horário (à esquerda).
            TextView horario = new TextView(this);
            horario.setText("○  " + HORARIOS[i]);
            horario.setTextSize(15);
            horario.setTextColor(AZUL);
            horario.setTypeface(null, Typeface.BOLD);
            linha.addView(horario, new LinearLayout.LayoutParams(dp(93), -2));

            // Miniatura própria de cada ponto turístico.
            ImageView imagem = new ImageView(this);
            imagem.setImageResource(FOTOS[i]);
            imagem.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imagem.setBackgroundResource(R.drawable.bg_detalhe_thumb);
            imagem.setClipToOutline(true);
            imagem.setContentDescription("Fotografia de " + NOMES[i]);
            linha.addView(imagem, new LinearLayout.LayoutParams(dp(68), dp(76)));

            // Nome e descrição do local, com quebra de linha automática.
            LinearLayout textos = new LinearLayout(this);
            textos.setOrientation(LinearLayout.VERTICAL);
            textos.setPadding(dp(12), 0, 0, 0);
            TextView nome = new TextView(this);
            nome.setText(NOMES[i]);
            nome.setTextColor(AZUL);
            nome.setTypeface(null, Typeface.BOLD);
            nome.setTextSize(17);
            TextView descricao = new TextView(this);
            descricao.setText(DESCRICOES[i]);
            descricao.setTextColor(Color.rgb(69, 99, 128));
            descricao.setTextSize(14);
            textos.addView(nome);
            textos.addView(descricao);
            linha.addView(textos, new LinearLayout.LayoutParams(0, -2, 1f));
            conteudoRoteiro.addView(linha);
        }
    }

    /** Texto estático da aba Detalhes. Informações exemplificativas do protótipo. */
    private void configurarDescricao() {
        conteudoDetalhes.setText(
                "Salvador Essencial\n\n" +
                        "Um passeio demonstrativo por alguns dos lugares mais conhecidos de Salvador. " +
                        "A proposta combina história, cultura e gastronomia.\n\n" +
                        "Duração: 1 dia\n" +
                        "Paradas apresentadas: 6\n" +
                        "Tempo estimado: aproximadamente 8 horas\n\n" +
                        "Observação: horários, trajetos e locais de alimentação são exemplos do protótipo. " +
                        "O cálculo real de deslocamento e a personalização serão realizados futuramente pelo back-end."
        );
    }

    /** Adiciona o fragment do Google Maps apenas quando a aba Mapa é aberta. */
    private void inicializarMapa() {
        mapaCriado = true;
        SupportMapFragment fragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.conteudoMapa, fragment)
                .commit();
        fragment.getMapAsync(this);
    }

    /** Exibe marcadores demonstrativos, sem traçar rotas nem consultar APIs extras. */
    @Override
    public void onMapReady(@NonNull GoogleMap mapa) {
        for (int i = 0; i < PONTOS.length; i++) {
            mapa.addMarker(new MarkerOptions().position(PONTOS[i]).title(HORARIOS[i] + " — " + NOMES[i]));
        }
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-12.968, -38.512), 11.5f));
        mapa.getUiSettings().setZoomControlsEnabled(true);
    }

    /** Converte unidades dp para pixels do aparelho. */
    private int dp(float valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}

