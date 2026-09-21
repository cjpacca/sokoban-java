public enum TipoEntidad {
    VACIA('.'),
    ROBOT('R'),
    CAJA_OBJETIVO('O'),
    CAJA_BLOQUEO('X');

    private final char caracter;

    TipoEntidad(char caracter) {
        this.caracter = caracter;
    }

    public char getCaracter() {
        return caracter;
    }
}