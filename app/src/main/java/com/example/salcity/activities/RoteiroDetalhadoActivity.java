package com.example.salcity.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.salcity.R;
import com.example.salcity.model.ChavesApp;
import com.example.salcity.model.CoordenadasSalvador;
import com.example.salcity.model.DimensoesRoteiro;
import com.example.salcity.model.ParadaRoteiro;
import com.example.salcity.model.RoteiroEssencialDados;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RoteiroDetalhadoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private LinearLayout conteudoRoteiro;
    private FrameLayout conteudoMapa;
    private TextView conteudoDetalhes, abaRoteiro, abaMapa, abaDetalhes;
    private ImageButton btnFavorito;
    private Button btnAdicionar;
    private Button btnSalvar;

    private SharedPreferences preferencias;
    private boolean mapaCriado = false;
    private List<ParadaRoteiro> paradas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roteiro_detalhado);

        preferencias = getSharedPreferences(ChavesApp.PREFS_UI, MODE_PRIVATE);
        paradas = RoteiroEssencialDados.getParadas(this);

        associarComponentes();
        configurarBotoes();
        construirLinhaDoTempo();
        configurarDescricao();
        selecionarAba(0);
        atualizarFavorito();
        atualizarBotaoSalvar();
    }

    private void associarComponentes() {
        conteudoRoteiro = findViewById(R.id.conteudoRoteiro);
        conteudoMapa    = findViewById(R.id.conteudoMapa);
        conteudoDetalhes = findViewById(R.id.conteudoDetalhes);
        abaRoteiro  = findViewById(R.id.abaRoteiro);
        abaMapa     = findViewById(R.id.abaMapa);
        abaDetalhes = findViewById(R.id.abaDetalhes);
        btnFavorito = findViewById(R.id.btnFavoritoDetalhe);
        btnAdicionar = findViewById(R.id.btnAdicionarRoteiro);
        btnSalvar    = findViewById(R.id.btnSalvarRoteiro);
    }

    private void configurarBotoes() {
        findViewById(R.id.btnVoltarDetalhe).setOnClickListener(v -> finish());
        findViewById(R.id.btnNotificacaoDetalhe).setOnClickListener(v ->
                Toast.makeText(this, R.string.roteiro_notificacoes_futuras, Toast.LENGTH_SHORT).show());

        abaRoteiro.setOnClickListener(v  -> selecionarAba(0));
        abaMapa.setOnClickListener(v     -> selecionarAba(1));
        abaDetalhes.setOnClickListener(v -> selecionarAba(2));

        btnFavorito.setOnClickListener(v -> {
            boolean atual = preferencias.getBoolean(ChavesApp.CHAVE_FAVORITO, false);
            preferencias.edit().putBoolean(ChavesApp.CHAVE_FAVORITO, !atual).apply();
            atualizarFavorito();
        });

        btnSalvar.setOnClickListener(v -> salvarRoteiro());

        btnAdicionar.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdicionarRoteiroActivity.class);
            intent.putExtra("codigo_roteiro", "essencial");
            intent.putExtra("titulo_roteiro", getString(R.string.roteiro_essencial_titulo));
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (btnSalvar != null) atualizarBotaoSalvar();
    }

    private JSONArray lerRoteirosSalvos() throws JSONException {
        String dados = getSharedPreferences(ChavesApp.PREFS_ROTEIROS, MODE_PRIVATE)
                .getString(ChavesApp.CHAVE_LISTA, "[]");
        return new JSONArray(dados);
    }

    private boolean roteiroJaSalvo(JSONArray lista) {
        for (int i = 0; i < lista.length(); i++) {
            JSONObject item = lista.optJSONObject(i);
            if (item != null && ChavesApp.ID_ESSENCIAL.equals(item.optString("id"))) return true;
        }
        return false;
    }

    private void atualizarBotaoSalvar() {
        try {
            boolean salvo = roteiroJaSalvo(lerRoteirosSalvos());
            btnSalvar.setText(salvo ? R.string.roteiro_salvo : R.string.roteiro_salvar);
            btnSalvar.setEnabled(!salvo);
            btnSalvar.setAlpha(salvo
                    ? DimensoesRoteiro.ALPHA_DESABILITADO
                    : DimensoesRoteiro.ALPHA_ATIVO);
        } catch (JSONException erro) {
            btnSalvar.setText(R.string.roteiro_salvar);
            btnSalvar.setEnabled(true);
        }
    }

    private void salvarRoteiro() {
        try {
            JSONArray lista = lerRoteirosSalvos();
            if (roteiroJaSalvo(lista)) {
                atualizarBotaoSalvar();
                Toast.makeText(this, R.string.roteiro_ja_salvo, Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject roteiro = new JSONObject();
            roteiro.put("id", ChavesApp.ID_ESSENCIAL);
            roteiro.put("origem", "sugerido");
            roteiro.put("nome", getString(R.string.roteiro_essencial_titulo));
            roteiro.put("inicio", "");
            roteiro.put("fim", "");
            roteiro.put("orcamento", 0);
            roteiro.put("descricao", getString(R.string.roteiro_essencial_descricao_completa));

            JSONArray tags = new JSONArray();
            tags.put("Cultura");
            tags.put("História");
            roteiro.put("tags", tags);

            lista.put(roteiro);
            boolean gravou = getSharedPreferences(ChavesApp.PREFS_ROTEIROS, MODE_PRIVATE)
                    .edit().putString(ChavesApp.CHAVE_LISTA, lista.toString()).commit();

            if (gravou) {
                atualizarBotaoSalvar();
                Toast.makeText(this, R.string.roteiro_salvo_sucesso, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.roteiro_erro_salvar, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException erro) {
            Toast.makeText(this, R.string.roteiro_erro_ler, Toast.LENGTH_LONG).show();
        }
    }

    private void atualizarFavorito() {
        boolean favorito = preferencias.getBoolean(ChavesApp.CHAVE_FAVORITO, false);
        int cor = ContextCompat.getColor(this,
                favorito ? R.color.favorito_vermelho : R.color.azul_salcity);
        btnFavorito.setImageTintList(ColorStateList.valueOf(cor));
        btnFavorito.setContentDescription(getString(favorito
                ? R.string.roteiro_remover_favorito
                : R.string.roteiro_adicionar_favorito));
    }

    private void selecionarAba(int indice) {
        conteudoRoteiro.setVisibility(indice == 0 ? View.VISIBLE : View.GONE);
        conteudoMapa.setVisibility(indice == 1 ? View.VISIBLE : View.GONE);
        conteudoDetalhes.setVisibility(indice == 2 ? View.VISIBLE : View.GONE);

        int corAzul     = ContextCompat.getColor(this, R.color.azul_salcity);
        int corDestaque = ContextCompat.getColor(this, R.color.azul_destaque_salcity);

        TextView[] abas = {abaRoteiro, abaMapa, abaDetalhes};
        for (int i = 0; i < abas.length; i++) {
            boolean ativa = (i == indice);
            abas[i].setTextColor(ativa ? corDestaque : corAzul);
            abas[i].setTypeface(null, ativa ? Typeface.BOLD : Typeface.NORMAL);
            abas[i].setPaintFlags(ativa
                    ? (abas[i].getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG)
                    : (abas[i].getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG));
            abas[i].setSelected(ativa);
        }
        if (indice == 1 && !mapaCriado) inicializarMapa();
    }

    private void construirLinhaDoTempo() {
        conteudoRoteiro.removeAllViews();
        int corAzul        = ContextCompat.getColor(this, R.color.azul_salcity);
        int corTextoSecund = ContextCompat.getColor(this, R.color.texto_secundario_salcity);

        for (ParadaRoteiro parada : paradas) {
            LinearLayout linha = new LinearLayout(this);
            linha.setGravity(Gravity.CENTER_VERTICAL);
            linha.setOrientation(LinearLayout.HORIZONTAL);
            linha.setPadding(0,
                    dp(DimensoesRoteiro.PADDING_LINHA_DP),
                    0,
                    dp(DimensoesRoteiro.PADDING_LINHA_DP));
            linha.setMinimumHeight(dp(DimensoesRoteiro.ALTURA_MINIMA_LINHA_DP));

            TextView horario = new TextView(this);
            horario.setText(getString(R.string.roteiro_horario_formatado, parada.getHorario()));
            horario.setTextSize(DimensoesRoteiro.TEXTO_HORARIO_SP);
            horario.setTextColor(corAzul);
            horario.setTypeface(null, Typeface.BOLD);
            linha.addView(horario, new LinearLayout.LayoutParams(
                    dp(DimensoesRoteiro.LARGURA_HORARIO_DP),
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            ImageView imagem = new ImageView(this);
            imagem.setImageResource(parada.getFotoResId());
            imagem.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imagem.setBackgroundResource(R.drawable.bg_detalhe_thumb);
            imagem.setClipToOutline(true);
            imagem.setContentDescription(getString(
                    R.string.roteiro_foto_descricao, parada.getNome()));
            linha.addView(imagem, new LinearLayout.LayoutParams(
                    dp(DimensoesRoteiro.LARGURA_IMAGEM_DP),
                    dp(DimensoesRoteiro.ALTURA_IMAGEM_DP)));

            LinearLayout textos = new LinearLayout(this);
            textos.setOrientation(LinearLayout.VERTICAL);
            textos.setPadding(dp(DimensoesRoteiro.PADDING_TEXTO_DP), 0, 0, 0);

            TextView nome = new TextView(this);
            nome.setText(parada.getNome());
            nome.setTextColor(corAzul);
            nome.setTypeface(null, Typeface.BOLD);
            nome.setTextSize(DimensoesRoteiro.TEXTO_NOME_SP);

            TextView descricao = new TextView(this);
            descricao.setText(parada.getDescricao());
            descricao.setTextColor(corTextoSecund);
            descricao.setTextSize(DimensoesRoteiro.TEXTO_DESCRICAO_SP);

            textos.addView(nome);
            textos.addView(descricao);
            linha.addView(textos, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            conteudoRoteiro.addView(linha);
        }
    }

    private void configurarDescricao() {
        conteudoDetalhes.setText(R.string.roteiro_essencial_descricao_completa);
    }

    private void inicializarMapa() {
        mapaCriado = true;
        SupportMapFragment fragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.conteudoMapa, fragment)
                .commit();
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap mapa) {
        for (ParadaRoteiro parada : paradas) {
            mapa.addMarker(new MarkerOptions()
                    .position(parada.getCoordenadas())
                    .title(getString(R.string.roteiro_marcador_formatado,
                            parada.getHorario(), parada.getNome())));
        }
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(
                CoordenadasSalvador.centroDoMapa(),
                CoordenadasSalvador.ZOOM_INICIAL));
        mapa.getUiSettings().setZoomControlsEnabled(true);
    }

    private int dp(float valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
