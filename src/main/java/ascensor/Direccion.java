package ascensor;

/** Direccion de movimiento del ascensor (o de una solicitud). */
public enum Direccion {
    SUBIENDO, BAJANDO, DETENIDO;

    /** Devuelve la direccion contraria (DETENIDO no tiene contraria). */
    public Direccion opuesta() {
        switch (this) {
            case SUBIENDO: return BAJANDO;
            case BAJANDO: return SUBIENDO;
            default: return DETENIDO;
        }
    }
}
