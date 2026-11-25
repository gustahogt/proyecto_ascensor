package ascensor;

import java.util.TreeSet;

/**
 * La cabina del ascensor: guarda su ubicacion, direccion y estado, y tiene su
 * puerta y sus botones. No decide a donde ir (eso lo hace SistemaControl).
 */
public class Ascensor {
    private final int pisoMinimo;
    private final int pisoMaximo;
    private int pisoActual;
    private Direccion direccion = Direccion.DETENIDO;
    private EstadoAscensor estado = EstadoAscensor.DETENIDO;
    private final Puerta puerta = new Puerta();
    private final BotonCabina[] botonesCabina;
    private boolean fallaDireccion;
    private final BotonMantenerPuertas botonMantener = new BotonMantenerPuertas();

    /** Crea un ascensor que atiende los pisos 1..numPisos, iniciando en pisoInicial. */
    public Ascensor(int numPisos, int pisoInicial) {
        if (numPisos < 2 || pisoInicial < 1 || pisoInicial > numPisos) {
            throw new IllegalArgumentException("Configuracion de ascensor invalida");
        }
        this.pisoMinimo = 1;
        this.pisoMaximo = numPisos;
        this.pisoActual = pisoInicial;
        this.botonesCabina = new BotonCabina[numPisos + 1]; // indice = numero de piso
        for (int p = 1; p <= numPisos; p++) {
            botonesCabina[p] = new BotonCabina(p);
        }
    }

    /** Presiona el boton de un piso destino dentro de la cabina. */
    public boolean seleccionarDestino(int piso) {
        if (piso < pisoMinimo || piso > pisoMaximo || piso == pisoActual
                || estado == EstadoAscensor.EMERGENCIA) {
            return false;
        }
        return botonesCabina[piso].presionar();
    }

    /** Pisos cuyo boton de cabina esta encendido. */
    public TreeSet<Integer> destinosPendientes() {
        TreeSet<Integer> destinos = new TreeSet<>();
        for (int p = pisoMinimo; p <= pisoMaximo; p++) {
            if (botonesCabina[p].isIluminado()) {
                destinos.add(p);
            }
        }
        return destinos;
    }

    /** Mueve la cabina un piso en la direccion actual. */
    public void mover() {
        if (direccion == Direccion.SUBIENDO && pisoActual < pisoMaximo) {
            pisoActual++;
        } else if (direccion == Direccion.BAJANDO && pisoActual > pisoMinimo) {
            pisoActual--;
        }
    }

    /** Cambia la direccion (la logica de cuando cambiar esta en SistemaControl). */
    public void cambiarDireccion(Direccion nueva) {
        this.direccion = nueva;
    }

    /** Se detiene y abre puertas en el piso actual; apaga su boton de cabina. */
    public boolean abrirPuertas() {
        botonesCabina[pisoActual].reiniciar();
        boolean ok = puerta.abrir();
        if (ok) {
            estado = EstadoAscensor.PUERTAS_ABIERTAS;
        }
        return ok;
    }

    /** Presiona "mantener puertas abiertas" (solo tiene efecto con puertas abiertas). */
    public void mantenerPuertasAbiertas() {
        if (botonMantener.presionar()) {
            puerta.mantenerAbierta();
            botonMantener.reiniciar();
        }
    }

    public BotonCabina getBotonCabina(int piso) { return botonesCabina[piso]; }
    public BotonMantenerPuertas getBotonMantener() { return botonMantener; }
    public Puerta getPuerta() { return puerta; }
    public int getPisoActual() { return pisoActual; }
    public int getPisoMinimo() { return pisoMinimo; }
    public int getPisoMaximo() { return pisoMaximo; }
    public Direccion getDireccion() { return direccion; }
    public EstadoAscensor getEstado() { return estado; }
    public boolean isFallaDireccion() { return fallaDireccion; }
    public void setFallaDireccion(boolean f) { this.fallaDireccion = f; }
    public void setEstado(EstadoAscensor estado) { this.estado = estado; }
}
