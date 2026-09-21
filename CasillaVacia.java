public class CasillaVacia implements Entidad {
    @Override 
    public TipoEntidad getTipo() {
        return TipoEntidad.VACIA;
    }

    public char getCaracter() {
        return '.';
    }
}
