package com.example.salcity.data;

import java.util.List;
import java.util.Set;

/*
 * =====================================================
 * PROJETO: SALCITY
 * INTERFACE: CalendarioRepository
 *
 * OBJETIVO:
 * Definir o que a tela de calendário precisa, sem saber
 * ONDE os dados ficam guardados.
 *
 * IMPLEMENTAÇÕES:
 * - CalendarioRepositoryLocal: SharedPreferences (usada agora).
 * - CalendarioRepositoryRoom: favoritos no banco (futura).
 *
 * REGRAS:
 * - Os resultados chegam SEMPRE na thread principal.
 * - Mês de 1 a 12 (janeiro = 1).
 * - Para trocar a implementação, altere uma única linha
 *   na CalendarioActivity.onCreate().
 * =====================================================
 */
public interface CalendarioRepository {

    // Retorno assíncrono genérico.
    interface Callback<T> {
        void onResultado(T resultado);
        void onErro(String mensagem);
    }

    // Itens (roteiros e eventos) de um dia.
    void listarDoDia(int ano, int mes, int dia, Callback<List<ItemCalendario>> callback);

    // Dias do mês (1..31) que possuem algum item. Usado para marcar a grade.
    void diasComItens(int ano, int mes, Callback<Set<Integer>> callback);

    // Todos os itens conhecidos, em qualquer data.
    void listarTodos(Callback<List<ItemCalendario>> callback);

    // Itens marcados como favoritos (eventos salvos).
    void listarFavoritos(Callback<List<ItemCalendario>> callback);

    // Marca ou desmarca o favorito. Devolve o NOVO estado (true = favorito).
    void alternarFavorito(String chave, Callback<Boolean> callback);
}
