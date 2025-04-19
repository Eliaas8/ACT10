import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.function.Consumer;

class ValidadorContrasena extends Thread {
    private String contrasena;
    private Consumer<String> registrar;

    public ValidadorContrasena(String contrasena, Consumer<String> registrar) {
        this.contrasena = contrasena;
        this.registrar = registrar;
    }

    public void run() {
        StringBuilder resultado = new StringBuilder();
        resultado.append("Contraseña: ").append(contrasena).append(" | Resultado: ");

        boolean valida = true;

        if (contrasena.length() < 8) {
            resultado.append(" [Falla: Mínimo 8 caracteres]");
            valida = false;
        }

        if (!Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(contrasena).find()) {
            resultado.append(" [Falla: 1 carácter especial]");
            valida = false;
        }

        int mayusculas = contarCoincidencias(contrasena, "[A-Z]");
        if (mayusculas < 2) {
            resultado.append(" [Falla: 2 mayúsculas]");
            valida = false;
        }

        int minusculas = contarCoincidencias(contrasena, "[a-z]");
        if (minusculas < 3) {
            resultado.append(" [Falla: 3 minúsculas]");
            valida = false;
        }

        if (!Pattern.compile("[0-9]").matcher(contrasena).find()) {
            resultado.append(" [Falla: 1 número]");
            valida = false;
        }

        if (valida) {
            resultado.append(" VÁLIDA");
        } else {
            resultado.append(" INVÁLIDA");
        }

        System.out.println(resultado.toString());

        registrar.accept(resultado.toString());
    }

    private int contarCoincidencias(String texto, String regex) {
        int contador = 0;
        var matcher = Pattern.compile(regex).matcher(texto);
        while (matcher.find()) {
            contador++;
        }
        return contador;
    }
}

class ACT10 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("REQUISITOS PARA VALIDAR UNA CONTRASEÑA:");
        System.out.println("- Mínimo 8 caracteres");
        System.out.println("- Al menos 1 carácter especial (!@#$%^&*(),.?\":{}|<>)");
        System.out.println("- Al menos 2 letras mayúsculas");
        System.out.println("- Al menos 3 letras minúsculas");
        System.out.println("- Al menos 1 número\n");

        System.out.print("¿Cuántas contraseñas quieres validar?: ");
        int cantidad = scanner.nextInt();
        scanner.nextLine();

        String nombreArchivo = "registro_validaciones.txt";
        Consumer<String> registrar = texto -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo, true))) {
                writer.write(texto);
                writer.newLine();
            } catch (IOException e) {
                System.out.println("Error al escribir en archivo: " + e.getMessage());
            }
        };

        ValidadorContrasena[] hilos = new ValidadorContrasena[cantidad];

        for (int i = 0; i < cantidad; i++) {
            System.out.print("Ingresa la contraseña #" + (i + 1) + ": ");
            String contrasena = scanner.nextLine();
            hilos[i] = new ValidadorContrasena(contrasena, registrar);
        }

        System.out.println("\nIniciando validaciones concurrentes...\n");

        for (ValidadorContrasena hilo : hilos) {
            hilo.start();
        }

        for (ValidadorContrasena hilo : hilos) {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nValidaciones finalizadas.");
        System.out.println("Consulta el archivo '" + nombreArchivo + "' para ver el registro.");
    }
}