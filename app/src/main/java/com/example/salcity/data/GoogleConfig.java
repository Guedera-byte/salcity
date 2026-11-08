package com.example.salcity.data;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: GoogleConfig
 *
 * OBJETIVO:
 * Guardar a configuração do login com Google.
 *
 * COMO PREENCHER:
 * Cole em WEB_CLIENT_ID o "ID do cliente" do tipo
 * "Aplicativo da Web" criado no Google Cloud Console.
 *
 * ATENÇÃO:
 * Use o ID do cliente do tipo WEB, e não o do tipo
 * Android. O do Android só precisa existir no console
 * (com o pacote e o SHA-1), mas não é colado aqui.
 *
 * Enquanto estiver vazio, o botão "Entrar com Google"
 * mostra o aviso "ainda não configurado".
 * =====================================================
 */
public final class GoogleConfig {

    // Exemplo: "123456789-abcdefg.apps.googleusercontent.com"
    public static final String WEB_CLIENT_ID = "196573656511-464qqr2ptur614o2mislmnrimcm2rqjg.apps.googleusercontent.com";

    private GoogleConfig() {
        // Classe utilitária: não deve ser instanciada.
    }

    public static boolean estaConfigurado() {
        return WEB_CLIENT_ID != null && !WEB_CLIENT_ID.trim().isEmpty();
    }
}
