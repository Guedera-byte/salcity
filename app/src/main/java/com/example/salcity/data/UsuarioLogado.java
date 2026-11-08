package com.example.salcity.data;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: UsuarioLogado
 *
 * OBJETIVO:
 * Guardar os dados básicos de quem fez login.
 * Nunca contém senha nem hash.
 * =====================================================
 */
public final class UsuarioLogado {

    public final long id;
    public final String nome;
    public final String email;

    public UsuarioLogado(long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }
}
