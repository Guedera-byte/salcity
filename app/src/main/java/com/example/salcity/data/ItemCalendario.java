package com.example.salcity.data;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: ItemCalendario
 *
 * OBJETIVO:
 * Representar uma linha da lista do calendário:
 * um roteiro do usuário ou um evento.
 *
 * A "chave" identifica o item de forma única e é usada
 * para marcá-lo como favorito (evento salvo).
 *
 * A classe é imutável. Para mudar o favorito, use
 * comFavorito(), que cria uma cópia.
 * =====================================================
 */
public final class ItemCalendario {

    public enum Tipo { ROTEIRO, EVENTO }

    public final String chave;
    public final Tipo tipo;
    public final String titulo;
    public final String subtitulo;
    public final String nomeImagem;
    public final boolean favorito;

    public ItemCalendario(String chave,
                          Tipo tipo,
                          String titulo,
                          String subtitulo,
                          String nomeImagem,
                          boolean favorito) {
        this.chave = chave;
        this.tipo = tipo;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.nomeImagem = nomeImagem;
        this.favorito = favorito;
    }

    // Devolve uma cópia com o estado de favorito alterado.
    public ItemCalendario comFavorito(boolean novoValor) {
        return new ItemCalendario(chave, tipo, titulo, subtitulo, nomeImagem, novoValor);
    }
}
