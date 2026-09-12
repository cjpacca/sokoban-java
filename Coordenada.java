public class Coordenada {
    private int fila;
    private int col;

    public Coordenada(int fila, int col) {
        this.fila = fila;
        this.col = col;
    }

    public int getFila() {
        return fila;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return "(" + fila + "," + col + ")";
    }
}