package com.example.salcity.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.salcity.R;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/** SalCity: somente interface de filtros; integração com dados virá depois. */
public class FiltrosActivity extends AppCompatActivity {
    private static final int NAVY = Color.rgb(7, 51, 110);
    private static final int WHITE = Color.WHITE;
    private static final int MAX_ORCAMENTO = 500;

    private static final String[] TEMAS = {
            "Praia", "Cultura", "Gastronomia", "Religioso", "Eventos",
            "Vida noturna", "Natureza", "Histórico", "Familiar"
    };
    private static final int[] ICONES_TEMAS = {
            R.drawable.ic_filtro_beach, R.drawable.ic_filtro_culture,
            R.drawable.ic_filtro_food, R.drawable.ic_filtro_religious,
            R.drawable.ic_filtro_calendar, R.drawable.ic_filtro_night,
            R.drawable.ic_filtro_nature, R.drawable.ic_filtro_history,
            R.drawable.ic_filtro_family
    };
    private static final String[] TRANSPORTES = {
            "A pé", "Carro", "Transporte público", "Bicicleta"
    };
    private static final int[] ICONES_TRANSPORTE = {
            R.drawable.ic_filtro_walk, R.drawable.ic_filtro_car,
            R.drawable.ic_filtro_bus, R.drawable.ic_filtro_bike
    };
    private static final String[] LOCALIZACOES = {
            "Toda Salvador", "Centro", "Barra", "Rio Vermelho", "Pelourinho"
    };

