package es.upm.etsiinf.proyectofinalpegatinas.data.local;

import java.io.Serializable;
import java.util.List;

/**
 * Modelo de usuario (coleccionista).
 */
public class User implements Serializable {
    private String id;
    private String name;
    private List<UserSticker> collection;

    public User() {
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
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

    public List<UserSticker> getCollection() {
        return collection;
    }

    public void setCollection(List<UserSticker> collection) {
        this.collection = collection;
    }
}
