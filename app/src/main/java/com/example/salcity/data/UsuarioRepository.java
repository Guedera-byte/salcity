package com.example.salcity.data;

/*
 * =====================================================
 * PROJETO: SALCITY
 * INTERFACE: UsuarioRepository
 *
 * OBJETIVO:
 * Definir o que a tela de cadastro precisa, sem saber
 * ONDE os dados são guardados.
 *
 * IMPLEMENTAÇÕES:
 * - UsuarioRepositoryProvisorio: sem banco (usada agora).
 * - UsuarioRepositoryRoom: com banco local (futura).
 *
 * Para trocar, altere uma única linha na
 * CadastroActivity.onCreate().
 * =====================================================
 */
public interface UsuarioRepository {

    // Retorno assíncrono do cadastro (sempre na thread principal).
    interface CadastroCallback {
        void onSucesso(long id);
        void onErro(String mensagem);
    }

    void cadastrar(String nome, String email, String senha, CadastroCallback callback);
}
