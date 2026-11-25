package ascensor;

/**
 * Boton generico (clase abstracta). Los botones de piso y de cabina heredan de
 * ella y definen su propia descripcion (polimorfismo).
 */
public abstract class Boton {
    private boolean iluminado;
    private boolean fallando;

    /**
     * Presiona el boton. Si esta fallando no registra la solicitud.
     * @return true si la solicitud fue registrada (luz + sonido)
     */
    public boolean presionar() {
        if (fallando) {
            return false;
        }
        iluminado = true;
        return true;
    }

    /** Apaga la luz del boton (la solicitud ya fue atendida). */
    public void reiniciar() {
        iluminado = false;
    }

    /** Texto del indicador visual/sonoro que ve el usuario. */
    public String indicar() {
        return iluminado ? "[LUZ ON + beep] " + descripcion() : "[luz off] " + descripcion();
    }

    /** Descripcion del boton; cada subclase la define. */
    public abstract String descripcion();

    public boolean isIluminado() { return iluminado; }
    public boolean isFallando() { return fallando; }
    public void setFallando(boolean fallando) { this.fallando = fallando; }
}
