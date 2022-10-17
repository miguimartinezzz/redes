
package es.udc.redes.tutorial.copy;
import java.io.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Copy {

    public Copy(String origen, String destino) throws IOException {

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(origen);
            out = new FileOutputStream(destino);
            byte[] buffer = new byte[1024];
            int longitudEscritura;
            while ((longitudEscritura = in.read(buffer)) > 0) {
                out.write(buffer, 0, longitudEscritura);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if (in != null) {
                in.close();
            }
            if (out != null){
                out.close();
            }
        }
    }

    public static void main(String[] args) throws IOException{
        System.out.println(args[0]);
        System.out.println(args[1]);
        Copy test = new Copy(args[0], args[1]);
    }
}