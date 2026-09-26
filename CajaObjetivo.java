public class CajaObjetivo implements Entidad {
    @Override
    public TipoEntidad getTipo() {
        return TipoEntidad.CAJA_OBJETIVO;
    }

    @Override
    public char getCaracter() {
        return TipoEntidad.CAJA_OBJETIVO.getCaracter();
    }
}
