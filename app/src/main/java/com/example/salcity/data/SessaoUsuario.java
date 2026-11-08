package com.example.salcity.data;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: SessaoUsuario
 *
 * OBJETIVO:
 * Lembrar quem está logado, mesmo depois de fechar o app.
 *
 * Guarda apenas id, nome e e-mail em SharedPreferences.
 * NUNCA guarda senha.
 *
 * Não depende do banco de dados.
 * =====================================================
 */
public final class SessaoUsuario {

    private static final String PREFS = "salcity_sessao";
    private static final String CHAVE_ID = "usuario_id";
    private static final String CHAVE_NOME = "usuario_nome";
    private static final String CHAVE_EMAIL = "usuario_email";
    private static final String CHAVE_LOGADO = "logado";

    private SessaoUsuario() {
        // Classe utilitária: não deve ser instanciada.
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // Registra o usuário que acabou de entrar.
    public static void iniciar(Context context, UsuarioLogado usuario) {
        prefs(context).edit()
                .putBoolean(CHAVE_LOGADO, true)
                .putLong(CHAVE_ID, usuario.id)
                .putString(CHAVE_NOME, usuario.nome)
                .putString(CHAVE_EMAIL, usuario.email)
                .apply();
    }

    public static boolean estaLogado(Context context) {
        return prefs(context).getBoolean(CHAVE_LOGADO, false);
    }

    // Devolve o usuário logado, ou null se não houver sessão.
    public static UsuarioLogado atual(Context context) {
        SharedPreferences p = prefs(context);
        if (!p.getBoolean(CHAVE_LOGADO, false)) return null;
        return new UsuarioLogado(
                p.getLong(CHAVE_ID, 0L),
                p.getString(CHAVE_NOME, ""),
                p.getString(CHAVE_EMAIL, "")
        );
    }

    // Encerra a sessão. Apaga só os dados da sessão, nada mais.
    public static void encerrar(Context context) {
        prefs(context).edit().clear().apply();
    }
}
