package com.example.salcity.data;

public final class ValidadorCadastro {

    private static final int TAMANHO_MINIMO_SENHA = 8;

    private ValidadorCadastro() { }

    public static String validar(String nome, String email,
                                 String senha, String confirmacao) {
        if (nome == null || nome.trim().isEmpty()) {
            return "Informe o nome.";
        }
        if (email == null
                || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "E-mail inválido.";
        }
        if (senha == null || senha.isEmpty()) {
            return "Informe uma senha.";
        }
        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            return "A senha deve ter pelo menos 8 caracteres.";
        }
        if (!senha.equals(confirmacao)) {
            return "As senhas não coincidem.";
        }
        return null;
    }
}