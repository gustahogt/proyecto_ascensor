package ascensor;

/** Estados posibles del ascensor. */
public enum EstadoAscensor {
    DETENIDO,       // quieto, sin solicitudes
    EN_MOVIMIENTO,  // viajando entre pisos
    PUERTAS_ABIERTAS, // detenido en un piso con puertas abiertas
    EMERGENCIA,     // falla detectada, fuera de servicio
}
