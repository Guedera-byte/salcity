package com.example.salcity.data;

/*
 * =====================================================
 * PROJETO: SALCITY
 * INTERFACE: AutenticacaoRepository
 *
 * OBJETIVO:
 * Definir o que a tela de login precisa, sem saber
 * ONDE as contas ficam guardadas.
 *
 * IMPLEMENTAÇÕES:
 * - AutenticacaoRepositoryProvisorio: sem banco (usada agora).
 * - AutenticacaoRepositoryRoom: com banco local (futura).
 *
 * Para trocar, altere uma única linha na
 * LoginActivity.onCreate().
 * =====================================================
 */
public interface AutenticacaoRepository {

    // Retorno assíncrono do login (sempre na thread principal).
    interface LoginCallback {
        void onSucesso(UsuarioLogado usuario);
        void onErro(String mensagem);
    }

    void entrar(String identificador, String senha, LoginCallback callback);
}
