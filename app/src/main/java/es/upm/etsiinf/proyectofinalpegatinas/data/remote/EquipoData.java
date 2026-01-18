package es.upm.etsiinf.proyectofinalpegatinas.data.remote;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Modelo JSON para un equipo.
 */
public class EquipoData implements Serializable {
    @SerializedName("pais")
    public String pais;

    @SerializedName("codigo")
    public String codigo;

    @SerializedName("iso_pais")
    public String isoPais;

    @SerializedName("jugadores")
    public List<JugadorData> jugadores;
}
