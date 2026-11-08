package com.example.salcity;

import static org.junit.Assert.*;

import com.example.salcity.data.ValidadorCadastro;
import org.junit.Test;

public class ValidadorCadastroTest {

    @Test
    public void cadastroValido_naoTemErro() {
        assertNull(ValidadorCadastro.validar("Ana", "ana@email.com", "12345678", "12345678"));
    }

    @Test
    public void emailInvalido_temErro() {
        assertEquals("E-mail inválido.",
                ValidadorCadastro.validar("Ana", "ana@", "12345678", "12345678"));
    }

    @Test
    public void senhaComSeteCaracteres_eRejeitada() {
        assertEquals("A senha deve ter pelo menos 8 caracteres.",
                ValidadorCadastro.validar("Ana", "ana@email.com", "1234567", "1234567"));
    }

    @Test
    public void senhaComOitoCaracteres_eAceita() {
        assertNull(ValidadorCadastro.validar("Ana", "ana@email.com", "12345678", "12345678"));
    }

    @Test
    public void senhasDiferentes_temErro() {
        assertEquals("As senhas não coincidem.",
                ValidadorCadastro.validar("Ana", "ana@email.com", "12345678", "87654321"));
    }

    @Test
    public void nomeVazio_temErro() {
        assertEquals("Informe o nome.",
                ValidadorCadastro.validar("   ", "ana@email.com", "12345678", "12345678"));
    }
}