public record Coordenada(int fila, int col) {
    
    public Coordenada {
        if (fila < 0 || fila > 5 || col < 0 || col > 5) {
            throw new IllegalArgumentException("Las coordenadas deben estar entre 0 y 5.");
        }
    }

    @Override
    public String toString() {
        return "(" + fila + "," + col + ")";
    }
}