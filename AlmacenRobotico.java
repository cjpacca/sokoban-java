import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AlmacenRobotico {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Error: Debe proporcionar el nombre del archivo TXT como argumento.");
            return;
        }

        String archivoEntrada = args[0];
        
        int cajasObj = 0;
        int cajasBloq = 0;
        int Robots = 0;
        int bateriaR = 0;
        int Productores = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(archivoEntrada))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Separamos la línea por la coma
                String[] partes = linea.split(",");
                String clave = partes[0].trim();

                switch (clave) {
                    case "Cajas_Objetivo_Iniciales":
                        cajasObj = Integer.parseInt(partes[1].trim());
                        break;
                    case "Cajas_Bloqueo_Iniciales":
                        cajasBloq = Integer.parseInt(partes[1].trim());
                        break;
                    case "Robots":
                        Robots = Integer.parseInt(partes[1].trim());
                        bateriaR = Integer.parseInt(partes[2].trim());
                        break;
                    case "Productores":
                        Productores = Integer.parseInt(partes[1].trim());
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo de configuración: " + e.getMessage());
            return;
        }

        // Validación de Capacidad Inicial
        int totalEntidadesIniciales = cajasObj + cajasBloq + Robots;
        
        if (totalEntidadesIniciales > 36) {
            System.out.println("ERROR DE CONFIGURACIÓN INICIAL: El número total de entidades (" 
                                + totalEntidadesIniciales + ") supera la capacidad de la matriz 6x6 (36).");
            return; 
        }

        //prints temporales
        System.out.println("Configuración cargada exitosamente.");
        System.out.println("Total de entidades iniciales a posicionar: " + totalEntidadesIniciales);

    }
}