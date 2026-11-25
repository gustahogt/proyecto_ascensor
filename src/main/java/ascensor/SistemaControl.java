package ascensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Cerebro del sistema: recibe las solicitudes (pisos y cabina) y decide cuando
 * mover, parar y cambiar de direccion usando el algoritmo SCAN (como un
 * "ascensor de edificio"): sigue en su direccion atendiendo paradas y solo
 * cambia de sentido cuando ya no quedan solicitudes adelante.
 *
 * La simulacion avanza con {@link #tick()}: cada tick es una unidad de tiempo.
 */
public class SistemaControl {
    private final Piso[] pisos; // indice = numero de piso (1..n)
    private final Ascensor ascensor;
    private final List<String> mensajes = new ArrayList<>();
    private long tiempo; // ticks transcurridos
    private final ModuloAlertas alertas = new ModuloAlertas();
    private final RegistroActividad registro = new RegistroActividad();

    public SistemaControl(int numPisos, int pisoInicial) {
        this.ascensor = new Ascensor(numPisos, pisoInicial);
        this.pisos = new Piso[numPisos + 1];
        for (int p = 1; p <= numPisos; p++) {
            pisos[p] = new Piso(p, p == 1, p == numPisos);
        }
    }

    // ---------------------------------------------------------------
    // Entradas del usuario
    // ---------------------------------------------------------------

    /** Un usuario presiona el boton de subir/bajar en un piso. */
    public boolean llamar(int piso, Direccion direccion) {
        if (piso < 1 || piso >= pisos.length || ascensor.getEstado() == EstadoAscensor.EMERGENCIA) {
            return false;
        }
        BotonPiso boton = pisos[piso].getBoton(direccion);
        if (boton == null) {
            return false; // no existe (ej. subir desde el ultimo piso)
        }
        boolean ok = boton.presionar();
        mensajes.add(ok ? boton.indicar() : "El boton falla: " + boton.descripcion());
        return ok;
    }

    /** Un usuario dentro del ascensor elige un piso de destino. */
    public boolean elegirDestino(int piso) {
        boolean ok = ascensor.seleccionarDestino(piso);
        if (ok) {
            mensajes.add(ascensor.getBotonCabina(piso).indicar());
        }
        return ok;
    }

    // ---------------------------------------------------------------
    // Simulacion
    // ---------------------------------------------------------------

    /** Avanza la simulacion una unidad de tiempo. Devuelve los mensajes generados. */
    public List<String> tick() {
        tiempo++;
        mensajes.clear();
        procesar();
        registro.registrarTick(ascensor.getEstado());
        return new ArrayList<>(mensajes);
    }

    private void procesar() {
        // 0. Revisar fallas; si aparece alguna, entrar en emergencia.
        List<String> nuevas = alertas.revisar(ascensor, pisos);
        for (String n : nuevas) {
            mensajes.add(n);
            registro.registrarFalla();
            registro.registrarEvento(tiempo, n);
        }
        if (!nuevas.isEmpty() && ascensor.getEstado() != EstadoAscensor.EMERGENCIA) {
            activarEmergencia();
        }
        if (ascensor.getEstado() == EstadoAscensor.EMERGENCIA) {
            return;
        }
        // 1. Con puertas abiertas: intentar cerrar (sensor / mantener abiertas).
        if (ascensor.getEstado() == EstadoAscensor.PUERTAS_ABIERTAS) {
            cerrarPuertas();
            return;
        }
        // 2. Hay una solicitud en el piso donde ya estamos: abrir.
        if (debeParar(ascensor.getPisoActual())) {
            parar();
            return;
        }
        // 3. Decidir direccion y avanzar.
        actualizarDireccion();
        if (ascensor.getDireccion() == Direccion.DETENIDO) {
            ascensor.setEstado(EstadoAscensor.DETENIDO);
            return;
        }
        ascensor.mover();
        registro.registrarPisoRecorrido();
        ascensor.setEstado(EstadoAscensor.EN_MOVIMIENTO);
        mensajes.add("Ascensor " + ascensor.getDireccion() + " -> piso " + ascensor.getPisoActual());
        if (debeParar(ascensor.getPisoActual())) {
            parar();
        }
    }

    /** Detiene la cabina en el piso actual y abre ambas puertas. */
    private void parar() {
        int p = ascensor.getPisoActual();
        actualizarDireccion();
        // Apagar los botones de piso atendidos (segun la direccion que tomara).
        Direccion d = ascensor.getDireccion();
        for (Direccion x : new Direccion[] {Direccion.SUBIENDO, Direccion.BAJANDO}) {
            BotonPiso b = pisos[p].getBoton(x);
            if (b != null && (d == Direccion.DETENIDO || d == x)) {
                b.reiniciar();
            }
        }
        ascensor.abrirPuertas();
        pisos[p].getPuerta().abrir();
        registro.registrarParada();
        registro.registrarEvento(tiempo, "Parada en piso " + p);
        mensajes.add("Piso " + p + ": puertas abiertas");
    }

    private void cerrarPuertas() {
        int p = ascensor.getPisoActual();
        boolean cabina = ascensor.getPuerta().intentarCerrar();
        boolean piso = pisos[p].getPuerta().intentarCerrar();
        if (cabina && piso) {
            ascensor.setEstado(EstadoAscensor.DETENIDO);
            mensajes.add("Piso " + p + ": puertas cerradas");
        } else {
            mensajes.add("Piso " + p + ": las puertas siguen abiertas (obstaculo o mantener abiertas)");
        }
    }

    // ---------------------------------------------------------------
    // Emergencias y fallas
    // ---------------------------------------------------------------

    /**
     * Detiene el ascensor en el piso mas cercano y abre las puertas. El
     * ascensor avanza de piso en piso, asi que el mas cercano es el actual.
     */
    private void activarEmergencia() {
        int p = ascensor.getPisoActual();
        ascensor.cambiarDireccion(Direccion.DETENIDO);
        ascensor.setEstado(EstadoAscensor.EMERGENCIA);
        boolean abrio = ascensor.getPuerta().abrir();
        pisos[p].getPuerta().abrir();
        String msg = "EMERGENCIA: ascensor detenido en piso " + p
                + (abrio ? ", puertas abiertas" : " (la puerta de cabina no abre, avisar a mantenimiento)");
        mensajes.add(msg);
        registro.registrarEvento(tiempo, msg);
    }

    /** Simula una falla (para pruebas y demo). piso se ignora si no aplica. */
    public void simularFalla(TipoFalla tipo, int piso) {
        cambiarFalla(tipo, piso, true);
    }

    /** Repara una falla; si ya no quedan fallas el ascensor vuelve a servicio. */
    public void repararFalla(TipoFalla tipo, int piso) {
        cambiarFalla(tipo, piso, false);
        alertas.revisar(ascensor, pisos); // actualiza las fallas activas
        if (ascensor.getEstado() == EstadoAscensor.EMERGENCIA && !alertas.hayFallasActivas()) {
            // Si las puertas quedaron abiertas, se cerraran antes de continuar.
            ascensor.setEstado(ascensor.getPuerta().isAbierta()
                    ? EstadoAscensor.PUERTAS_ABIERTAS : EstadoAscensor.DETENIDO);
            registro.registrarEvento(tiempo, "Ascensor reparado, vuelve a servicio");
        }
    }

    private void cambiarFalla(TipoFalla tipo, int piso, boolean valor) {
        switch (tipo) {
            case BOTON_PISO:
                for (Direccion d : new Direccion[] {Direccion.SUBIENDO, Direccion.BAJANDO}) {
                    BotonPiso b = pisos[piso].getBoton(d);
                    if (b != null) b.setFallando(valor);
                }
                break;
            case BOTON_CABINA: ascensor.getBotonCabina(piso).setFallando(valor); break;
            case PUERTA_CABINA: ascensor.getPuerta().setFallando(valor); break;
            case PUERTA_PISO: pisos[piso].getPuerta().setFallando(valor); break;
            case SENSOR_PUERTA: pisos[piso].getPuerta().getSensor().setFallando(valor); break;
            case DIRECCION: ascensor.setFallaDireccion(valor); break;
        }
    }

    // ---------------------------------------------------------------
    // Logica SCAN
    // ---------------------------------------------------------------

    /** Hay alguna solicitud (cabina o piso) en un piso estrictamente mayor a p. */
    private boolean hayPendientesArriba(int p) {
        for (int i = p + 1; i < pisos.length; i++) {
            if (haySolicitudEn(i)) return true;
        }
        return false;
    }

    /** Hay alguna solicitud en un piso estrictamente menor a p. */
    private boolean hayPendientesAbajo(int p) {
        for (int i = 1; i < p; i++) {
            if (haySolicitudEn(i)) return true;
        }
        return false;
    }

    private boolean haySolicitudEn(int p) {
        if (ascensor.getBotonCabina(p).isIluminado()) return true;
        BotonPiso s = pisos[p].getBoton(Direccion.SUBIENDO);
        BotonPiso b = pisos[p].getBoton(Direccion.BAJANDO);
        return (s != null && s.isIluminado()) || (b != null && b.isIluminado());
    }

    /** Decide si el ascensor debe detenerse en el piso p con su direccion actual. */
    private boolean debeParar(int p) {
        if (ascensor.getBotonCabina(p).isIluminado()) return true;
        Direccion d = ascensor.getDireccion();
        BotonPiso subir = pisos[p].getBoton(Direccion.SUBIENDO);
        BotonPiso bajar = pisos[p].getBoton(Direccion.BAJANDO);
        boolean pideSubir = subir != null && subir.isIluminado();
        boolean pideBajar = bajar != null && bajar.isIluminado();
        if (d == Direccion.SUBIENDO) {
            // Misma direccion, o es el ultimo punto de la subida (punto de giro).
            return pideSubir || (pideBajar && !hayPendientesArriba(p));
        }
        if (d == Direccion.BAJANDO) {
            return pideBajar || (pideSubir && !hayPendientesAbajo(p));
        }
        return pideSubir || pideBajar; // detenido: atender lo que haya aqui
    }

    /**
     * Cambio de direccion: se mantiene la actual mientras haya solicitudes
     * adelante; solo se invierte si no quedan, y se detiene si no hay ninguna.
     */
    private void actualizarDireccion() {
        int p = ascensor.getPisoActual();
        boolean arriba = hayPendientesArriba(p);
        boolean abajo = hayPendientesAbajo(p);
        Direccion actual = ascensor.getDireccion();
        Direccion nueva;
        if (actual == Direccion.SUBIENDO && arriba) {
            nueva = Direccion.SUBIENDO;
        } else if (actual == Direccion.BAJANDO && abajo) {
            nueva = Direccion.BAJANDO;
        } else if (arriba && !abajo) {
            nueva = Direccion.SUBIENDO;
        } else if (abajo && !arriba) {
            nueva = Direccion.BAJANDO;
        } else if (arriba) { // ambos lados: ir al mas cercano
            nueva = masCercanoArriba(p) - p <= p - masCercanoAbajo(p)
                    ? Direccion.SUBIENDO : Direccion.BAJANDO;
        } else {
            nueva = Direccion.DETENIDO;
        }
        if (nueva != actual) {
            ascensor.cambiarDireccion(nueva);
            if (nueva != Direccion.DETENIDO) {
                mensajes.add("Cambio de direccion: " + nueva);
            }
        }
    }

    private int masCercanoArriba(int p) {
        for (int i = p + 1; i < pisos.length; i++) if (haySolicitudEn(i)) return i;
        return Integer.MAX_VALUE;
    }

    private int masCercanoAbajo(int p) {
        for (int i = p - 1; i >= 1; i--) if (haySolicitudEn(i)) return i;
        return Integer.MIN_VALUE;
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------
    public Ascensor getAscensor() { return ascensor; }
    public Piso getPiso(int numero) { return pisos[numero]; }
    public int getNumPisos() { return pisos.length - 1; }
    public ModuloAlertas getAlertas() { return alertas; }
    public RegistroActividad getRegistro() { return registro; }
    public long getTiempo() { return tiempo; }
}
