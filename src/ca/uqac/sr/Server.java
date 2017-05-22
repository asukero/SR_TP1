package ca.uqac.sr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Created by lowgr on 5/21/2017.
 */
public class Server {
    private ServerSocket listener = null;

    public Server(int port, int backlog, InetAddress inetAddress) throws IOException {
        this.listener = new ServerSocket(9090, 1, inetAddress);
    }

    public void startServer() throws IOException {
        try {
            while (true) {
                try (Socket socket = this.listener.accept()) {
                    ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

                    Message message = (Message) is.readObject();
                    message.computeResult();

                    os.writeObject(message);
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

    public static void main(String[] args) throws IOException {
        Server server = new Server(9999, 1, InetAddress.getByName("localhost"));
        server.startServer();
    }
}
