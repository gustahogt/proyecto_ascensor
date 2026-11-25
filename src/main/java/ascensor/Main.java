package ascensor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** Interfaz de linea de comandos del sistema de ascensores. */
public class Main {
    private static final int NUM_PISOS = 5;
    private static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        SistemaControl sistema = new SistemaControl(NUM_PISOS, 1);
        boolean salir = false;
        while (!salir) {
            limpiar();
            mostrarMenu();
            String op = leerLinea();
            if (op == null) break; // fin de la entrada
            limpiar();
            switch (op.trim()) {
                case "1": llamar(sistema); break;
                case "2": destino(sistema); break;
                case "3":
                    sistema.getAscensor().mantenerPuertasAbiertas();
                    System.out.println("Mantener puertas abiertas: solicitado");
                    pausar();
                    break;
                case "4": avanzar(sistema, 1); break;
                case "5":
                    int n = pedirEntero("Cuantos ticks? ", 1, 100);
                    if (n > 0) avanzar(sistema, n);
                    break;
                case "6": obstaculo(sistema); break;
                case "7": falla(sistema, true); break;
                case "8": falla(sistema, false); break;
                case "9": mostrarEstado(sistema); pausar(); break;
                case "10":
                    System.out.println(sistema.getRegistro().resumen());
                    sistema.getRegistro().getEventos().forEach(System.out::println);
                    pausar();
                    break;
                case "0": salir = true; break;
                default:
                    System.out.println("Opcion no valida");
                    pausar();
            }
        }
        limpiar();
        System.out.println("Hasta luego.");
    }

    private static void mostrarMenu() {
        System.out.println("=== Sistema de Control de Ascensores (" + NUM_PISOS + " pisos) ===\n");
        System.out.println(" 1) Llamar desde un piso");
        System.out.println(" 2) Elegir destino en cabina");
        System.out.println(" 3) Mantener puertas abiertas");
        System.out.println(" 4) Avanzar 1 tick");
        System.out.println(" 5) Avanzar N ticks");
        System.out.println(" 6) Obstaculo en puerta");
        System.out.println(" 7) Simular falla");
        System.out.println(" 8) Reparar falla");
        System.out.println(" 9) Ver estado");
        System.out.println("10) Ver registro de actividad");
        System.out.println(" 0) Salir");
        System.out.print("\n> ");
    }

    private static void llamar(SistemaControl s) {
        int piso = pedirEntero("Piso (1-" + NUM_PISOS + "): ", 1, NUM_PISOS);
        if (piso < 0) return;
        Direccion dir;
        if (piso == 1) {
            System.out.println("Piso 1: solo se puede subir.");
            dir = Direccion.SUBIENDO;
        } else if (piso == NUM_PISOS) {
            System.out.println("Piso " + NUM_PISOS + ": solo se puede bajar.");
            dir = Direccion.BAJANDO;
        } else {
            int d = pedirEntero("1=subir 2=bajar: ", 1, 2);
            if (d < 0) return;
            dir = d == 1 ? Direccion.SUBIENDO : Direccion.BAJANDO;
        }
        boolean ok = s.llamar(piso, dir);
        System.out.println(ok ? "Solicitud registrada" : "No se pudo registrar la solicitud");
        pausar();
    }

    private static void destino(SistemaControl s) {
        int piso = pedirEntero("Piso destino (1-" + NUM_PISOS + "): ", 1, NUM_PISOS);
        if (piso < 0) return;
        System.out.println(s.elegirDestino(piso) ? "Destino registrado" : "Destino no valido");
        pausar();
    }

    private static void obstaculo(SistemaControl s) {
        int op = pedirEntero("1=poner obstaculo 2=quitar: ", 1, 2);
        if (op < 0) return;
        boolean hay = op == 1;
        int piso = s.getAscensor().getPisoActual();
        s.getAscensor().getPuerta().getSensor().setObstaculo(hay);
        s.getPiso(piso).getPuerta().getSensor().setObstaculo(hay);
        System.out.println("Obstaculo " + (hay ? "colocado" : "retirado") + " en piso " + piso);
        pausar();
    }

    private static void falla(SistemaControl s, boolean provocar) {
        TipoFalla[] tipos = TipoFalla.values();
        for (int i = 0; i < tipos.length; i++) System.out.println((i + 1) + ") " + tipos[i]);
        int t = pedirEntero("Tipo: ", 1, tipos.length);
        if (t < 0) return;
        int piso = pedirEntero("Piso afectado (1-" + NUM_PISOS + "): ", 1, NUM_PISOS);
        if (piso < 0) return;
        if (provocar) s.simularFalla(tipos[t - 1], piso); else s.repararFalla(tipos[t - 1], piso);
        System.out.println((provocar ? "Falla simulada" : "Falla reparada")
                + " (se detecta en el proximo tick)");
        pausar();
    }

    private static void avanzar(SistemaControl s, int n) {
        for (int i = 0; i < n; i++) {
            for (String m : s.tick()) System.out.println("  t=" + s.getTiempo() + " " + m);
        }
        pausar();
    }

    private static void mostrarEstado(SistemaControl s) {
        Ascensor a = s.getAscensor();
        System.out.println("Piso " + a.getPisoActual() + " | " + a.getDireccion() + " | "
                + a.getEstado() + " | puerta " + (a.getPuerta().isAbierta() ? "abierta" : "cerrada"));
        System.out.println("Destinos en cabina: " + a.destinosPendientes());
        StringBuilder sb = new StringBuilder("Llamadas en pisos: ");
        for (int p = 1; p <= s.getNumPisos(); p++) {
            for (Direccion d : new Direccion[] {Direccion.SUBIENDO, Direccion.BAJANDO}) {
                BotonPiso b = s.getPiso(p).getBoton(d);
                if (b != null && b.isIluminado()) sb.append(p).append(d == Direccion.SUBIENDO ? "^ " : "v ");
            }
        }
        System.out.println(sb);
    }

    // ---------------------------------------------------------------
    // Utilidades de terminal
    // ---------------------------------------------------------------

    /** Limpia la terminal (secuencia ANSI: borrar pantalla y volver al inicio). */
    private static void limpiar() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Lee una linea; devuelve null si se acabo la entrada. */
    private static String leerLinea() {
        try {
            return IN.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Espera a que el usuario presione cualquier tecla. Pone la terminal en
     * modo sin buffer con stty; si no es posible, pide ENTER.
     */
    private static void pausar() {
        System.out.print("\nPresione cualquier tecla para continuar...");
        System.out.flush();
        boolean raw = sttyRaw(true);
        try {
            if (raw) IN.read(); else IN.readLine();
        } catch (IOException e) {
            // se ignora: simplemente se continua
        } finally {
            if (raw) sttyRaw(false);
        }
        System.out.println();
    }

    /** Activa/desactiva el modo "una tecla a la vez" (solo Linux/macOS). */
    private static boolean sttyRaw(boolean activar) {
        try {
            String cmd = activar ? "stty -icanon -echo min 1 < /dev/tty" : "stty icanon echo < /dev/tty";
            return new ProcessBuilder("sh", "-c", cmd).inheritIO().start().waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Pide un entero en [min, max]. Si es invalido muestra el error, pausa y
     * vuelve a pedir (limpiando la pantalla). Devuelve -1 si se acabo la entrada.
     */
    private static int pedirEntero(String texto, int min, int max) {
        while (true) {
            System.out.print(texto);
            String linea = leerLinea();
            if (linea == null) return -1;
            try {
                int v = Integer.parseInt(linea.trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException e) {
                // se informa abajo
            }
            System.out.println("Valor invalido, debe estar entre " + min + " y " + max);
            pausar();
            limpiar();
        }
    }
}
