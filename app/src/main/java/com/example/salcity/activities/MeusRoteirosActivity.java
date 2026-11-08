
        package com.example.salcity.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.salcity.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * SalCity — Meus roteiros (front-end).
 * Exibe os roteiros realmente criados na CriarNovoRoteiroActivity.
 * Preserva o mesmo SharedPreferences e esquema JSON da versão anterior.
 * Os cartões seguem o visual aprovado; não há comunicação com servidor.
 */
public class MeusRoteirosActivity extends AppCompatActivity {
    // Mesmas chaves usadas na CriarNovoRoteiroActivity: NÃO ALTERAR.
    private static final String PREFS = "salcity_roteiros_criados_demo";
    private static final String CHAVE_LISTA = "roteiros_json";
    private static final int AZUL = Color.rgb(8, 47, 112);
    private static final int CINZA = Color.rgb(76, 105, 141);
    private LinearLayout listaRoteiros;
    private TextView txtQuantidade;
    private View estadoVazio;

    /** Inicializa layout e botões principais. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_roteiros);
        listaRoteiros = findViewById(R.id.listaMeusRoteiros);
        txtQuantidade = findViewById(R.id.txtQuantidadeRoteiros);
        estadoVazio = findViewById(R.id.estadoVazioRoteiros);
        findViewById(R.id.btnVoltarMeusRoteiros).setOnClickListener(v -> finish());
        findViewById(R.id.btnNovoRoteiro).setOnClickListener(v -> abrirCriacao());
        findViewById(R.id.btnCriarPrimeiroRoteiro).setOnClickListener(v -> abrirCriacao());
    }

    /** Recarrega a lista após voltar da criação ou edição local. */
    @Override protected void onResume() {
        super.onResume();
        carregarRoteiros();
    }

    /** Abre o formulário já existente, sem modificar sua lógica. */
    private void abrirCriacao() {
        startActivity(new Intent(this, CriarNovoRoteiroActivity.class));
    }

    /** Lê o JSON gravado pela tela Criar novo roteiro e renderiza os cartões. */
    private void carregarRoteiros() {
        listaRoteiros.removeAllViews();
        String json = getSharedPreferences(PREFS, MODE_PRIVATE).getString(CHAVE_LISTA, "[]");
        try {
            JSONArray roteiros = new JSONArray(json);
            int quantidade = roteiros.length();
            txtQuantidade.setText("Meus roteiros (" + quantidade + ")");
            estadoVazio.setVisibility(quantidade == 0 ? View.VISIBLE : View.GONE);
            listaRoteiros.setVisibility(quantidade == 0 ? View.GONE : View.VISIBLE);
            for (int i = quantidade - 1; i >= 0; i--) {
                JSONObject roteiro = roteiros.optJSONObject(i);
                if (roteiro != null) adicionarCartao(roteiro, i);
            }
        } catch (JSONException erro) {
            txtQuantidade.setText("Meus roteiros");
            estadoVazio.setVisibility(View.VISIBLE);
            listaRoteiros.setVisibility(View.GONE);
            Toast.makeText(this, "Não foi possível ler os roteiros locais", Toast.LENGTH_LONG).show();
        }
    }

    /** Cria um cartão com fotografia, período, tags, ações e menu. */
    private void adicionarCartao(JSONObject roteiro, int indice) {
        String nome = roteiro.optString("nome", "Roteiro sem nome");
        String inicio = roteiro.optString("inicio", "—");
        String fim = roteiro.optString("fim", "—");
        JSONArray tags = roteiro.optJSONArray("tags");

        LinearLayout cartao = new LinearLayout(this);
        cartao.setOrientation(LinearLayout.VERTICAL);
        cartao.setPadding(dp(12), dp(14), dp(12), dp(14));
        cartao.setBackgroundResource(R.drawable.bg_meus_cartao);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(16);
        listaRoteiros.addView(cartao, params);

        LinearLayout superior = new LinearLayout(this);
        superior.setOrientation(LinearLayout.HORIZONTAL);
        superior.setGravity(Gravity.TOP);
        cartao.addView(superior);

        // Reutiliza fotos reais que você já colocou em res/drawable.
        ImageView foto = new ImageView(this);
        boolean praia = contemTag(tags, "Praia");
        boolean sugerido = "sugerido".equals(roteiro.optString("origem"));
        foto.setImageResource(sugerido ? R.drawable.roteiro_capa_pelourinho
                : (praia ? R.drawable.roteiro_farol : R.drawable.roteiro_pelourinho));
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        foto.setContentDescription(praia ? "Fotografia do Farol da Barra" : "Fotografia do Pelourinho");
        GradientDrawable bordaFoto = new GradientDrawable();
        bordaFoto.setColor(Color.rgb(230, 241, 255));
        bordaFoto.setCornerRadius(dp(13));
        foto.setBackground(bordaFoto);
        foto.setClipToOutline(true);
        superior.addView(foto, new LinearLayout.LayoutParams(dp(112), dp(135)));

        LinearLayout informacoes = new LinearLayout(this);
        informacoes.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, -2, 1);
        infoParams.leftMargin = dp(12);
        superior.addView(informacoes, infoParams);

