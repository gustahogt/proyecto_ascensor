package ascensor;

import java.util.List;

/**
 * Pruebas sin dependencias externas (no se necesita JUnit). Cada prueba usa
 * {@link #verificar}; al final se imprime el resumen y se sale con codigo 1 si
 * alguna falla. Ejecutar con: ./build.sh test
 */
public class PruebasSistema {
    private static int ok;
    private static int fallos;

    public static void main(String[] args) {
        botones();
        puertas();
        scanSigueDireccion();
        cambioDeDireccion();
        puntoDeGiro();
        sensorYMantenerAbiertas();
        emergenciaYReparacion();
        registroActividad();
        System.out.println("\nPruebas OK: " + ok + " | FALLOS: " + fallos);
        if (fallos > 0) System.exit(1);
    }

    private static void verificar(String nombre, boolean condicion) {
        if (condicion) { ok++; System.out.println("  ok    " + nombre); }
        else { fallos++; System.out.println("  FALLO " + nombre); }
    }

    private static void avanzar(SistemaControl s, int n) {
        for (int i = 0; i < n; i++) s.tick();
    }

    static void botones() {
        System.out.println("Botones");
        Boton b = new BotonPiso(3, Direccion.SUBIENDO);
        verificar("presionar enciende la luz", b.presionar() && b.isIluminado());
        verificar("indicador muestra luz encendida", b.indicar().contains("LUZ ON"));
        b.reiniciar();
        verificar("reiniciar apaga la luz", !b.isIluminado());
        b.setFallando(true);
        verificar("boton con falla no registra", !b.presionar() && !b.isIluminado());
        Boton[] todos = {new BotonPiso(1, Direccion.BAJANDO), new BotonCabina(4), new BotonMantenerPuertas()};
        verificar("polimorfismo: descripciones distintas",
                !todos[0].descripcion().equals(todos[1].descripcion()));
    }

    static void puertas() {
        System.out.println("Puertas");
        Puerta p = new Puerta();
        verificar("abrir", p.abrir() && p.isAbierta());
        verificar("cerrar sin obstaculo", p.intentarCerrar() && !p.isAbierta());
    }

    static void scanSigueDireccion() {
        System.out.println("SCAN: sigue en su direccion");
        SistemaControl s = new SistemaControl(10, 1);
        s.elegirDestino(8);
        s.llamar(4, Direccion.SUBIENDO);
        s.llamar(6, Direccion.BAJANDO); // opuesta: no debe parar yendo hacia 8
        boolean paroEn4 = false, paroEn6 = false;
        for (int i = 0; i < 20 && s.getAscensor().getPisoActual() < 8
                || s.getAscensor().getEstado() == EstadoAscensor.PUERTAS_ABIERTAS && i < 20; i++) {
            s.tick();
            if (s.getAscensor().getEstado() == EstadoAscensor.PUERTAS_ABIERTAS) {
                if (s.getAscensor().getPisoActual() == 4) paroEn4 = true;
                if (s.getAscensor().getPisoActual() == 6) paroEn6 = true;
            }
        }
        verificar("para en 4 (misma direccion)", paroEn4);
        verificar("no para en 6 (llamada de bajada) al subir", !paroEn6);
    }

    static void cambioDeDireccion() {
        System.out.println("Cambio de direccion");
        SistemaControl s = new SistemaControl(10, 1);
        s.elegirDestino(5);
        avanzar(s, 6); // llega a 5 y abre
        verificar("llego al piso 5", s.getAscensor().getPisoActual() == 5);
        s.llamar(2, Direccion.SUBIENDO);
        avanzar(s, 3);
        verificar("sin solicitudes arriba, baja", s.getAscensor().getDireccion() == Direccion.BAJANDO
                || s.getAscensor().getPisoActual() < 5);
        avanzar(s, 10);
        verificar("atiende la llamada del piso 2", s.getAscensor().getPisoActual() == 2);
        verificar("queda detenido sin solicitudes", s.getAscensor().getDireccion() == Direccion.DETENIDO);
    }

    static void puntoDeGiro() {
        System.out.println("Punto de giro");
        SistemaControl s = new SistemaControl(10, 1);
        s.llamar(7, Direccion.BAJANDO); // nadie mas: sube a 7 y atiende aunque quiera bajar
        avanzar(s, 6); // 6 pisos de subida; abre al llegar
        verificar("llega al 7 para la llamada de bajada", s.getAscensor().getPisoActual() == 7);
        verificar("puertas abiertas en 7", s.getAscensor().getPuerta().isAbierta());
    }

    static void sensorYMantenerAbiertas() {
        System.out.println("Sensor de obstaculo y mantener abiertas");
        SistemaControl s = new SistemaControl(5, 1);
        s.elegirDestino(3);
        avanzar(s, 2); // llega a 3, puertas abiertas
        s.getAscensor().getPuerta().getSensor().setObstaculo(true);
        avanzar(s, 3);
        verificar("obstaculo impide cerrar", s.getAscensor().getPuerta().isAbierta());
        s.getAscensor().getPuerta().getSensor().setObstaculo(false);
        s.getAscensor().mantenerPuertasAbiertas();
        s.tick();
        verificar("mantener abiertas extiende un ciclo", s.getAscensor().getPuerta().isAbierta());
        avanzar(s, 2);
        verificar("luego cierra", !s.getAscensor().getPuerta().isAbierta());
    }

    static void emergenciaYReparacion() {
        System.out.println("Emergencia");
        SistemaControl s = new SistemaControl(10, 1);
        s.elegirDestino(9);
        avanzar(s, 3);
        s.simularFalla(TipoFalla.DIRECCION, 1);
        List<String> msgs = s.tick();
        verificar("se notifica a mantenimiento", msgs.stream().anyMatch(m -> m.contains("ALERTA")));
        verificar("estado EMERGENCIA", s.getAscensor().getEstado() == EstadoAscensor.EMERGENCIA);
        verificar("puertas abiertas en emergencia", s.getAscensor().getPuerta().isAbierta());
        int piso = s.getAscensor().getPisoActual();
        avanzar(s, 3);
        verificar("no se mueve en emergencia", s.getAscensor().getPisoActual() == piso);
        s.repararFalla(TipoFalla.DIRECCION, 1);
        verificar("reparado sale de emergencia", s.getAscensor().getEstado() != EstadoAscensor.EMERGENCIA);
        avanzar(s, 15);
        verificar("retoma y llega al destino 9", s.getAscensor().getPisoActual() == 9);
    }

    static void registroActividad() {
        System.out.println("Registro de actividad");
        SistemaControl s = new SistemaControl(10, 1);
        s.elegirDestino(4);
        avanzar(s, 6);
        RegistroActividad r = s.getRegistro();
        verificar("cuenta pisos recorridos", r.getPisosRecorridos() == 3);
        verificar("cuenta paradas", r.getParadas() == 1);
        verificar("cuenta ticks", r.getTicksTotales() == 6);
    }
}
