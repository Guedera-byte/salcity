package com.example.salcity.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;

public final class ParadaRoteiro {
    private final String horario;
    private final String nome;
    private final String descricao;
    @DrawableRes private final int fotoResId;
    private final LatLng coordenadas;

    public ParadaRoteiro(@NonNull String horario, @NonNull String nome, @NonNull String descricao, @DrawableRes int fotoResId, @NonNull LatLng coordenadas) {
        this.horario = horario;
        this.nome = nome;
        this.descricao = descricao;
        this.fotoResId = fotoResId;
        this.coordenadas = coordenadas;
    }
    
    @NonNull public String getHorario() { return horario; }
    @NonNull public String getNome() { return nome; }
    @NonNull public String getDescricao() { return descricao; }
    @DrawableRes public int getFotoResId() { return fotoResId; }
    @NonNull public LatLng getCoordenadas() { return coordenadas; }
}
