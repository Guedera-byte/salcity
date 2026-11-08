package com.example.salcity;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.salcity.data.SessaoUsuario;
import com.example.salcity.data.UsuarioLogado;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34})
public class SessaoUsuarioTest {

    private Context ctx;

    @Before
    public void preparar() {
        ctx = ApplicationProvider.getApplicationContext();
        SessaoUsuario.encerrar(ctx); // começa sempre limpo
    }

    @Test
    public void semLogin_estaLogadoEFalso() {
        assertFalse(SessaoUsuario.estaLogado(ctx));
        assertNull(SessaoUsuario.atual(ctx));
    }

    @Test
    public void depoisDeIniciar_guardaOUsuario() {
        SessaoUsuario.iniciar(ctx, new UsuarioLogado(1L, "Ana", "ana@email.com"));
        assertTrue(SessaoUsuario.estaLogado(ctx));
        assertEquals("Ana", SessaoUsuario.atual(ctx).nome);
    }

    @Test
    public void depoisDeEncerrar_naoEstaMaisLogado() {
        SessaoUsuario.iniciar(ctx, new UsuarioLogado(1L, "Ana", "ana@email.com"));
        SessaoUsuario.encerrar(ctx);
        assertFalse(SessaoUsuario.estaLogado(ctx));
    }
}