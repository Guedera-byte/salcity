package com.example.salcity.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Coordenadas de referência usadas no protótipo.
 */
public final class CoordenadasSalvador {
    public static final double CENTRO_LATITUDE  = -12.968d;
    public static final double CENTRO_LONGITUDE = -38.512d;
    public static final float  ZOOM_INICIAL     = 11.5f;

    public static final LatLng ELEVADOR_LACERDA = new LatLng(-12.9741d, -38.5137d);
    public static final LatLng MERCADO_MODELO   = new LatLng(-12.9718d, -38.5147d);
    public static final LatLng PELOURINHO       = new LatLng(-12.9730d, -38.5090d);
    public static final LatLng ALMOCO           = new LatLng(-12.9730d, -38.5090d);
    public static final LatLng IGREJA_BONFIM    = new LatLng(-12.9237d, -38.5087d);
    public static final LatLng FAROL_BARRA      = new LatLng(-13.0106d, -38.5325d);

    private CoordenadasSalvador() {}
    
    public static LatLng centroDoMapa() { 
        return new LatLng(CENTRO_LATITUDE, CENTRO_LONGITUDE); 
    }
}
