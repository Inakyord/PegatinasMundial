package es.upm.etsiinf.proyectofinalpegatinas.data.remote;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Modelo JSON para un jugador.
 */
public class JugadorData implements Serializable {
    @SerializedName("nombre")
    public String nombre;

    @SerializedName("posicion")
    public String posicion;

    @SerializedName("edad")
    public int edad;

    @SerializedName("club")
    public String club;

    @SerializedName("estatura")
    public int estatura;
}
