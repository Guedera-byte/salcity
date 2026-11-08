package com.example.salcity.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;

import java.util.Locale;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: UsuarioRepositoryProvisorio
 *
 * OBJETIVO:
 * Permitir que a tela de cadastro funcione ANTES de
 * existir um banco de dados.
 *
 * IMPORTANTE:
 * Esta classe NÃO salva nada. Ela apenas valida os dados
 * e responde "sucesso". Nenhuma conta é criada de verdade.
 *
 * Quando o banco existir, troque-a por outra
 * implementação de UsuarioRepository.
 * =====================================================
 */
public class UsuarioRepositoryProvisorio implements UsuarioRepository {

    // Deve ser igual ao mínimo exigido pela CadastroActivity.
    private static final int TAMANHO_MINIMO_SENHA = 8;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void cadastrar(String nome,
                          String email,
                          String senha,
                          CadastroCallback callback) {

        String emailNormalizado =
                email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        if (nome == null || nome.trim().isEmpty()) {
            erro(callback, "Informe o nome.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailNormalizado).matches()) {
            erro(callback, "E-mail inválido.");
            return;
        }
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            erro(callback, "A senha deve ter pelo menos "
                    + TAMANHO_MINIMO_SENHA + " caracteres.");
            return;
        }

        // Nada é gravado: devolve um id fictício.
        mainHandler.post(() -> callback.onSucesso(0L));
    }

    private void erro(CadastroCallback callback, String mensagem) {
        mainHandler.post(() -> callback.onErro(mensagem));
    }
}
