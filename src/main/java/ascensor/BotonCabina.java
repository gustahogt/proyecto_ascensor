package ascensor;

/** Boton dentro del ascensor que selecciona el piso de destino. */
public class BotonCabina extends Boton {
    private final int pisoDestino;

    public BotonCabina(int pisoDestino) {
        this.pisoDestino = pisoDestino;
    }

    public int getPisoDestino() { return pisoDestino; }

    @Override
    public String descripcion() {
        return "Cabina -> piso " + pisoDestino;
    }
}
