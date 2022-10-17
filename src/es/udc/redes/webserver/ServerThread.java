
package es.udc.redes.webserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class ServerThread extends Thread {

    private Date date;
    private int code;
    private File file = null;

    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");

    private final Socket socket;
    private final String DIRECTORY;
    private final String DIRECTORY_INDEX;
    private final Boolean ALLOW;

    public ServerThread(Socket s, String d, String i, Boolean a) {
        this.socket = s;
        this.DIRECTORY = d;
        this.DIRECTORY_INDEX = i;
        this.ALLOW = a;
    }


    @Override
    public void run() {


        BufferedReader reader = null;
        PrintWriter writer = null;
        BufferedOutputStream dataOut = null;
        String fileRequest = null;
        String http=null;
        String method=null;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
            dataOut = new BufferedOutputStream(socket.getOutputStream());
            String line = reader.readLine();
            String header = null;
            String aux = reader.readLine();
            if (aux != null) {
                while (!aux.equals("")) {
                    if (aux.contains("If-Modified-Since")) {
                        header = aux;
                        break;
                    } else {
                        aux = reader.readLine();
                    }
                }
            }
            if(line!=null){
                String[] params=line.split(" ");
                method=params[0];
                fileRequest=params[1];

                if (!method.equals("GET") && !method.equals("HEAD")) {
                    code=400;
                    printHeader(writer, dataOut, "", fileRequest);
                }
                if(method.equals("HEAD")){
                    printHeader(writer, dataOut, method, fileRequest);
                }
                if (method.equals("GET")) {
                    if (header != null && header.contains("If-Modified-Since")) {
                        File reload = new File(DIRECTORY + fileRequest);
                        String stringIfMod = header.substring(19);
                        Date ifModDate = parseDate(stringIfMod);
                        Date lastDate = parseDate(formatDate(reload.lastModified()));
                        if (lastDate.before(ifModDate) || ifModDate.equals(lastDate)) {
                            code = 304;
                            printHeader(writer, dataOut, "", fileRequest);
                        } else {
                            printHeader(writer, dataOut, method, fileRequest);
                        }
                    } else {
                        printHeader(writer, dataOut, method, fileRequest);
                    }
                }
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (dataOut != null) {
                    dataOut.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void printHeader(PrintWriter printer, OutputStream dataOut, String metodo, String content) throws IOException {
        File requested = new File(DIRECTORY + content);

        if(code == 400 || code == 304){
            if(code == 400) {
                file = new File(DIRECTORY + "/error400.html");
            }
            if(code == 304){
                file = new File(DIRECTORY + "/error304.html");
            }
        }
        else {
            if (requested.exists()) {
                code = 200;
                file = new File(DIRECTORY + content);
            }
            else{
                code =404;
                file = new File(DIRECTORY + "/error404.html");
            }
        }
        date = new Date();
        int length = (int) file.length();
        byte[] data = readerData(file, length);
        printer.println("HTTP/1.0 " + code);
        printer.println("Date: " + date);
        printer.println("Server: WebServer_715");
        printer.println("Last-Modified: " + formatDate(file.lastModified()));
        printer.println("Content-Length: " + file.length());
        printer.println("Content-Type: " + getType(file));
        printer.println("");
        printer.flush();

        if (code == 200) {
            switch (metodo) {
                case "GET":
                    dataOut.write(data, 0, length);
                    break;
                case "HEAD":
                    break;
                default:
                    break;
            }
        } else {
            if(code == 404){
                if(!metodo.equals("HEAD")){
                    dataOut.write(data, 0, length);
                }
            }
            else{
                dataOut.write(data,0,length);
            }
        }
        dataOut.flush();

        activity(metodo, content, length);
    }

    private void activity(String metodo, String content, int length) throws IOException {
        File access = new File(DIRECTORY + "/access.log");
        File errors = new File(DIRECTORY + "/errors.log");
        PrintWriter registrar;

        try {
            switch (code) {
                case 200:
                case 304:
                    registrar = new PrintWriter(new FileOutputStream(access, true), true);

                    registrar.println("------ A C C E S S ------");
                    registrar.println("Petición de acceso: " + metodo + " " + content);
                    registrar.println("IP cliente: " + socket.getInetAddress().toString());
                    registrar.println("Fecha y hora de petición: " + date);
                    registrar.println("Código de estado: " + code);
                    registrar.println("Tamaño: " + length + " byte(s)");
                    registrar.println("-------------------------");
                    registrar.println("");
                    registrar.flush();
                    break;

                case 400:
                case 404:
                    registrar = new PrintWriter(new FileOutputStream(errors, true), true);

                    registrar.println("------- E R R O R -------");
                    registrar.println("Petición errónea: " + metodo + " " + content);
                    registrar.println("IP cliente: " + socket.getInetAddress().toString());
                    registrar.println("Fecha y hora de error: " + date);
                    registrar.println("Código de error: " + code);
                    registrar.println("-------------------------");
                    registrar.println("");
                    break;

            }
        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe);
        }
    }

    private String formatDate(long seconds) {
        return formatoFecha.format(seconds);
    }

    private Date parseDate(String fecha) {
        Date parsedDate = null;
        try {
            parsedDate = formatoFecha.parse(fecha);
        } catch (ParseException ex) {
            System.out.println(ex);
        }
        return parsedDate;
    }

    private byte[] readerData(File file, int lentgh) throws IOException {
        FileInputStream input = null;
        byte[] data = new byte[lentgh];

        try {
            input = new FileInputStream(file);
            input.read(data);
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return data;
    }

    private String getType(File fileRequest) {
        String requested = fileRequest.getName();
        String type = "application/octet-stream";

        if (requested.endsWith(".htm") || requested.endsWith(".html")) {
            type = "text/html";
        }
        if (requested.endsWith(".log") || requested.endsWith(".txt")) {
            type = "text/plain";
        }
        if (requested.endsWith(".gif")) {
            type = "image/gif";
        }
        if (requested.endsWith(".png")) {
            type = "image/png";
        }
        return type;
    }

}