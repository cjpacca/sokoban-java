public class CajaBloqueo implements Entidad {
    @Override
    public TipoEntidad getTipo() {
        return TipoEntidad.CAJA_BLOQUEO;
    }

    @Override
    public char getCaracter() {
        return TipoEntidad.CAJA_BLOQUEO.getCaracter();
    }
}
