import java.util.Random;

public class Productor implements Runnable {
    private String idHilo;
    private Tablero tablero;
    private boolean active;
    private Random random;

    public Productor(String idHilo, Tablero tablero) {
        this.idHilo = idHilo;
        this.tablero = tablero;
        this.active = true;
        this.random = new Random();
    }

    @Override 
    public void run() {
        while (active) {
            try {
                // 75% de Caja Objetivo, 25% Bloqueo
                Entidad nuevaCaja;
                if (random.nextInt(100) < 75) {
                    nuevaCaja = new CajaObjetivo();
                } else {
                    nuevaCaja = new CajaBloqueo();
                }
                tablero.insertarCaja(nuevaCaja, idHilo);
                Thread.sleep(600); 
            } catch (InterruptedException e) {
                // Si el hilo principal detiene la simulación
                active = false; 
            }
        }
    }

    public void detener() {
        this.active = false;
    }
}
