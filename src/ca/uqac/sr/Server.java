package ca.uqac.sr;

import ca.uqac.sr.utils.DoSomething;
import sun.misc.IOUtils;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.net.*;


/**
 * Created by lowgr on 5/21/2017.
 */
public class Server {
    private ServerSocket listener = null;

    public Server(int port) throws IOException {
        this.listener = new ServerSocket(9090);
    }

    public void startServer() throws IOException {
        try {
            System.out.println("Server is running, waiting for client...");
            while (true) {
                try {
                    Socket socket = this.listener.accept();
                    ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

                    Message message = (Message) is.readObject();

                    String rMessage = null;
                    switch (message.sendType) {
                        case OBJECT:
                            DoSomething object = readObject(socket);
                            object.compute();
                            rMessage = "Compute sucess, result is " + object.getResult();
                            break;
                        case SOURCE:
                            File fileReceived = readFile(socket, message.fileSize);
                            break;
                        case BYTE:
                            File fileReceivede = readFile(socket, message.fileSize);
                            break;
                        default:
                            break;

                    }
                    System.out.println(rMessage);
                    os.writeObject(rMessage);
                    os.flush();
                    socket.close();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            listener.close();
        }
    }

    private DoSomething readObject(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
        DoSomething object = (DoSomething) is.readObject();
        return object;
    }

    private File readFile(Socket socket, long fileSize) throws IOException, ClassNotFoundException {

        DataInputStream dis = new DataInputStream(socket.getInputStream());
        FileOutputStream fos = new FileOutputStream("temp_file");
        byte[] buffer = new byte[4096];

        int filesize = (int)fileSize; // Send file size in separate msg
        int read = 0;
        int totalRead = 0;
        int remaining = filesize;
        while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            System.out.println("read " + totalRead + " bytes.");
            fos.write(buffer, 0, read);
        }

        File fileReceived = new File("temp_file");

        dis.close();
        System.out.println("File temp_file"
                + " downloaded (" + fileSize + " bytes)");


        fos.close();
        return fileReceived;
    }

    public static void main(String[] args) throws IOException {


        try {
            //Vérification du nombre d'arguments
            if (args.length != 1) {
                throw new IllegalArgumentException("Veuillez indiquer 1 argument");
            } else {
                final Server server = new Server(new Integer(args[0]));


                /** Création d'un thread qui vérifie si un signal d'interruption (par exemple Ctrl-C) a été soumis au programme
                 * Dans ce cas, fermeture du writer et du socket.
                 */
//                Runtime.getRuntime().addShutdownHook(new Thread() {
//                    @Override
//                    public void run() {
//                        System.out.println("SERVEUR: interruption recue, fermeture du serveur...");
//                        try {
//                            server.listener.close();
//                        } catch (IOException e) {
//                            System.err.println("Erreur lors de la fermeture du socket");
//                        }
//                    }
//                });

                server.startServer();

            }
        } catch (Exception ex) {
            System.err.println(ex.toString());
            System.out.println("Usage:\n" +
                    "\tjava -jar ApplicationServer.jar [port number] [source folder] [class folder] [output filename]\n" +
                    "\tex: java -jar ApplicationServer.jar 4242 ./sources/ ./classes sortieServeur.txt");
        }
    }
}
