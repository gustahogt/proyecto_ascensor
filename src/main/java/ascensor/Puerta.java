package ascensor;

/**
 * Puerta (de cabina o de piso). Se abre y cierra automaticamente; el sensor
 * impide el cierre si hay obstaculo y se puede pedir mantenerla abierta.
 */
public class Puerta {
    private boolean abierta;
    private boolean mantenerAbierta;
    private boolean fallando;
    private final SensorObstaculo sensor = new SensorObstaculo();

    /** Abre la puerta. Una puerta averiada no abre. */
    public boolean abrir() {
        if (fallando) {
            return false;
        }
        abierta = true;
        return true;
    }

    /**
     * Intenta cerrar la puerta.
     * @return true si quedo cerrada; false si el sensor detecta obstaculo,
     *         alguien pidio mantenerla abierta, o la puerta esta averiada.
     */
    public boolean intentarCerrar() {
        if (!abierta) {
            return true;
        }
        if (fallando || sensor.detectaObstaculo()) {
            return false;
        }
        if (mantenerAbierta) {
            mantenerAbierta = false; // la peticion extiende un ciclo
            return false;
        }
        abierta = false;
        return true;
    }

    /** Activa "mantener puertas abiertas" para el proximo intento de cierre. */
    public void mantenerAbierta() {
        if (abierta) {
            mantenerAbierta = true;
        }
    }

    public boolean isAbierta() { return abierta; }
    public boolean isFallando() { return fallando; }
    public void setFallando(boolean fallando) { this.fallando = fallando; }
    public SensorObstaculo getSensor() { return sensor; }
}
