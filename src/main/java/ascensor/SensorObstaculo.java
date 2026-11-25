package ascensor;

/** Sensor de seguridad de una puerta: detecta si hay un obstaculo en el umbral. */
public class SensorObstaculo {
    private boolean obstaculo;
    private boolean fallando;

    public boolean detectaObstaculo() { return obstaculo; }
    public void setObstaculo(boolean obstaculo) { this.obstaculo = obstaculo; }
    public boolean isFallando() { return fallando; }
    public void setFallando(boolean fallando) { this.fallando = fallando; }
}
