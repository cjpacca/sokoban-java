import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tablero {

    private final Entidad[][] _tablero = new Entidad[6][6];
    private int cajasObjetivo, extraidos, saturaciones, agotados, tick, celdasOcupadas;

    public Tablero() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                _tablero[i][j] = new CasillaVacia();
            }
        }
    }

    private void registrarTraza(String idHilo, String accion, String detalles) {
        this.tick++;
        String tickFormat = String.format("%02d", this.tick);
        String espacio = detalles.isEmpty() ? "" : " ";
        System.out.println(
            "[Tick-" +
                tickFormat +
                "] [" +
                idHilo +
                "] " +
                accion +
                espacio +
                detalles
        );
    }

    public synchronized void insertarCaja(Entidad nuevaCaja, String idHilo)
        throws InterruptedException {
        while (celdasOcupadas == 36) {
            saturaciones++;
            registrarTraza(idHilo, "ESPERA SATURACION", "");
            wait();
        }

        List<Coordenada> disponibles = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (_tablero[i][j].getTipo() == TipoEntidad.VACIA) {
                    disponibles.add(new Coordenada(i, j));
                }
            }
        }

        int posicionCoordenada = random.nextInt(disponibles.size());
        Coordenada coordenadaAInsertar = disponibles.get(posicionCoordenada);

        _tablero[coordenadaAInsertar.fila()][coordenadaAInsertar.col()] =  nuevaCaja;
        celdasOcupadas++;

        String tipoAccion = nuevaCaja.getTipo() == TipoEntidad.CAJA_OBJETIVO ? "INSERTAR OBJETIVO ->": "INSERTAR BLOQUEO ->";
        registrarTraza(idHilo, tipoAccion, coordenadaAInsertar.toString());

        if (nuevaCaja.getTipo() == TipoEntidad.CAJA_OBJETIVO) {
            this.cajasObjetivo++;
            notifyAll();
        }
    }

    public synchronized boolean solicitarMovimiento(Coordenada origen, Coordenada destino, String idHilo) {
        if (cajasObjetivo == 0) {
            return false;
        }

        TipoEntidad tipoEntidadDestino = _tablero[destino.fila()][destino.col()].getTipo();

        if (tipoEntidadDestino == TipoEntidad.VACIA) {
            if (destino.fila() == 5 && destino.col() == 5) {
                return false;
            }

            _tablero[destino.fila()][destino.col()] = _tablero[origen.fila()][origen.col()];
            _tablero[origen.fila()][origen.col()] = new CasillaVacia();
            registrarTraza(idHilo, "MOVIMIENTO ->", destino.toString());
            return true;
        } else if (tipoEntidadDestino == TipoEntidad.ROBOT) {
            return false;
        } else {
            return empujarCaja(origen, destino, idHilo);
        }
    }

    private boolean empujarCaja(Coordenada origen, Coordenada destino, String idHilo) {
        int filaDiff = destino.fila() - origen.fila();
        int colDiff = destino.col() - origen.col();

        int filaProyectada = destino.fila() + filaDiff;
        int colProyectada = destino.col() + colDiff;

        if (
            filaProyectada < 0 ||
            filaProyectada > 5 ||
            colProyectada < 0 ||
            colProyectada > 5
        ) {
            return false;
        }

        Coordenada proyeccionCaja = new Coordenada(
            filaProyectada,
            colProyectada
        );

        if (
            _tablero[proyeccionCaja.fila()][proyeccionCaja.col()].getTipo() != TipoEntidad.VACIA) {
            return false;
        }

        Entidad cajaEmpujada = _tablero[destino.fila()][destino.col()];

        if (filaProyectada == 5 && colProyectada == 5) {
            if (cajaEmpujada.getTipo() == TipoEntidad.CAJA_OBJETIVO) {
                this.extraidos++;
                this.cajasObjetivo--;
                registrarTraza(idHilo, "EXTRACCION EXITOSA ->", "(5,5)");
            } else {
                registrarTraza(idHilo, "EMPUJAR_BLOQUEO ->", "(5,5)");
            }

            this.celdasOcupadas--;
            _tablero[destino.fila()][destino.col()] = _tablero[origen.fila()][
                origen.col()
            ];
            _tablero[origen.fila()][origen.col()] = new CasillaVacia();

            notifyAll();
            return true;
        }

        _tablero[proyeccionCaja.fila()][proyeccionCaja.col()] = cajaEmpujada;
        _tablero[destino.fila()][destino.col()] = _tablero[origen.fila()][origen.col()];
        _tablero[origen.fila()][origen.col()] = new CasillaVacia();

        String accion =
            cajaEmpujada.getTipo() == TipoEntidad.CAJA_OBJETIVO
                ? "EMPUJAR OBJETIVO"
                : "EMPUJAR_BLOQUEO";
        registrarTraza(
            idHilo,
            accion + " " + destino.toString() + " ->",
            proyeccionCaja.toString()
        );

        return true;
    }

    public synchronized Coordenada buscarObjetivo()
        throws InterruptedException {
        while (cajasObjetivo == 0) {
            wait();
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (_tablero[i][j].getTipo() == TipoEntidad.CAJA_OBJETIVO) {
                    return new Coordenada(i, j);
                }
            }
        }

        return null;
    }

    public synchronized void registrarRobotAgotado(String idHilo) {
        this.agotados++;
        registrarTraza(idHilo, "BATERIA AGOTADA", "El robot se apaga");
    }

    public void imprimirReporteFinal() {
        System.out.println("\n=== REPORTE FINAL DE SIMULACION ===");
        System.out.println("Total de Ticks procesados: " + this.tick);
        System.out.println("Cajas objetivo extraídas: " + this.extraidos);
        System.out.println("Robots sin batería (Agotados): " + this.agotados);
        System.out.println("Saturaciones del almacén: " + this.saturaciones);
        System.out.println(
            "Cajas objetivo restantes en tablero: " + this.cajasObjetivo
        );
        System.out.println("===================================");
    }

    public void imprimirTablero() {
        System.out.println("\n=== ESTADO FINAL DEL TABLERO ===");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                System.out.print(_tablero[i][j].getCaracter() + " ");
            }
            System.out.println();
        }
    }
}
