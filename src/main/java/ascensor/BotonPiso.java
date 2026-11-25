package ascensor;

/** Boton de llamada ubicado en un piso: pide el ascensor para subir o bajar. */
public class BotonPiso extends Boton {
    private final int piso;
    private final Direccion direccion; // SUBIENDO o BAJANDO

    public BotonPiso(int piso, Direccion direccion) {
        if (direccion == Direccion.DETENIDO) {
            throw new IllegalArgumentException("Un boton de piso debe ser de subida o bajada");
        }
        this.piso = piso;
        this.direccion = direccion;
    }

    public int getPiso() { return piso; }
    public Direccion getDireccion() { return direccion; }

    @Override
    public String descripcion() {
        return "Piso " + piso + " " + (direccion == Direccion.SUBIENDO ? "subir" : "bajar");
    }
}
