package com.example.salcity.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.salcity.R;
import com.example.salcity.data.SalCityAiApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/** Fluxo de busca de eventos e montagem do roteiro usando o backend SalCity. */
public class EventosIaActivity extends AppCompatActivity {
    private Calendar inicio = Calendar.getInstance();
    private Calendar fim = Calendar.getInstance();
    private Button btnInicio, btnFim, btnBuscar, btnGerar;
    private LinearLayout temasContainer, eventosContainer;
    private TextView estado, resultado;
    private final String[] opcoesTema = {"Praia", "Cultura", "Gastronomia", "Religioso", "Eventos", "Vida noturna", "Natureza", "Histórico", "Familiar"};

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_eventos_ia);
        inicio = Calendar.getInstance();
        fim = (Calendar) inicio.clone();
        fim.add(Calendar.DAY_OF_MONTH, 1);
        btnInicio = findViewById(R.id.btnDataInicioIa);
        btnFim = findViewById(R.id.btnDataFimIa);
        btnBuscar = findViewById(R.id.btnBuscarEventosIa);
        btnGerar = findViewById(R.id.btnGerarRoteiroIa);
        temasContainer = findViewById(R.id.containerTemasIa);
        eventosContainer = findViewById(R.id.containerListaEventosIa);
        estado = findViewById(R.id.txtEstadoBuscaIa);
        resultado = findViewById(R.id.txtResultadoRoteiroIa);
        findViewById(R.id.btnVoltarEventosIa).setOnClickListener(v -> finish());
        btnInicio.setOnClickListener(v -> escolherData(true));
        btnFim.setOnClickListener(v -> escolherData(false));
        criarTemas();
        atualizarDatas();
        btnBuscar.setOnClickListener(v -> buscarEventos());
        btnGerar.setOnClickListener(v -> gerarRoteiro());
    }

    private void criarTemas() {
        for (String tema : opcoesTema) {
            CheckBox check = new CheckBox(this);
            check.setText(tema);
            check.setTextColor(Color.rgb(7, 51, 110));
            check.setTextSize(15);
            if ("Cultura".equals(tema)) check.setChecked(true);
            temasContainer.addView(check);
        }
    }

    private void escolherData(boolean isInicio) {
        Calendar base = isInicio ? inicio : fim;
        DatePickerDialog dialog = new DatePickerDialog(this, (picker, year, month, day) -> {
            Calendar date = Calendar.getInstance();
            date.clear(); date.set(year, month, day);
            if (!isInicio && date.before(inicio)) {
                Toast.makeText(this, "A data final não pode ser anterior à inicial", Toast.LENGTH_LONG).show();
                return;
            }
            if (isInicio) {
                inicio = date;
                if (fim.before(inicio)) fim = (Calendar) inicio.clone();
            } else fim = date;
            atualizarDatas();
        }, base.get(Calendar.YEAR), base.get(Calendar.MONTH), base.get(Calendar.DAY_OF_MONTH));
        if (isInicio) dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        else dialog.getDatePicker().setMinDate(inicio.getTimeInMillis());
        dialog.show();
    }

    private String formatar(Calendar value, String pattern) {
        return new SimpleDateFormat(pattern, Locale.US).format(value.getTime());
    }

    private void atualizarDatas() {
        String display = "dd/MM/yyyy";
        btnInicio.setText("Chegada: " + formatar(inicio, display));
        btnFim.setText("Saída: " + formatar(fim, display));
    }

    private ArrayList<String> obterTemas() {
        ArrayList<String> values = new ArrayList<>();
        for (int i = 0; i < temasContainer.getChildCount(); i++) {
            CheckBox item = (CheckBox) temasContainer.getChildAt(i);
            if (item.isChecked()) values.add(item.getText().toString());
        }
        return values;
    }

    private void buscarEventos() {
        ArrayList<String> escolhidos = obterTemas();
        if (escolhidos.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos um tema", Toast.LENGTH_SHORT).show(); return;
        }
        eventosContainer.removeAllViews();
        btnBuscar.setEnabled(false);
        btnGerar.setVisibility(View.GONE);
        resultado.setVisibility(View.GONE);
        estado.setVisibility(View.VISIBLE);
        estado.setText("Consultando fontes aprovadas e encontrando eventos…");
        try {
            JSONObject request = new JSONObject();
            request.put("city", "Salvador");
            request.put("start_date", formatar(inicio, "yyyy-MM-dd"));
            request.put("end_date", formatar(fim, "yyyy-MM-dd"));
            request.put("themes", SalCityAiApi.strings(escolhidos));
            SalCityAiApi.buscarEventos(request, new SalCityAiApi.Callback() {
                @Override public void onSuccess(JSONObject data) {
                    btnBuscar.setEnabled(true);
                    JSONArray eventos = data.optJSONArray("events");
                    if (eventos == null || eventos.length() == 0) {
                        estado.setText("Nenhum evento confirmado para este período nas fontes configuradas."); return;
                    }
                    for (int i = 0; i < eventos.length(); i++) {
                        JSONObject evento = eventos.optJSONObject(i);
                        if (evento == null) continue;
                        CheckBox option = new CheckBox(EventosIaActivity.this);
                        String label = evento.optString("name", "Evento") + "\n" +
                                evento.optString("start_datetime", "Data a confirmar") + " · " +
                                evento.optString("venue", "Local a confirmar") + "\n" +
                                evento.optString("description", "") + "\nFonte: " + evento.optString("source_url", "");
                        option.setText(label.trim());
                        option.setTextColor(Color.WHITE);
                        option.setTag(evento);
                        option.setPadding(dp(8), dp(10), dp(8), dp(10));
                        eventosContainer.addView(option, new LinearLayout.LayoutParams(-1, -2));
                    }
                    estado.setText("Selecione os eventos que deseja incluir no roteiro.");
                    btnGerar.setVisibility(View.VISIBLE);
                }
                @Override public void onError(String message) {
                    btnBuscar.setEnabled(true);
                    estado.setText("Não foi possível buscar eventos: " + message);
                }
            });
        } catch (JSONException e) {
            btnBuscar.setEnabled(true); estado.setText("Não foi possível preparar os filtros.");
        }
    }

    private void gerarRoteiro() {
        JSONArray selecionados = new JSONArray();
        for (int i = 0; i < eventosContainer.getChildCount(); i++) {
            CheckBox item = (CheckBox) eventosContainer.getChildAt(i);
            if (item.isChecked()) selecionados.put((JSONObject) item.getTag());
        }
        if (selecionados.length() == 0) {
            Toast.makeText(this, "Marque pelo menos um evento", Toast.LENGTH_SHORT).show(); return;
        }
        btnGerar.setEnabled(false);
        resultado.setVisibility(View.VISIBLE);
        resultado.setText("Montando seu roteiro…");
        try {
            JSONObject request = new JSONObject();
            request.put("city", "Salvador");
            request.put("start_date", formatar(inicio, "yyyy-MM-dd"));
            request.put("end_date", formatar(fim, "yyyy-MM-dd"));
            request.put("themes", SalCityAiApi.strings(obterTemas()));
            request.put("transport", "A pé");
            request.put("budget", "Não informado");
            request.put("include_restaurants", false);
            request.put("selected_events", selecionados);
            SalCityAiApi.gerarRoteiro(request, new SalCityAiApi.Callback() {
                @Override public void onSuccess(JSONObject data) { btnGerar.setEnabled(true); mostrarRoteiro(data); }
                @Override public void onError(String message) { btnGerar.setEnabled(true); resultado.setText("Falha ao gerar roteiro: " + message); }
            });
        } catch (JSONException e) {
            btnGerar.setEnabled(true); resultado.setText("Não foi possível preparar os dados do roteiro.");
        }
    }

    private void mostrarRoteiro(JSONObject data) {
        JSONObject itinerary = data.optJSONObject("itinerary");
        if (itinerary == null) itinerary = data;
        StringBuilder output = new StringBuilder(itinerary.optString("title", "Roteiro em Salvador"));
        String summary = itinerary.optString("summary", "");
        if (!summary.isEmpty()) output.append("\n\n").append(summary);
        JSONArray days = itinerary.optJSONArray("days");
        if (days != null) for (int i = 0; i < days.length(); i++) {
            JSONObject day = days.optJSONObject(i); if (day == null) continue;
            output.append("\n\n").append(day.optString("date", "Dia " + (i + 1)));
            JSONArray activities = day.optJSONArray("activities");
            if (activities != null) for (int j = 0; j < activities.length(); j++) {
                JSONObject activity = activities.optJSONObject(j); if (activity == null) continue;
                output.append("\n• ").append(activity.optString("time", "")).append(" ")
                        .append(activity.optString("title", "Atividade"));
                String venue = activity.optString("venue", "");
                if (!venue.isEmpty()) output.append(" — ").append(venue);
                String detail = activity.optString("description", "");
                if (!detail.isEmpty()) output.append("\n  ").append(detail);
            }
        }
        resultado.setText(output.toString());
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
