package com.example.salcity.data;

import android.os.Handler;
import android.os.Looper;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: AutenticacaoRepositoryProvisorio
 *
 * OBJETIVO:
 * Permitir navegar pelo app (Login -> Home) ANTES de
 * existir um banco de dados.
 *
 * ATENÇÃO - SOMENTE PARA DESENVOLVIMENTO:
 * Esta classe NÃO confere conta nem senha. Qualquer
 * identificador e senha preenchidos são aceitos.
 * Ela NÃO pode ser usada em uma versão publicada do app.
 * Antes de publicar, troque-a por uma implementação real
 * (AutenticacaoRepositoryRoom ou servidor).
 * =====================================================
 */
public class AutenticacaoRepositoryProvisorio implements AutenticacaoRepository {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void entrar(String identificador, String senha, LoginCallback callback) {

        if (identificador == null || identificador.trim().isEmpty()
                || senha == null || senha.isEmpty()) {
            mainHandler.post(() -> callback.onErro("Informe e-mail e senha."));
            return;
        }

        String id = identificador.trim();

        // Usuário fictício, só para a navegação funcionar.
        UsuarioLogado fake = new UsuarioLogado(0L, "Usuário de teste", id);
        mainHandler.post(() -> callback.onSucesso(fake));
    }
}
