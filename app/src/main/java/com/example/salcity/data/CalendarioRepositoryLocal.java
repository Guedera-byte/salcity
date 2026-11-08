package com.example.salcity.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: CalendarioRepositoryLocal
 *
 * OBJETIVO:
 * Fornecer os dados do calendário SEM banco de dados,
 * usando os mesmos SharedPreferences que o app já usa:
 *
 * - Roteiros: salcity_roteiros_criados_demo (JSON gravado
 *   pela CriarNovoRoteiroActivity).
 * - Favoritos: salcity_calendario_favoritos.
 *
 * Também inclui 3 eventos FICTÍCIOS de demonstração em
 * 31/10/2026. Não são eventos reais.
 *
 * As chaves de favorito são iguais às que o app já
 * gravava, então os favoritos existentes continuam valendo.
 * =====================================================
 */
public class CalendarioRepositoryLocal implements CalendarioRepository {

    private static final String TAG = "SalCityCalendario";

    // Mesmos nomes usados pela CriarNovoRoteiroActivity: NÃO ALTERAR.
    private static final String PREFS_ROTEIROS = "salcity_roteiros_criados_demo";
    private static final String CHAVE_ROTEIROS = "roteiros_json";
    private static final String PREFS_FAVORITOS = "salcity_calendario_favoritos";

    private final Context context;

    public CalendarioRepositoryLocal(Context context) {
        this.context = context.getApplicationContext();
    }

    // ---------------------------------------------------
    // API pública (resultado entregue na hora, na thread
    // principal, pois a leitura local é rápida)
    // ---------------------------------------------------

    @Override
    public void listarDoDia(int ano, int mes, int dia,
                            Callback<List<ItemCalendario>> callback) {
        int chaveDia = chaveData(ano, mes, dia);
        List<ItemCalendario> lista = new ArrayList<>();

        for (JSONObject roteiro : lerRoteiros()) {
            if (roteiroContem(roteiro, chaveDia)) {
                lista.add(paraItem(roteiro));
            }
        }
        if (ehDiaDemonstrativo(ano, mes, dia)) {
            lista.addAll(eventosDemonstrativos());
        }
        callback.onResultado(lista);
    }

    @Override
    public void diasComItens(int ano, int mes, Callback<Set<Integer>> callback) {
        Set<Integer> dias = new HashSet<>();

        Calendar primeiro = Calendar.getInstance();
        primeiro.clear();
        primeiro.set(ano, mes - 1, 1);
        int total = primeiro.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<JSONObject> roteiros = lerRoteiros();
        for (int dia = 1; dia <= total; dia++) {
            int chaveDia = chaveData(ano, mes, dia);
            for (JSONObject roteiro : roteiros) {
                if (roteiroContem(roteiro, chaveDia)) {
                    dias.add(dia);
                    break;
                }
            }
            if (ehDiaDemonstrativo(ano, mes, dia)) {
                dias.add(dia);
            }
        }
        callback.onResultado(dias);
    }

    @Override
    public void listarTodos(Callback<List<ItemCalendario>> callback) {
        List<ItemCalendario> lista = new ArrayList<>();
        for (JSONObject roteiro : lerRoteiros()) {
            lista.add(paraItem(roteiro));
        }
        lista.addAll(eventosDemonstrativos());
        callback.onResultado(lista);
    }

    @Override
    public void listarFavoritos(Callback<List<ItemCalendario>> callback) {
        List<ItemCalendario> favoritos = new ArrayList<>();
        for (ItemCalendario item : todosOsItens()) {
            if (item.favorito) favoritos.add(item);
        }
        callback.onResultado(favoritos);
    }

    @Override
    public void alternarFavorito(String chave, Callback<Boolean> callback) {
        SharedPreferences prefs = prefsFavoritos();
        boolean novo = !prefs.getBoolean(chave, false);
        prefs.edit().putBoolean(chave, novo).apply();
        callback.onResultado(novo);
    }

    // ---------------------------------------------------
    // Montagem dos itens
    // ---------------------------------------------------