    private final Set<String> temasSelecionados = new LinkedHashSet<>();
    private final ArrayList<LinearLayout> chipsTemas = new ArrayList<>();
    private final ArrayList<LinearLayout> chipsTransportes = new ArrayList<>();
    private String transporte = "A pé";
    private Spinner spinnerLocalizacao;
    private SeekBar seekMin, seekMax;
    private TextView txtMin, txtMax;
    private int orcamentoMin = 50;
    private int orcamentoMax = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtros);
        spinnerLocalizacao = findViewById(R.id.spinnerLocalizacao);
        seekMin = findViewById(R.id.seekMinOrcamento);
        seekMax = findViewById(R.id.seekMaxOrcamento);
        txtMin = findViewById(R.id.txtMinOrcamento);
        txtMax = findViewById(R.id.txtMaxOrcamento);

        configurarLocalizacao();
        criarTemas();
        criarTransportes();
        configurarOrcamento();
        temasSelecionados.add("Cultura");
        atualizarSelecoes();

        findViewById(R.id.btnVoltarFiltros).setOnClickListener(v -> finish());
        findViewById(R.id.btnLimparFiltros).setOnClickListener(v -> limparTudo());
        findViewById(R.id.btnAplicarFiltros).setOnClickListener(v -> aplicarFiltros());
    }

    private void configurarLocalizacao() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, LOCALIZACOES);
        spinnerLocalizacao.setAdapter(adapter);
    }

    /** Temas permitem selecionar vários chips simultaneamente. */
    private void criarTemas() {
        LinearLayout grupo = findViewById(R.id.grupoTemas);
        for (int inicio = 0; inicio < TEMAS.length; inicio += 3) {
            LinearLayout linha = novaLinha();
            for (int j = inicio; j < Math.min(inicio + 3, TEMAS.length); j++) {
                final String tema = TEMAS[j];
                LinearLayout chip = novoChip(tema, ICONES_TEMAS[j]);
                chipsTemas.add(chip);
                chip.setOnClickListener(v -> {
                    if (!temasSelecionados.add(tema)) temasSelecionados.remove(tema);
                    atualizarSelecoes();
                });
                adicionarComPeso(linha, chip);
            }
            grupo.addView(linha);
        }
    }

    /** Transporte permite uma única seleção. */
    private void criarTransportes() {
        LinearLayout grupo = findViewById(R.id.grupoTransportes);
        // Duas linhas evitam cortes em telas Android estreitas.
        for (int inicio = 0; inicio < TRANSPORTES.length; inicio += 2) {
            LinearLayout linha = novaLinha();
            for (int j = inicio; j < Math.min(inicio + 2, TRANSPORTES.length); j++) {
                final String opcao = TRANSPORTES[j];
                LinearLayout chip = novoChip(opcao, ICONES_TRANSPORTE[j]);
                chipsTransportes.add(chip);
                chip.setOnClickListener(v -> {
                    transporte = opcao;
                    atualizarSelecoes();
                });
                adicionarComPeso(linha, chip);
            }
            grupo.addView(linha);
        }
    }

    private LinearLayout novaLinha() {
        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(8);
        linha.setLayoutParams(lp);
        return linha;
    }

    private LinearLayout novoChip(String nome, int icone) {
        LinearLayout chip = new LinearLayout(this);
        chip.setGravity(Gravity.CENTER);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setMinimumHeight(dp(52));
        chip.setPadding(dp(5), dp(8), dp(5), dp(8));
        chip.setBackgroundResource(R.drawable.bg_sc_filter_chip_off);
        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setContentDescription(nome);
        ImageView imagem = new ImageView(this);
        imagem.setImageResource(icone);
        imagem.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        chip.addView(imagem);
        TextView texto = new TextView(this);
        texto.setText(nome);
        texto.setTextColor(NAVY);
        texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        texto.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.leftMargin = dp(5);
        chip.addView(texto, lp);
        return chip;
    }

    private void adicionarComPeso(LinearLayout linha, View chip) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.leftMargin = dp(3);
        lp.rightMargin = dp(3);
        linha.addView(chip, lp);
    }

    private void atualizarSelecoes() {
        for (int i = 0; i < TEMAS.length; i++) {
            atualizarChip(chipsTemas.get(i), temasSelecionados.contains(TEMAS[i]));
        }
        for (int i = 0; i < TRANSPORTES.length; i++) {
            atualizarChip(chipsTransportes.get(i), TRANSPORTES[i].equals(transporte));
        }
    }

    private void atualizarChip(LinearLayout chip, boolean selecionado) {
        chip.setBackgroundResource(selecionado
                ? R.drawable.bg_sc_filter_chip_on : R.drawable.bg_sc_filter_chip_off);
        TextView texto = (TextView) chip.getChildAt(1);
        ImageView imagem = (ImageView) chip.getChildAt(0);
        texto.setTextColor(selecionado ? WHITE : NAVY);
        texto.setTypeface(null, selecionado ? Typeface.BOLD : Typeface.NORMAL);
        imagem.setImageTintList(ColorStateList.valueOf(selecionado ? WHITE : NAVY));
        chip.setSelected(selecionado);
    }

    /** Dois SeekBars garantem mínimo <= máximo, sem bibliotecas extras. */
    private void configurarOrcamento() {
        seekMin.setMax(MAX_ORCAMENTO);
        seekMax.setMax(MAX_ORCAMENTO);
        seekMin.setProgress(orcamentoMin);
        seekMax.setProgress(orcamentoMax);
        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (!fromUser) return;
                if (bar == seekMin) {
                    orcamentoMin = Math.min(progress, orcamentoMax);
                    if (orcamentoMin != progress) seekMin.setProgress(orcamentoMin);
                } else {
                    orcamentoMax = Math.max(progress, orcamentoMin);
                    if (orcamentoMax != progress) seekMax.setProgress(orcamentoMax);
                }
                atualizarTextoOrcamento();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        };
        seekMin.setOnSeekBarChangeListener(listener);
        seekMax.setOnSeekBarChangeListener(listener);
        atualizarTextoOrcamento();
    }

    private void atualizarTextoOrcamento() {
        txtMin.setText("R$ " + orcamentoMin);
        txtMax.setText("R$ " + orcamentoMax);
    }

    private void limparTudo() {
        temasSelecionados.clear();
        transporte = null;
        spinnerLocalizacao.setSelection(0);
        orcamentoMin = 0;
        orcamentoMax = MAX_ORCAMENTO;
        seekMin.setProgress(orcamentoMin);
        seekMax.setProgress(orcamentoMax);
        atualizarTextoOrcamento();
        atualizarSelecoes();
        Toast.makeText(this, "Filtros limpos", Toast.LENGTH_SHORT).show();
    }

    /** Devolve os filtros para a Home, sem abrir roteiros. */
    private void aplicarFiltros() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("temas", new ArrayList<>(temasSelecionados));
        intent.putExtra("localizacao", spinnerLocalizacao.getSelectedItem().toString());
        intent.putExtra("transporte", transporte);
        intent.putExtra("orcamento_min", orcamentoMin);
        intent.putExtra("orcamento_max", orcamentoMax);
        // Compatibilidade temporária com telas que ainda leem a chave antiga.
        intent.putExtra("orcamento", "R$ " + orcamentoMin + " - R$ " + orcamentoMax);
        setResult(RESULT_OK, intent);
        finish();
    }

    private int dp(float valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
