package ascensor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Detecta fallas en botones, puertas, sensores y direccion, y notifica al
 * equipo de mantenimiento (aqui: guarda y devuelve el mensaje de alerta).
 */
public class ModuloAlertas {
    private final Set<String> activas = new HashSet<>();
    private final List<String> historial = new ArrayList<>();

    /**
     * Revisa todos los componentes y devuelve las fallas NUEVAS detectadas.
     * Una falla ya notificada no se vuelve a reportar hasta que se repare.
     */
    public List<String> revisar(Ascensor a, Piso[] pisos) {
        List<String> encontradas = new ArrayList<>();
        for (int p = a.getPisoMinimo(); p <= a.getPisoMaximo(); p++) {
            if (a.getBotonCabina(p).isFallando())
                encontradas.add("Falla en " + a.getBotonCabina(p).descripcion());
            for (Direccion d : new Direccion[] {Direccion.SUBIENDO, Direccion.BAJANDO}) {
                BotonPiso b = pisos[p].getBoton(d);
                if (b != null && b.isFallando()) encontradas.add("Falla en boton " + b.descripcion());
            }
            if (pisos[p].getPuerta().isFallando())
                encontradas.add("Falla en puerta del piso " + p);
            if (pisos[p].getPuerta().getSensor().isFallando())
                encontradas.add("Falla en sensor de puerta del piso " + p);
        }
        if (a.getBotonMantener().isFallando())
            encontradas.add("Falla en boton mantener puertas abiertas");
        if (a.getPuerta().isFallando()) encontradas.add("Falla en puerta de la cabina");
        if (a.getPuerta().getSensor().isFallando())
            encontradas.add("Falla en sensor de puerta de la cabina");
        if (a.isFallaDireccion()) encontradas.add("Falla en el sistema de direccion");

        List<String> nuevas = new ArrayList<>();
        for (String f : encontradas) {
            if (activas.add(f)) {
                String alerta = "ALERTA a mantenimiento: " + f;
                historial.add(alerta);
                nuevas.add(alerta);
            }
        }
        activas.retainAll(encontradas); // fallas reparadas dejan de estar activas
        return nuevas;
    }

    public boolean hayFallasActivas() { return !activas.isEmpty(); }
    public List<String> getHistorial() { return new ArrayList<>(historial); }
}