    private List<ItemCalendario> todosOsItens() {
        List<ItemCalendario> lista = new ArrayList<>();
        for (JSONObject roteiro : lerRoteiros()) {
            lista.add(paraItem(roteiro));
        }
        lista.addAll(eventosDemonstrativos());
        return lista;
    }

    private ItemCalendario paraItem(JSONObject roteiro) {
        String nome = roteiro.optString("nome", "Roteiro sem nome");
        String codigo = roteiro.optString("id", nome);
        String chave = "roteiro_" + codigo;
        String subtitulo = "Meu roteiro • " + roteiro.optString("inicio")
                + " a " + roteiro.optString("fim");
        return new ItemCalendario(chave, ItemCalendario.Tipo.ROTEIRO,
                nome, subtitulo, "roteiro_pelourinho", favorito(chave));
    }

    // Eventos FICTÍCIOS de demonstração (31/10/2026).
    private List<ItemCalendario> eventosDemonstrativos() {
        List<ItemCalendario> lista = new ArrayList<>();
        lista.add(evento("festival_demo", "Festival da Primavera",
                "Pelourinho • 18h • Evento demonstrativo", "roteiro_pelourinho"));
        lista.add(evento("feira_demo", "Feira de Artesanato",
                "Mercado Modelo • 09h • Evento demonstrativo", "roteiro_pelourinho"));
        lista.add(evento("farol_demo", "Pôr do Sol no Farol",
                "Farol da Barra • 17h • Evento demonstrativo", "roteiro_farol"));
        return lista;
    }

    private ItemCalendario evento(String chave, String titulo,
                                  String subtitulo, String imagem) {
        return new ItemCalendario(chave, ItemCalendario.Tipo.EVENTO,
                titulo, subtitulo, imagem, favorito(chave));
    }

    // ---------------------------------------------------
    // Leitura dos dados locais
    // ---------------------------------------------------

    private SharedPreferences prefsFavoritos() {
        return context.getSharedPreferences(PREFS_FAVORITOS, Context.MODE_PRIVATE);
    }

    private boolean favorito(String chave) {
        return prefsFavoritos().getBoolean(chave, false);
    }

    // Lê os roteiros gravados pela tela "Criar novo roteiro".
    private List<JSONObject> lerRoteiros() {
        List<JSONObject> lista = new ArrayList<>();
        String json = context.getSharedPreferences(PREFS_ROTEIROS, Context.MODE_PRIVATE)
                .getString(CHAVE_ROTEIROS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject objeto = array.optJSONObject(i);
                if (objeto != null) lista.add(objeto);
            }
        } catch (Exception erro) {
            Log.e(TAG, "Não foi possível ler roteiros", erro);
        }
        return lista;
    }

    // ---------------------------------------------------
    // Datas (sem horário): ano*10000 + mês*100 + dia
    // ---------------------------------------------------

    private static int chaveData(int ano, int mes, int dia) {
        return ano * 10000 + mes * 100 + dia;
    }

    // Converte "dd/MM/yyyy" em chave de data; devolve -1 se inválida.
    private static int chaveDeTexto(String texto) {
        try {
            String[] partes = texto.trim().split("/");
            if (partes.length != 3) return -1;
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);
            if (dia < 1 || dia > 31 || mes < 1 || mes > 12) return -1;
            return chaveData(ano, mes, dia);
        } catch (Exception e) {
            return -1;
        }
    }

    // Verifica se o dia está dentro do período do roteiro (inclusive).
    private boolean roteiroContem(JSONObject roteiro, int chaveDia) {
        int inicio = chaveDeTexto(roteiro.optString("inicio"));
        int fim = chaveDeTexto(roteiro.optString("fim"));
        return inicio != -1 && fim != -1 && chaveDia >= inicio && chaveDia <= fim;
    }

    // Os eventos fictícios existem apenas em 31/10/2026.
    private static boolean ehDiaDemonstrativo(int ano, int mes, int dia) {
        return ano == 2026 && mes == 10 && dia == 31;
    }
}
