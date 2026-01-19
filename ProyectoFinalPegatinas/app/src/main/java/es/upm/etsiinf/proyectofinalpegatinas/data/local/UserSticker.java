package es.upm.etsiinf.proyectofinalpegatinas.data.local;

import java.io.Serializable;

/**
 * Relación entre un usuario y un cromo. Indica cantidad (tengo/falta/repetida).
 */
public class UserSticker implements Serializable {
    private String userId;
    private String stickerId;
    private int quantity; // 0 = faltante, 1 = se tiene, >1 = repetida

    public UserSticker() {
    }

    public UserSticker(String userId, String stickerId, int quantity) {
        this.userId = userId;
        this.stickerId = stickerId;
        this.quantity = quantity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
