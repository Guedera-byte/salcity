package com.example.salcity.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.salcity.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Tela demonstrativa de adição de locais a roteiros. Não utiliza back-end. */
public class AdicionarRoteiroActivity extends AppCompatActivity {
    private static final int AZUL = Color.rgb(3, 39, 83);
    private static final int AMARELO = Color.rgb(255, 187, 0);
    private static final int CINZA = Color.rgb(105, 130, 157);
    private static final String PREFS = "salcity_locais_adicionados";

    // Dados demonstrativos: não são resultados de geolocalização.
    private static final class Local {
        final String id, nome, categoria, distancia, imagem;
        Local(String id, String nome, String categoria, String distancia, String imagem) {
            this.id = id; this.nome = nome; this.categoria = categoria;
            this.distancia = distancia; this.imagem = imagem;
        }
    }
    private final List<Local> locais = Arrays.asList(
            new Local("solar", "Solar do Unhão", "Museu", "2,1 km", "foto_solar_unhao"),
            new Local("porto", "Praia do Porto da Barra", "Praia", "3,5 km", "foto_porto_barra"),
            new Local("rio", "Rio Vermelho", "Vida noturna", "4,2 km", "foto_rio_vermelho"),
            new Local("casa", "Casa do Rio Vermelho", "Museu", "4,0 km", "foto_casa_rio_vermelho")
    );
    private final String[] categorias = {"Ponto turístico", "Restaurante", "Evento", "Atividade", "Outro"};
    private final List<TextView> botoesCategoria = new ArrayList<>();
    private String categoriaAtual = "Ponto turístico";
    private LinearLayout listaResultados;
    private EditText campoBusca;
    private Spinner seletorRoteiro;
    private SharedPreferences preferencias;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_roteiro);
        preferencias = getSharedPreferences(PREFS, MODE_PRIVATE);
        findViewById(R.id.btnVoltarAdicionar).setOnClickListener(v -> finish());
        listaResultados = findViewById(R.id.listaResultadosAdicionar);
        campoBusca = findViewById(R.id.campoBuscaAdicionar);
        seletorRoteiro = findViewById(R.id.spinnerRoteiroAdicionar);
        configurarSeletor();
        configurarCategorias();
        campoBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { atualizarResultados(); }
            @Override public void afterTextChanged(Editable s) { }
        });
        atualizarResultados();
    }

    /** Seleção de roteiro local; a lista poderá vir do back-end futuramente. */
    private void configurarSeletor() {
        String[] roteiros = {"Salvador Essencial  •  1 dia · 5 locais", "Sol e Mar  •  1 dia · 4 locais"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roteiros);
        seletorRoteiro.setAdapter(adapter);
    }

    /** Chips clicáveis; o filtro demonstra categorias, sem consultas remotas. */
    private void configurarCategorias() {
        LinearLayout linha1 = findViewById(R.id.categoriasLinha1);
        LinearLayout linha2 = findViewById(R.id.categoriasLinha2);
        for (int i = 0; i < categorias.length; i++) {
            final String categoria = categorias[i];
            TextView chip = new TextView(this);
            chip.setText(categoria);
            chip.setTextSize(14);
            chip.setTypeface(null, Typeface.BOLD);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(12), dp(12), dp(12), dp(12));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(48), i < 2 ? 1.3f : 1f);
            p.setMargins(dp(3), dp(3), dp(3), dp(3));
            (i < 2 ? linha1 : linha2).addView(chip, p);
            botoesCategoria.add(chip);
            chip.setOnClickListener(v -> { categoriaAtual = categoria; atualizarCategorias(); atualizarResultados(); });
        }
        atualizarCategorias();
    }

    private void atualizarCategorias() {
        for (int i = 0; i < botoesCategoria.size(); i++) {
            TextView chip = botoesCategoria.get(i);
            boolean ativo = categorias[i].equals(categoriaAtual);
            chip.setBackgroundResource(ativo ? R.drawable.bg_adicionar_chip_ativo : R.drawable.bg_adicionar_chip_inativo);
            chip.setTextColor(ativo ? Color.WHITE : AZUL);
        }
    }

    /** Renderiza os resultados locais e filtra por nome. */
    private void atualizarResultados() {
        listaResultados.removeAllViews();
        String busca = campoBusca.getText().toString().trim().toLowerCase(Locale.ROOT);
        int encontrados = 0;
        for (Local local : locais) {
            if (!local.nome.toLowerCase(Locale.ROOT).contains(busca)) continue;
            // Os quatro exemplos pertencem à lista demonstrativa de pontos turísticos.
            if (!categoriaAtual.equals("Ponto turístico") && !categoriaAtual.equals("Outro")) continue;
            adicionarLinha(local);
            encontrados++;
        }
        if (encontrados == 0) {
            TextView vazio = new TextView(this);
            vazio.setText("Nenhum resultado demonstrativo para esta busca ou categoria.");
            vazio.setTextColor(CINZA);
            vazio.setTextSize(15);
            vazio.setPadding(dp(8), dp(22), dp(8), dp(22));
            listaResultados.addView(vazio);
        }
    }

    /** Constrói cada linha: foto real, nome, descrição e botão +/✓. */
    private void adicionarLinha(Local local) {
        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        linha.setPadding(0, dp(10), 0, dp(10));
        listaResultados.addView(linha, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(108)));

        ImageView foto = new ImageView(this);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        foto.setBackgroundResource(R.drawable.bg_adicionar_foto);
        foto.setClipToOutline(true);
        int resId = getResources().getIdentifier(local.imagem, "drawable", getPackageName());
        // Fallback existente no projeto caso as quatro novas fotografias ainda não tenham sido copiadas.
        foto.setImageResource(resId != 0 ? resId : R.drawable.roteiro_pelourinho);
        linha.addView(foto, new LinearLayout.LayoutParams(dp(82), dp(82)));

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        textos.setPadding(dp(12), 0, dp(4), 0);
        textos.setGravity(Gravity.CENTER_VERTICAL);
        linha.addView(textos, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        TextView titulo = new TextView(this);
        titulo.setText(local.nome);
        titulo.setTextColor(AZUL);
        titulo.setTextSize(16);
        titulo.setTypeface(null, Typeface.BOLD);
        textos.addView(titulo);
        TextView descricao = new TextView(this);
        descricao.setText(local.categoria + " • " + local.distancia);
        descricao.setTextColor(CINZA);
        descricao.setTextSize(14);
        textos.addView(descricao);

        TextView adicionar = new TextView(this);
        adicionar.setGravity(Gravity.CENTER);
        adicionar.setTextSize(30);
        adicionar.setTypeface(null, Typeface.BOLD);
        adicionar.setTextColor(Color.WHITE);
        adicionar.setBackgroundResource(R.drawable.bg_adicionar_botao);
        linha.addView(adicionar, new LinearLayout.LayoutParams(dp(50), dp(50)));
        atualizarBotao(adicionar, local.id);
        adicionar.setOnClickListener(v -> {
            String chave = chaveLocal(local.id);
            boolean novo = !preferencias.getBoolean(chave, false);
            preferencias.edit().putBoolean(chave, novo).apply();
            atualizarBotao(adicionar, local.id);
            Toast.makeText(this, novo ? "Adicionado ao roteiro (simulação local)" : "Removido do roteiro", Toast.LENGTH_SHORT).show();
        });
        View divisor = new View(this);
        divisor.setBackgroundColor(Color.rgb(231, 236, 242));
        listaResultados.addView(divisor, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
    }

    private String chaveLocal(String id) {
        return "roteiro_" + seletorRoteiro.getSelectedItemPosition() + "_local_" + id;
    }
    private void atualizarBotao(TextView botao, String id) {
        boolean adicionado = preferencias.getBoolean(chaveLocal(id), false);
        botao.setText(adicionado ? "✓" : "+");
        botao.setContentDescription(adicionado ? "Remover local do roteiro" : "Adicionar local ao roteiro");
    }
    private int dp(float valor) { return Math.round(valor * getResources().getDisplayMetrics().density); }
}