        LinearLayout cabecalho = new LinearLayout(this);
        cabecalho.setGravity(Gravity.TOP | Gravity.CENTER_VERTICAL);
        informacoes.addView(cabecalho);
        TextView titulo = texto(nome, 19, AZUL, true);
        titulo.setMaxLines(3);
        cabecalho.addView(titulo, new LinearLayout.LayoutParams(0, -2, 1));
        TextView menu = texto("⋮", 28, AZUL, true);
        menu.setGravity(Gravity.CENTER);
        menu.setContentDescription("Opções do roteiro " + nome);
        menu.setClickable(true);
        menu.setFocusable(true);
        cabecalho.addView(menu, new LinearLayout.LayoutParams(dp(32), dp(42)));
        menu.setOnClickListener(v -> abrirMenu(menu, indice, nome));

        String periodoExibido = "sugerido".equals(roteiro.optString("origem"))
                ? "★  Roteiro sugerido" : "▣  " + inicio + " a " + fim;
        TextView periodo = texto(periodoExibido, 13, CINZA, false);
        LinearLayout.LayoutParams periodoParams = new LinearLayout.LayoutParams(-1, -2);
        periodoParams.topMargin = dp(10);
        informacoes.addView(periodo, periodoParams);

        if (tags != null && tags.length() > 0) {
            LinearLayout linhaTags = new LinearLayout(this);
            linhaTags.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams linhaParams = new LinearLayout.LayoutParams(-1, -2);
            linhaParams.topMargin = dp(10);
            informacoes.addView(linhaTags, linhaParams);
            // No máximo duas tags na capa, como na referência visual.
            for (int j = 0; j < Math.min(2, tags.length()); j++) {
                String tag = tags.optString(j);
                TextView chip = texto(tag, 12, AZUL, false);
                chip.setGravity(Gravity.CENTER);
                chip.setPadding(dp(9), dp(7), dp(9), dp(7));
                chip.setBackgroundResource(R.drawable.bg_meus_tag);
                LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(0, -2, 1);
                if (j > 0) chipParams.leftMargin = dp(5);
                linhaTags.addView(chip, chipParams);
            }
        }

        View divisor = new View(this);
        divisor.setBackgroundResource(R.drawable.bg_meus_divisor);
        LinearLayout.LayoutParams divisorParams = new LinearLayout.LayoutParams(-1, dp(1));
        divisorParams.topMargin = dp(14);
        divisorParams.bottomMargin = dp(12);
        cartao.addView(divisor, divisorParams);

        /*
         * =====================================================
         * BOTÃO: VER DETALHES
         * =====================================================
         *
         * OBJETIVO:
         * Criar o botão responsável por apresentar
         * as informações do roteiro selecionado.
         *
         * FUNCIONALIDADES:
         * - Exibir o texto "Ver detalhes".
         * - Manter letras maiúsculas e minúsculas.
         * - Aplicar as cores do SalCity.
         * - Abrir o resumo do roteiro ao clicar.
         */

// Cria o contêiner horizontal dos botões.
        LinearLayout acoes = new LinearLayout(this);

// Define a orientação horizontal.
        acoes.setOrientation(LinearLayout.HORIZONTAL);

// Adiciona o contêiner ao cartão.
        cartao.addView(acoes);

// Cria o botão Ver detalhes.
        Button detalhes = new Button(this);

// Define o texto do botão.
        detalhes.setText("◉  Ver detalhes");

// Mantém letras maiúsculas e minúsculas.
        detalhes.setAllCaps(false);

// Define o tamanho do texto.
        detalhes.setTextSize(14);

// Define a cor branca para o texto.
        detalhes.setTextColor(Color.WHITE);

// Aplica o fundo azul personalizado.
        detalhes.setBackgroundResource(R.drawable.bg_meus_azul);

// Define largura proporcional e altura de 48dp.
        acoes.addView(
                detalhes,
                new LinearLayout.LayoutParams(0, dp(48), 1)
        );

// Configura o clique para apresentar o resumo.
        detalhes.setOnClickListener(v -> mostrarResumo(roteiro));

