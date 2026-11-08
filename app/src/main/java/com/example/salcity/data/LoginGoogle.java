package com.example.salcity.data;

import android.app.Activity;
import android.os.CancellationSignal;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

/*
 * =====================================================
 * PROJETO: SALCITY
 * CLASSE: LoginGoogle
 *
 * OBJETIVO:
 * Fazer o login com a conta Google usando o
 * Credential Manager do Android.
 *
 * FUNCIONAMENTO:
 * 1. Abre a folha de seleção de contas do Google.
 * 2. O usuário escolhe a conta.
 * 3. Recebemos nome, e-mail e o ID token.
 * 4. Devolvemos o resultado na thread principal.
 *
 * IMPORTANTE:
 * O ID token deveria ser validado por um SERVIDOR antes
 * de confiarmos na conta. Sem servidor, esta etapa é
 * apenas local. Quando o back-end existir, envie o
 * idToken a ele.
 * =====================================================
 */
public final class LoginGoogle {

    private static final String TAG = "SalCityLoginGoogle";

    // Retorno do login com Google (sempre na thread principal).
    public interface Callback {
        void onSucesso(UsuarioLogado usuario, String idToken);
        void onCancelado();
        void onErro(String mensagem);
    }

    private LoginGoogle() {
        // Classe utilitária: não deve ser instanciada.
    }

    public static void entrar(@NonNull Activity activity,
                              @NonNull Callback callback) {

        // Sem o ID do cliente Web, o Google recusa o pedido.
        if (!GoogleConfig.estaConfigurado()) {
            callback.onErro("Login com Google ainda não configurado.");
            return;
        }

        CredentialManager gerenciador = CredentialManager.create(activity);

        GetSignInWithGoogleOption opcao =
                new GetSignInWithGoogleOption.Builder(GoogleConfig.WEB_CLIENT_ID)
                        .build();

        GetCredentialRequest pedido = new GetCredentialRequest.Builder()
                .addCredentialOption(opcao)
                .build();

        gerenciador.getCredentialAsync(
                activity,
                pedido,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {

                    @Override
                    public void onResult(GetCredentialResponse resposta) {
                        tratarResposta(resposta, callback);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException erro) {
                        tratarErro(erro, callback);
                    }
                }
        );
    }

    // Converte a credencial recebida em um UsuarioLogado.
    private static void tratarResposta(GetCredentialResponse resposta,
                                       Callback callback) {

        Credential credencial = resposta.getCredential();

        if (credencial instanceof CustomCredential
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        .equals(credencial.getType())) {
            try {
                GoogleIdTokenCredential google = GoogleIdTokenCredential
                        .createFrom(((CustomCredential) credencial).getData());

                String email = google.getId();
                String nome = google.getDisplayName();
                if (nome == null || nome.trim().isEmpty()) {
                    nome = email;
                }

                // O id 0 indica "conta ainda não ligada ao banco local".
                UsuarioLogado usuario = new UsuarioLogado(0L, nome, email);
                callback.onSucesso(usuario, google.getIdToken());

            } catch (Exception e) {
                Log.e(TAG, "Resposta do Google inválida", e);
                callback.onErro("Não foi possível ler a resposta do Google.");
            }
        } else {
            Log.e(TAG, "Tipo de credencial inesperado: " + credencial.getType());
            callback.onErro("Não foi possível entrar com o Google.");
        }
    }

    private static void tratarErro(GetCredentialException erro,
                                   Callback callback) {

        if (erro instanceof GetCredentialCancellationException) {
            // O usuário fechou a janela: não é um erro.
            callback.onCancelado();
        } else if (erro instanceof NoCredentialException) {
            callback.onErro("Nenhuma conta Google encontrada neste aparelho.");
        } else {
            Log.e(TAG, "Falha no login com Google", erro);
            callback.onErro("Não foi possível entrar com o Google. Tente novamente.");
        }
    }
}
