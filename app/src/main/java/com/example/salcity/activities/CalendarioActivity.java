package com.example.salcity.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.salcity.R;
import com.example.salcity.data.CalendarioRepository;
import com.example.salcity.data.CalendarioRepositoryLocal;
import com.example.salcity.data.ItemCalendario;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Tela de calendário do SalCity.
 * Exibe datas reais e, para o dia selecionado, os roteiros e eventos
 * fornecidos pelo CalendarioRepository.
 * Hoje o repositório é a versão local (sem banco). Para usar o banco,
 * troque a linha marcada no onCreate().
 */
public class CalendarioActivity extends AppCompatActivity {
    private static final int AZUL = Color.rgb(8, 43, 103);
    private final Calendar mesExibido = Calendar.getInstance();
    private final Calendar dataSelecionada = Calendar.getInstance();
    private GridLayout gridCalendario;
    private TextView txtMesAno, txtVazio;
    private LinearLayout listaItens;

    // Camada de dados do calendário e dos eventos salvos.
    private CalendarioRepository repositorio;

    // Dias do mês exibido que possuem itens (marcados com •).
    private Set<Integer> diasComItens = Collections.emptySet();
    // Mês (ano*100+mês) a que os dias acima pertencem.
    private int mesDosDias = -1;
    // Identifica a última renderização, para ignorar respostas atrasadas.
    private int geracao = 0;

    /** Inicializa as referências, botões e o mês atual. */
    @Override protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        setContentView(R.layout.activity_calendario);

        // Camada de dados LOCAL (sem banco). Quando o banco existir, troque por:
        // new CalendarioRepositoryRoom(this,
        //         AppDatabase.getInstance(this).eventoFavoritoDao(),
        //         new CalendarioRepositoryLocal(this))
        repositorio = new CalendarioRepositoryLocal(this);

