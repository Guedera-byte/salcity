package com.example.salcity.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.example.salcity.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** SalCity: formulário demonstrativo de criação de roteiros. Sem chamadas à API. */
public class CriarNovoRoteiroActivity extends AppCompatActivity {
    private static final String PREFS = "salcity_roteiros_criados_demo";
    private static final String CHAVE_LISTA = "roteiros_json";
    private static final int AZUL = Color.rgb(8, 53, 114);
    private static final int BRANCO = Color.WHITE;
    private static final String[] TAGS = {"Praia", "Cultura", "Gastronomia", "Religioso", "Eventos", "Vida noturna", "Família", "Natureza", "Histórico"};
    private static final int[] ICONES_TAGS = {
            R.drawable.ic_filtro_beach, R.drawable.ic_filtro_culture, R.drawable.ic_filtro_food,
            R.drawable.ic_filtro_religious, R.drawable.ic_filtro_calendar, R.drawable.ic_filtro_night,
            R.drawable.ic_filtro_family, R.drawable.ic_filtro_nature, R.drawable.ic_filtro_history
    };
    private static final String[] TRANSPORTES = {"A pé", "Carro", "Transporte\npúblico", "Bicicleta", "Apps de\ntransporte"};
    private static final String[] VALORES_TRANSPORTE = {"A pé", "Carro", "Transporte público", "Bicicleta", "Apps de transporte"};
    private static final int[] ICONES_TRANSPORTE = {
            R.drawable.ic_filtro_walk, R.drawable.ic_filtro_car, R.drawable.ic_filtro_bus,
            R.drawable.ic_filtro_bike, R.drawable.ic_filtro_ride
    };
    private static final String[] BAIRROS = {
            "Escolha um bairro", "Barra", "Pelourinho", "Rio Vermelho", "Ondina",
            "Itapuã", "Pituba", "Comércio", "Santo Antônio Além do Carmo", "Brotas",
            "Graça", "Campo Grande", "Stella Maris", "Piatã", "Costa Azul", "Outro bairro"
    };

