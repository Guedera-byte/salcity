package com.example.salcity.model;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import com.example.salcity.R;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RoteiroEssencialDados {
    private static final int[] FOTOS = {
            R.drawable.roteiro_elevador, R.drawable.roteiro_mercado, R.drawable.roteiro_pelourinho,
            R.drawable.roteiro_almoco, R.drawable.roteiro_bonfim, R.drawable.roteiro_farol
    };
    
    private static final LatLng[] PONTOS = {
            CoordenadasSalvador.ELEVADOR_LACERDA, CoordenadasSalvador.MERCADO_MODELO,
            CoordenadasSalvador.PELOURINHO, CoordenadasSalvador.ALMOCO,
            CoordenadasSalvador.IGREJA_BONFIM, CoordenadasSalvador.FAROL_BARRA
    };

    private RoteiroEssencialDados() {}

    @NonNull
    public static List<ParadaRoteiro> getParadas(@NonNull Context contexto) {
        Resources res = contexto.getResources();
        String[] horarios = res.getStringArray(R.array.roteiro_essencial_horarios);
        String[] nomes = res.getStringArray(R.array.roteiro_essencial_nomes);
        String[] descricoes = res.getStringArray(R.array.roteiro_essencial_descricoes);
        
        List<ParadaRoteiro> paradas = new ArrayList<>(horarios.length);
        for (int i = 0; i < horarios.length; i++) {
            paradas.add(new ParadaRoteiro(horarios[i], nomes[i], descricoes[i], FOTOS[i], PONTOS[i]));
        }
        return Collections.unmodifiableList(paradas);
    }
}
