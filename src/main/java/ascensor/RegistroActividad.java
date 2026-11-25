package ascensor;

import java.util.ArrayList;
import java.util.List;

/** Registra la actividad del ascensor para analisis de mantenimiento. */
public class RegistroActividad {
    private long ticksTotales;
    private long ticksEnMovimiento;
    private long ticksPuertasAbiertas;
    private int pisosRecorridos;
    private int paradas;
    private int fallas;
    private final List<String> eventos = new ArrayList<>();

    /** Se llama una vez por tick con el estado en que estuvo el ascensor. */
    public void registrarTick(EstadoAscensor estado) {
        ticksTotales++;
        if (estado == EstadoAscensor.EN_MOVIMIENTO) ticksEnMovimiento++;
        if (estado == EstadoAscensor.PUERTAS_ABIERTAS) ticksPuertasAbiertas++;
    }

    public void registrarPisoRecorrido() { pisosRecorridos++; }
    public void registrarParada() { paradas++; }
    public void registrarFalla() { fallas++; }

    public void registrarEvento(long tiempo, String texto) {
        eventos.add("[t=" + tiempo + "] " + texto);
    }

    public List<String> getEventos() { return new ArrayList<>(eventos); }
    public long getTicksTotales() { return ticksTotales; }
    public long getTicksEnMovimiento() { return ticksEnMovimiento; }
    public int getPisosRecorridos() { return pisosRecorridos; }
    public int getParadas() { return paradas; }
    public int getFallas() { return fallas; }

    /** Resumen de desempeno. */
    public String resumen() {
        return "Tiempo total: " + ticksTotales + " ticks | en movimiento: " + ticksEnMovimiento
                + " | puertas abiertas: " + ticksPuertasAbiertas
                + "\nPisos recorridos: " + pisosRecorridos + " | paradas: " + paradas
                + " | fallas: " + fallas;
    }
}