        Button adicionar = new Button(this);
        adicionar.setText("＋  Adicionar local");
        adicionar.setAllCaps(false);
        adicionar.setTextSize(13);
        adicionar.setTextColor(AZUL);
        adicionar.setBackgroundResource(R.drawable.bg_meus_botao_claro);
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        addParams.leftMargin = dp(8);
        acoes.addView(adicionar, addParams);
        adicionar.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdicionarRoteiroActivity.class);
            intent.putExtra("codigo_roteiro", nome); // Identificador provisório: apenas front-end.
            intent.putExtra("titulo_roteiro", nome);
            startActivity(intent);
        });
    }

    /** Mostra dados reais do roteiro selecionado; evita abrir o detalhe fixo de Salvador Essencial. */
    private void mostrarResumo(JSONObject roteiro) {
        StringBuilder tagsTexto = new StringBuilder();
        JSONArray tags = roteiro.optJSONArray("tags");
        if (tags != null) for (int i = 0; i < tags.length(); i++) {
            if (i > 0) tagsTexto.append(", ");
            tagsTexto.append(tags.optString(i));
        }
        int valor = roteiro.optInt("orcamento", 0);
        boolean sugerido = "sugerido".equals(roteiro.optString("origem"));
        String mensagem = (sugerido ? "Origem: Roteiro sugerido"
                : "Período: " + roteiro.optString("inicio", "—") + " a " + roteiro.optString("fim", "—"))
                + "\nInteresses: " + (tagsTexto.length() == 0 ? "Não informados" : tagsTexto)
                + (sugerido ? "" : "\nOrçamento por pessoa: " + (valor >= 500 ? "R$ 500+" : "R$ " + valor));
        String descricao = roteiro.optString("descricao", "");
        if (!descricao.isEmpty()) mensagem += "\n\n" + descricao;
        new AlertDialog.Builder(this)
                .setTitle(roteiro.optString("nome", "Roteiro"))
                .setMessage(mensagem)
                .setPositiveButton("Fechar", null)
                .show();
    }

    /** Menu contextual: visualizar informações ou excluir o registro local. */
    private void abrirMenu(View ancora, int indice, String nome) {
        PopupMenu popup = new PopupMenu(this, ancora);
        popup.getMenu().add("Ver informações").setOnMenuItemClickListener(item -> {
            try {
                JSONArray lista = lerLista();
                JSONObject roteiro = lista.optJSONObject(indice);
                if (roteiro != null) mostrarResumo(roteiro);
            } catch (JSONException e) {
                Toast.makeText(this, "Não foi possível abrir o roteiro", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.getMenu().add("Excluir roteiro").setOnMenuItemClickListener(item -> {
            new AlertDialog.Builder(this)
                    .setTitle("Excluir roteiro?")
                    .setMessage("Deseja excluir \"" + nome + "\" deste dispositivo?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Excluir", (d, w) -> excluirRoteiro(indice))
                    .show();
            return true;
        });
        popup.show();
    }

    /** Remove apenas o item escolhido do JSON local, mantendo os demais. */
    private void excluirRoteiro(int indice) {
        try {
            JSONArray atual = lerLista();
            JSONArray nova = new JSONArray();
            for (int i = 0; i < atual.length(); i++) {
                if (i != indice) nova.put(atual.get(i));
            }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(CHAVE_LISTA, nova.toString()).apply();
            carregarRoteiros();
            Toast.makeText(this, "Roteiro excluído", Toast.LENGTH_SHORT).show();
        } catch (JSONException erro) {
            Toast.makeText(this, "Erro ao excluir roteiro", Toast.LENGTH_SHORT).show();
        }
    }

    /** Obtém os roteiros salvos, no mesmo formato usado pela tela de criação. */
    private JSONArray lerLista() throws JSONException {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        return new JSONArray(prefs.getString(CHAVE_LISTA, "[]"));
    }

    /** Verifica se uma tag está selecionada, para escolher uma fotografia representativa. */
    private boolean contemTag(JSONArray tags, String procurada) {
        if (tags == null) return false;
        for (int i = 0; i < tags.length(); i++) {
            if (procurada.equalsIgnoreCase(tags.optString(i))) return true;
        }
        return false;
    }

    /** Cria texto com tamanho, cor e peso padronizados. */
    private TextView texto(String conteudo, int tamanho, int cor, boolean negrito) {
        TextView view = new TextView(this);
        view.setText(conteudo);
        view.setTextSize(tamanho);
        view.setTextColor(cor);
        if (negrito) view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    /** Converte dp para pixels, preservando a escala entre dispositivos. */
    private int dp(float valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
