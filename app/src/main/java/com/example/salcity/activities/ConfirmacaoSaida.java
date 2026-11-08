package com.example.salcity.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.salcity.R;
import com.example.salcity.data.SessaoUsuario;

/**
 * Janela reutilizável de confirmação de saída do SalCity.
 * Ao confirmar, encerra a sessão (SessaoUsuario) e retorna ao
 * LoginActivity. Os demais dados do app não são apagados.
 */
public final class ConfirmacaoSaida {

    private ConfirmacaoSaida() {
        // Classe utilitária: não deve ser instanciada.
    }

    /**
     * Exibe a confirmação sobre a Activity atual.
     * @param activity Activity que está exibindo a Home ou outra tela.
     * @param logoResId ID da logo original em drawable; use 0 para mostrar texto alternativo.
     */
    public static void mostrar(Activity activity, int logoResId) {
        if (activity.isFinishing() || activity.isDestroyed()) return;

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sair_salcity);
        dialog.setCancelable(true);

        // Usa a logo original já presente no projeto, sem recriá-la.
        ImageView logo = dialog.findViewById(R.id.imgLogoSair);
        TextView alternativa = dialog.findViewById(R.id.txtLogoAlternativaSair);
        if (logoResId != 0) {
            logo.setImageResource(logoResId);
            logo.setVisibility(View.VISIBLE);
            alternativa.setVisibility(View.GONE);
        }

        // Cancelar: fecha apenas a janela.
        dialog.findViewById(R.id.btnCancelarSair).setOnClickListener(v -> dialog.dismiss());

        // Sair: encerra a sessão, retorna ao login e limpa a pilha de telas.
        // Apenas os dados da sessão são apagados.
        dialog.findViewById(R.id.btnConfirmarSair).setOnClickListener(v -> {
            dialog.dismiss();
            SessaoUsuario.encerrar(activity);
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = activity.getResources().getDisplayMetrics().widthPixels
                    - dp(activity, 32);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.dimAmount = 0.65f;
            window.setAttributes(params);
        }
    }

    /** Converte dp em pixels para manter margens consistentes entre aparelhos. */
    private static int dp(Activity activity, int valor) {
        return Math.round(valor * activity.getResources().getDisplayMetrics().density);
    }
}
