package ascensor;

/** Un piso del edificio: tiene su puerta y botones de llamada (subir/bajar). */
public class Piso {
    private final int numero;
    private final BotonPiso botonSubir; // null en el ultimo piso
    private final BotonPiso botonBajar; // null en el primer piso
    private final Puerta puerta = new Puerta();

    public Piso(int numero, boolean esPrimero, boolean esUltimo) {
        this.numero = numero;
        this.botonSubir = esUltimo ? null : new BotonPiso(numero, Direccion.SUBIENDO);
        this.botonBajar = esPrimero ? null : new BotonPiso(numero, Direccion.BAJANDO);
    }

    /** Boton correspondiente a la direccion pedida (o null si no existe). */
    public BotonPiso getBoton(Direccion d) {
        return d == Direccion.SUBIENDO ? botonSubir : botonBajar;
    }

    public int getNumero() { return numero; }
    public Puerta getPuerta() { return puerta; }
}
