package es.upm.etsiinf.proyectofinalpegatinas.data.remote;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Modelo JSON para la raíz del JSON (Torneo).
 */
public class TorneoData implements Serializable {
    @SerializedName("torneo")
    public String torneo;

    @SerializedName("referencia_posiciones")
    public Map<String, String> referenciaPosiciones;

    @SerializedName("equipos")
    public List<EquipoData> equipos;
}
