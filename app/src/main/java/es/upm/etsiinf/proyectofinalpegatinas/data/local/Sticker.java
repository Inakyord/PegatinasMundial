package es.upm.etsiinf.proyectofinalpegatinas.data.local;

import java.io.Serializable;

/**
 * Modelo de datos para un Cromo (Sticker).
 */
public class Sticker implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private String team;
    private String codigoEquipo;
    private String pais;
    private String isoPais;
    private String posicion; // PO, DF, MC, DC
    private String posicionDescripcion;
    private int edad;
    private String club;
    private int estatura;
    private int quantity = 0;

    public Sticker() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Sticker(String id, String name, String imageUrl, String team) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.team = team;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getCodigoEquipo() {
        return codigoEquipo;
    }

    public void setCodigoEquipo(String codigoEquipo) {
        this.codigoEquipo = codigoEquipo;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getIsoPais() {
        return isoPais;
    }

    public void setIsoPais(String isoPais) {
        this.isoPais = isoPais;
    }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public String getPosicionDescripcion() {
        return posicionDescripcion;
    }

    public void setPosicionDescripcion(String posicionDescripcion) {
        this.posicionDescripcion = posicionDescripcion;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getClub() {
        return club;
    }

    public void setClub(String club) {
        this.club = club;
    }

    private String customImageUri;

    public void setCustomImageUri(String customImageUri) {
        this.customImageUri = customImageUri;
    }

    public String getCustomImageUri() {
        return customImageUri;
    }

    public int getEstatura() {
        return estatura;
    }

    public void setEstatura(int estatura) {
        this.estatura = estatura;
    }
}