        gridCalendario = findViewById(R.id.gridCalendario);
        txtMesAno = findViewById(R.id.txtMesAno);
        txtVazio = findViewById(R.id.txtVazioCalendario);
        listaItens = findViewById(R.id.listaItensCalendario);
        findViewById(R.id.btnVoltarCalendario).setOnClickListener(v -> finish());
        findViewById(R.id.btnMesAnterior).setOnClickListener(v -> mudarMes(-1));
        findViewById(R.id.btnMesSeguinte).setOnClickListener(v -> mudarMes(1));
        // Exibição inicial fixa: outubro de 2026, com o dia 31 selecionado.
        // Ao navegar pelos meses, as setas continuam funcionando normalmente.
        mesExibido.set(2026, Calendar.OCTOBER, 1);
        dataSelecionada.set(2026, Calendar.OCTOBER, 31);
        if (estado != null) {
            mesExibido.setTimeInMillis(estado.getLong("mes", mesExibido.getTimeInMillis()));
            dataSelecionada.setTimeInMillis(estado.getLong("dia", dataSelecionada.getTimeInMillis()));
        }
        renderizar();
    }

    /** Preserva o mês e o dia selecionado quando a Activity é recriada. */
    @Override protected void onSaveInstanceState(Bundle out) {
        out.putLong("mes", mesExibido.getTimeInMillis());
        out.putLong("dia", dataSelecionada.getTimeInMillis());
        super.onSaveInstanceState(out);
    }

    /** Recarrega os dados caso o usuário retorne de outra tela. */
    @Override protected void onResume() {
        super.onResume();
        if (gridCalendario != null) renderizar();
    }

    /** Avança ou retrocede o mês e seleciona o primeiro dia do novo mês. */
    private void mudarMes(int deslocamento) {
        mesExibido.set(Calendar.DAY_OF_MONTH, 1);
        mesExibido.add(Calendar.MONTH, deslocamento);
        dataSelecionada.setTimeInMillis(mesExibido.getTimeInMillis());
        renderizar();
    }

    /** Atualiza calendário e lista conforme a data selecionada. */
    private void renderizar() {
        String titulo = new SimpleDateFormat("MMMM 'de' yyyy", new Locale("pt", "BR"))
                .format(mesExibido.getTime());
        txtMesAno.setText(titulo.substring(0, 1).toUpperCase(new Locale("pt", "BR")) + titulo.substring(1));

        // Desenha a grade já com o que se sabe; será redesenhada ao chegar a resposta.
        preencherGrade();

        final int minhaGeracao = ++geracao;
        final int ano = mesExibido.get(Calendar.YEAR);
        final int mes = mesExibido.get(Calendar.MONTH) + 1;

        // Dias do mês que recebem o marcador •.
        repositorio.diasComItens(ano, mes, new CalendarioRepository.Callback<Set<Integer>>() {
            @Override public void onResultado(Set<Integer> dias) {
                if (descartar(minhaGeracao)) return;
                diasComItens = dias;
                mesDosDias = ano * 100 + mes;
                preencherGrade();
            }
            @Override public void onErro(String mensagem) {
                if (descartar(minhaGeracao)) return;
                avisar(mensagem);
            }
        });

        // Lista do dia selecionado.
        repositorio.listarDoDia(
                dataSelecionada.get(Calendar.YEAR),
                dataSelecionada.get(Calendar.MONTH) + 1,
                dataSelecionada.get(Calendar.DAY_OF_MONTH),
                new CalendarioRepository.Callback<List<ItemCalendario>>() {
                    @Override public void onResultado(List<ItemCalendario> itens) {
                        if (descartar(minhaGeracao)) return;
                        preencherLista(itens);
                    }
                    @Override public void onErro(String mensagem) {
                        if (descartar(minhaGeracao)) return;
                        avisar(mensagem);
                    }
                });
    }

    /** Ignora respostas de uma renderização antiga ou de uma tela já fechada. */
    private boolean descartar(int geracaoDaResposta) {
        return isFinishing() || isDestroyed() || geracaoDaResposta != geracao;
    }

    private void avisar(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    /** Desenha 7 colunas e 6 semanas, começando no domingo. */
    private void preencherGrade() {
        gridCalendario.removeAllViews();
        String[] dias = {"D", "S", "T", "Q", "Q", "S", "S"};
        for (String dia : dias) {
            TextView cabecalho = celula(dia, false);
            cabecalho.setTextColor(Color.rgb(81, 118, 154));
            cabecalho.setTypeface(null, Typeface.BOLD);
            adicionarNaGrade(cabecalho);
        }
        Calendar primeiro = (Calendar) mesExibido.clone();
        primeiro.set(Calendar.DAY_OF_MONTH, 1);
        int inicio = primeiro.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        int diasMes = primeiro.getActualMaximum(Calendar.DAY_OF_MONTH);
        int chaveMes = primeiro.get(Calendar.YEAR) * 100 + primeiro.get(Calendar.MONTH) + 1;
        boolean diasValidos = chaveMes == mesDosDias;
        for (int indice = 0; indice < 42; indice++) {
            int numero = indice - inicio + 1;
            if (numero < 1 || numero > diasMes) {
                adicionarNaGrade(celula("", false));
                continue;
            }
            Calendar diaAtual = (Calendar) primeiro.clone();
            diaAtual.set(Calendar.DAY_OF_MONTH, numero);
            boolean selecionado = mesmaData(diaAtual, dataSelecionada);
            TextView dia = celula(String.valueOf(numero), selecionado);
            if (diasValidos && diasComItens.contains(numero)) {
                dia.setText(numero + "\n•");
            }
            dia.setContentDescription("Selecionar " + formatar(diaAtual));
            dia.setOnClickListener(v -> {
                dataSelecionada.setTimeInMillis(diaAtual.getTimeInMillis());
                renderizar();
            });
            adicionarNaGrade(dia);
        }
    }

    /** Cria uma célula visual da grade. */
    private TextView celula(String conteudo, boolean selecionado) {
        TextView texto = new TextView(this);
        texto.setText(conteudo);
        texto.setGravity(Gravity.CENTER);
        texto.setTextSize(15);
        texto.setTextColor(AZUL);
        if (selecionado) {
            texto.setTypeface(null, Typeface.BOLD);
            texto.setBackgroundResource(R.drawable.bg_cal_dia_selecionado);
        }
        return texto;
    }

    /** Dimensiona cada célula igualmente usando o espaço disponível. */
    private void adicionarNaGrade(View view) {
        GridLayout.LayoutParams parametros = new GridLayout.LayoutParams();
        parametros.width = 0;
        parametros.height = dp(48);
        parametros.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        parametros.setMargins(dp(2), dp(2), dp(2), dp(2));
        gridCalendario.addView(view, parametros);
    }

    /** Mostra os itens do dia selecionado. */
    private void preencherLista(List<ItemCalendario> itens) {
        listaItens.removeAllViews();
        for (ItemCalendario item : itens) {
            adicionarItem(item);
        }
        txtVazio.setVisibility(itens.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Cria uma linha com imagem opcional, descrição e botão de favorito persistente. */
    private void adicionarItem(ItemCalendario item) {
        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        linha.setPadding(0, dp(10), 0, dp(10));
        ImageView foto = new ImageView(this);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        foto.setBackgroundResource(R.drawable.bg_cal_foto);
        foto.setClipToOutline(true);
        int idImagem = getResources().getIdentifier(item.nomeImagem, "drawable", getPackageName());
        if (idImagem != 0) foto.setImageResource(idImagem);
        foto.setContentDescription("Fotografia ilustrativa de Salvador");
        linha.addView(foto, new LinearLayout.LayoutParams(dp(100), dp(88)));
        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        textos.setPadding(dp(12), 0, dp(4), 0);
        TextView nome = new TextView(this);
        nome.setText(item.titulo);
        nome.setTextColor(AZUL);
        nome.setTextSize(16);
        nome.setTypeface(null, Typeface.BOLD);
        TextView descricao = new TextView(this);
        descricao.setText(item.subtitulo);
        descricao.setTextSize(13);
        descricao.setTextColor(Color.rgb(82, 114, 149));
        textos.addView(nome);
        textos.addView(descricao);
        linha.addView(textos, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView favorito = new TextView(this);
        favorito.setGravity(Gravity.CENTER);
        favorito.setTextSize(30);
        favorito.setTextColor(Color.RED);
        favorito.setText(item.favorito ? "♥" : "♡");
        favorito.setContentDescription("Favoritar " + item.titulo);
        favorito.setOnClickListener(v -> repositorio.alternarFavorito(item.chave,
                new CalendarioRepository.Callback<Boolean>() {
                    @Override public void onResultado(Boolean valor) {
                        if (isFinishing() || isDestroyed()) return;
                        favorito.setText(valor ? "♥" : "♡");
                        Toast.makeText(CalendarioActivity.this,
                                valor ? "Adicionado aos favoritos" : "Removido dos favoritos",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onErro(String mensagem) {
                        if (isFinishing() || isDestroyed()) return;
                        avisar(mensagem);
                    }
                }));
        linha.addView(favorito, new LinearLayout.LayoutParams(dp(48), dp(56)));
        listaItens.addView(linha);
        View divisor = new View(this);
        divisor.setBackgroundColor(Color.rgb(226, 231, 237));
        listaItens.addView(divisor, new LinearLayout.LayoutParams(-1, dp(1)));
    }

    /** Compara datas ignorando horas. */
    private boolean mesmaData(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
                && a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH);
    }

    private String formatar(Calendar data) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).format(data.getTime());
    }

    private int dp(int valor) { return Math.round(valor * getResources().getDisplayMetrics().density); }
}