    private EditText edtNome, edtDescricao;
    private TextView txtContador, txtInicio, txtFim, txtOrcamento;
    private LinearLayout containerTags, grupoTransportes;
    private SeekBar seekOrcamento;
    private Spinner spinnerBairro;
    private SwitchCompat switchRestaurantes;
    private View botaoLocalizacao;
    private final Set<String> tagsSelecionadas = new LinkedHashSet<>();
    private final ArrayList<LinearLayout> chipsTransporte = new ArrayList<>();
    private Calendar dataInicio, dataFim;
    private String transporteSelecionado = "A pé";
    private boolean usarLocalizacaoAtual = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_novo_roteiro);
        associarViews();
        findViewById(R.id.btnVoltarCriar).setOnClickListener(v -> finish());
        configurarDescricao();
        configurarDatas();
        criarTags();
        configurarLocalizacao();
        criarTransportes();
        configurarOrcamento();
        findViewById(R.id.btnCriarRoteiro).setOnClickListener(v -> validarESalvar());
    }

    private void associarViews() {
        edtNome = findViewById(R.id.edtNomeRoteiro);
        edtDescricao = findViewById(R.id.edtDescricaoRoteiro);
        txtContador = findViewById(R.id.txtContadorDescricao);
        txtInicio = findViewById(R.id.txtDataInicio);
        txtFim = findViewById(R.id.txtDataFim);
        txtOrcamento = findViewById(R.id.txtOrcamentoAtual);
        containerTags = findViewById(R.id.containerTagsCriar);
        grupoTransportes = findViewById(R.id.grupoTransportesCriar);
        seekOrcamento = findViewById(R.id.seekOrcamentoCriar);
        spinnerBairro = findViewById(R.id.spinnerBairroCriar);
        switchRestaurantes = findViewById(R.id.switchRestaurantesCriar);
        botaoLocalizacao = findViewById(R.id.btnUsarLocalizacaoCriar);
    }

    private void configurarDescricao() {
        edtDescricao.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtContador.setText(getString(R.string.sc_criar_contador, s.length()));
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void configurarDatas() {
        findViewById(R.id.btnDataInicio).setOnClickListener(v -> abrirCalendario(true));
        findViewById(R.id.btnDataFim).setOnClickListener(v -> abrirCalendario(false));
    }

    private void abrirCalendario(boolean inicio) {
        Calendar base = inicio ? dataInicio : dataFim;
        if (base == null) base = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, ano, mes, dia) -> {
            Calendar escolhida = Calendar.getInstance();
            escolhida.clear();
            escolhida.set(ano, mes, dia);
            if (inicio) {
                dataInicio = escolhida;
                if (dataFim != null && dataFim.before(dataInicio)) {
                    dataFim = null;
                    txtFim.setText(R.string.sc_criar_data_placeholder);
                }
                txtInicio.setText(formatarData(dataInicio));
            } else {
                if (dataInicio != null && escolhida.before(dataInicio)) {
                    Toast.makeText(this, "O término não pode ser anterior ao início", Toast.LENGTH_LONG).show();
                    return;
                }
                dataFim = escolhida;
                txtFim.setText(formatarData(dataFim));
            }
        }, base.get(Calendar.YEAR), base.get(Calendar.MONTH), base.get(Calendar.DAY_OF_MONTH));
        if (!inicio && dataInicio != null) dialog.getDatePicker().setMinDate(dataInicio.getTimeInMillis());
        dialog.show();
    }

    private String formatarData(Calendar data) {
        return new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR")).format(data.getTime());
    }

    /** Nove tags, em três linhas; cada uma pode ser selecionada independentemente. */
    private void criarTags() {
        for (int linha = 0; linha < 3; linha++) {
            LinearLayout horizontal = new LinearLayout(this);
            horizontal.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams linhaLp = new LinearLayout.LayoutParams(-1, dp(50));
            linhaLp.bottomMargin = dp(8);
            containerTags.addView(horizontal, linhaLp);
            for (int coluna = 0; coluna < 3; coluna++) {
                int indice = linha * 3 + coluna;
                String tag = TAGS[indice];
                LinearLayout chip = criarChip(tag, ICONES_TAGS[indice], false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
                if (coluna > 0) lp.leftMargin = dp(6);
                horizontal.addView(chip, lp);
                chip.setOnClickListener(v -> {
                    if (!tagsSelecionadas.add(tag)) tagsSelecionadas.remove(tag);
                    aplicarAparencia(chip, tagsSelecionadas.contains(tag), false);
                });
            }
        }
    }

    private void configurarLocalizacao() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, BAIRROS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBairro.setAdapter(adapter);
        botaoLocalizacao.setOnClickListener(v -> {
            usarLocalizacaoAtual = !usarLocalizacaoAtual;
            botaoLocalizacao.setBackgroundResource(usarLocalizacaoAtual
                    ? R.drawable.bg_sc_criar_localizacao_ativo : R.drawable.bg_sc_criar_transporte_off);
            botaoLocalizacao.setSelected(usarLocalizacaoAtual);
            spinnerBairro.setEnabled(!usarLocalizacaoAtual);
            Toast.makeText(this, usarLocalizacaoAtual
                    ? "Preferência de localização atual selecionada (GPS ainda não integrado)"
                    : "Selecione um bairro", Toast.LENGTH_SHORT).show();
        });
        spinnerBairro.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && usarLocalizacaoAtual) {
                    usarLocalizacaoAtual = false;
                    botaoLocalizacao.setSelected(false);
                    botaoLocalizacao.setBackgroundResource(R.drawable.bg_sc_criar_transporte_off);
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void criarTransportes() {
        for (int i = 0; i < TRANSPORTES.length; i++) {
            final String valor = VALORES_TRANSPORTE[i];
            LinearLayout chip = criarChip(TRANSPORTES[i], ICONES_TRANSPORTE[i], true);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(92), 1f);
            if (i > 0) lp.leftMargin = dp(5);
            grupoTransportes.addView(chip, lp);
            chipsTransporte.add(chip);
            chip.setOnClickListener(v -> {
                transporteSelecionado = valor;
                atualizarTransportes();
            });
        }
        atualizarTransportes();
    }

    private LinearLayout criarChip(String titulo, int icone, boolean vertical) {
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(vertical ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(3), dp(5), dp(3), dp(5));
        chip.setBackgroundResource(vertical ? R.drawable.bg_sc_criar_transporte_off : R.drawable.bg_criar_chip);
        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setContentDescription(titulo.replace('\n', ' '));
        ImageView imagem = new ImageView(this);
        imagem.setImageResource(icone);
        imagem.setLayoutParams(new LinearLayout.LayoutParams(dp(vertical ? 25 : 21), dp(vertical ? 25 : 21)));
        chip.addView(imagem);
        TextView texto = new TextView(this);
        texto.setText(titulo);
        texto.setTextColor(AZUL);
        texto.setGravity(Gravity.CENTER);
        texto.setTextSize(vertical ? 10 : 11);
        texto.setMaxLines(vertical ? 3 : 1);
        LinearLayout.LayoutParams textoLp = new LinearLayout.LayoutParams(vertical ? -1 : -2, -2);
        if (vertical) textoLp.topMargin = dp(5);
        else textoLp.leftMargin = dp(4);
        chip.addView(texto, textoLp);
        return chip;
    }

    private void atualizarTransportes() {
        for (int i = 0; i < chipsTransporte.size(); i++) {
            aplicarAparencia(chipsTransporte.get(i), VALORES_TRANSPORTE[i].equals(transporteSelecionado), true);
        }
    }

    private void aplicarAparencia(LinearLayout chip, boolean ativo, boolean transporte) {
        chip.setBackgroundResource(transporte
                ? (ativo ? R.drawable.bg_sc_criar_transporte_on : R.drawable.bg_sc_criar_transporte_off)
                : (ativo ? R.drawable.bg_criar_chip_ativo : R.drawable.bg_criar_chip));
        ImageView imagem = (ImageView) chip.getChildAt(0);
        TextView texto = (TextView) chip.getChildAt(1);
        imagem.setColorFilter(ativo ? BRANCO : AZUL);
        texto.setTextColor(ativo ? BRANCO : AZUL);
        texto.setTypeface(null, ativo ? Typeface.BOLD : Typeface.NORMAL);
        chip.setSelected(ativo);
    }

    private void configurarOrcamento() {
        seekOrcamento.setProgress(0);
        seekOrcamento.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int valor, boolean usuario) {
                txtOrcamento.setText(valor == 500 ? "R$ 500+" : "R$ " + valor);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    /** Valida e registra uma solicitação demonstrativa; não gera roteiros personalizados. */
    private void validarESalvar() {
        String nome = edtNome.getText().toString().trim();
        if (nome.isEmpty()) {
            edtNome.setError("Informe o nome do roteiro");
            edtNome.requestFocus();
            return;
        }
        if (dataInicio == null || dataFim == null) {
            Toast.makeText(this, "Selecione as datas de início e fim", Toast.LENGTH_LONG).show();
            return;
        }
        if (dataFim.before(dataInicio)) {
            Toast.makeText(this, "A data final deve ser igual ou posterior à inicial", Toast.LENGTH_LONG).show();
            return;
        }
        if (tagsSelecionadas.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos uma tag de interesse", Toast.LENGTH_LONG).show();
            return;
        }
        String localizacao = usarLocalizacaoAtual ? "Minha localização atual" : spinnerBairro.getSelectedItem().toString();
        if (!usarLocalizacaoAtual && spinnerBairro.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Escolha um bairro ou use a localização atual", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            JSONObject roteiro = new JSONObject();
            roteiro.put("nome", nome);
            roteiro.put("descricao", edtDescricao.getText().toString().trim());
            roteiro.put("inicio", formatarData(dataInicio));
            roteiro.put("fim", formatarData(dataFim));
            roteiro.put("tags", new JSONArray(tagsSelecionadas));
            roteiro.put("orcamento", seekOrcamento.getProgress());
            roteiro.put("restaurantes", switchRestaurantes.isChecked());
            roteiro.put("localizacao", localizacao);
            roteiro.put("transporte", transporteSelecionado);
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            JSONArray lista = new JSONArray(prefs.getString(CHAVE_LISTA, "[]"));
            lista.put(roteiro);
            prefs.edit().putString(CHAVE_LISTA, lista.toString()).apply();

            Intent intent = new Intent(this, RoteirosSugeridosActivity.class);
            intent.putExtra("nome_roteiro", nome);
            intent.putExtra("descricao", edtDescricao.getText().toString().trim());
            intent.putExtra("data_inicio", formatarData(dataInicio));
            intent.putExtra("data_fim", formatarData(dataFim));
            intent.putStringArrayListExtra("temas", new ArrayList<>(tagsSelecionadas));
            intent.putExtra("orcamento", seekOrcamento.getProgress());
            intent.putExtra("restaurantes", switchRestaurantes.isChecked());
            intent.putExtra("localizacao", localizacao);
            intent.putExtra("transporte", transporteSelecionado);
            startActivity(intent);
            finish();
        } catch (JSONException e) {
            Toast.makeText(this, "Não foi possível salvar os dados demonstrativos", Toast.LENGTH_LONG).show();
        }
    }

    private int dp(float valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}