import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.List;

public class Tablero {
    private final Lock lock = new ReentrantLock();
    private final Condition noFull = lock.newCondition();
    private final Condition noObjetivo = lock.newCondition();

    private final Entidad[][] _tablero = new Entidad[6][6];

    private int cajasObjetivo, extraidos, saturaciones, agotados, tick, celdasOcupadas;

    private void registrarTraza(String idHilo, String accion, String detalles) {
        this.tick++;
        String tickFormat = String.format("%02d", this.tick); 
        String espacio = detalles.isEmpty() ? "" : " ";
        System.out.println("[Tick-" + tickFormat + "] [" + idHilo + "] " + accion + espacio + detalles);
    }

    public Tablero() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                _tablero[i][j] = new CasillaVacia();
            }
        }
    }
    
    public void insertarCaja(Entidad nuevaCaja, String idHilo) throws InterruptedException {
        lock.lock();
        try {
            while(celdasOcupadas == 36) {
                saturaciones++;
                registrarTraza(idHilo, "ESPERA SATURACION", "");
                noFull.await(); 
            }
            
            List<Coordenada> disponibles = new ArrayList<>();
            Random random = new Random();
            for(int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    if(_tablero[i][j].getTipo() == TipoEntidad.VACIA) {
                        disponibles.add(new Coordenada(i, j));
                    }
                }
            }
            
            int posicionCoordenada = random.nextInt(disponibles.size());
            
            Coordenada coordenadaAInsertar = disponibles.get(posicionCoordenada);
            
            _tablero[coordenadaAInsertar.fila()][coordenadaAInsertar.col()] = nuevaCaja;
            celdasOcupadas++;
            
            String tipoAccion = (nuevaCaja.getTipo() == TipoEntidad.CAJA_OBJETIVO) ? "INSERTAR OBJETIVO ->" : "INSERTAR BLOQUEO ->";
            registrarTraza(idHilo, tipoAccion, coordenadaAInsertar.toString());
            
            if (nuevaCaja.getTipo() == TipoEntidad.CAJA_OBJETIVO) {
                this.cajasObjetivo++;
                noObjetivo.signal(); 
            }
            
        } finally {
            lock.unlock();
        }
    }

    public boolean solicitarMovimiento(Coordenada origen, Coordenada destino, String idHilo) {
        lock.lock();
        try {
            if (cajasObjetivo == 0) {
                return false;
            }
            TipoEntidad tipoEntidadDestino = _tablero[destino.fila()][destino.col()].getTipo();
            if(tipoEntidadDestino == TipoEntidad.VACIA) {
                _tablero[destino.fila()][destino.col()] = _tablero[origen.fila()][origen.col()];
                _tablero[origen.fila()][origen.col()] = new CasillaVacia();
                registrarTraza(idHilo, "MOVIMIENTO ->", destino.toString());
            } else if (tipoEntidadDestino == TipoEntidad.ROBOT) {
                return false;
            } else {
                return empujarCaja(origen, destino, idHilo);
            }
            
            return true;
        } finally {
            lock.unlock();
        }
    }

    private boolean empujarCaja(Coordenada origen, Coordenada destino, String idHilo) {
        int filaDiff = destino.fila() - origen.fila();
        int colDiff = destino.col() - origen.col();

        int filaProyectada = destino.fila() + filaDiff;
        int colProyectada = destino.col() + colDiff;

        if (filaProyectada < 0 || filaProyectada > 5 || colProyectada < 0 || colProyectada > 5) {
            return false;
        }
    
        Coordenada proyeccionCaja = new Coordenada(filaProyectada, colProyectada);
        
        if (_tablero[proyeccionCaja.fila()][proyeccionCaja.col()].getTipo() != TipoEntidad.VACIA) {
            return false;
        }
    
        Entidad cajaEmpujada = _tablero[destino.fila()][destino.col()];
    
        if (filaProyectada == 5 && colProyectada == 5) {
            if (cajaEmpujada.getTipo() == TipoEntidad.CAJA_OBJETIVO) {
                this.extraidos++;
                this.cajasObjetivo--;
                registrarTraza(idHilo, "EXTRACCION EXITOSA ->", "(5,5)");
            } else {
                registrarTraza(idHilo, "EMPUJAR BLOQUEO ->", "(5,5)");
            }
            
            this.celdasOcupadas--;
            _tablero[destino.fila()][destino.col()] = _tablero[origen.fila()][origen.col()];
            _tablero[origen.fila()][origen.col()] = new CasillaVacia();
            noFull.signal();
            return true;
        }

        _tablero[proyeccionCaja.fila()][proyeccionCaja.col()] = cajaEmpujada;
        _tablero[destino.fila()][destino.col()] = _tablero[origen.fila()][origen.col()];
        _tablero[origen.fila()][origen.col()] = new CasillaVacia();
        
        String accion = (cajaEmpujada.getTipo() == TipoEntidad.CAJA_OBJETIVO) ? "EMPUJAR OBJETIVO" : "EMPUJAR BLOQUEO";
        registrarTraza(idHilo, accion + " " + destino.toString() + " ->", proyeccionCaja.toString());

        return true;
    }

    public Coordenada buscarObjetivo() throws InterruptedException {
        lock.lock();
        try {
            while (cajasObjetivo == 0) {
                noObjetivo.await(); 
            }
            
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    if (_tablero[i][j].getTipo() == TipoEntidad.CAJA_OBJETIVO) {
                        return new Coordenada(i, j);
                    }
                }
            }
            
            return null;
        } finally {
            lock.unlock();
        }
    }
}
