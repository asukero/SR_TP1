package ca.uqac.sr;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by lowgr on 5/21/2017.
 */
public class Client {
    private Socket socket = null;

    public Client(InetAddress inetAddress) throws IOException {
        this.socket = new Socket(inetAddress, 9090);
    }

    public void send() throws IOException, ClassNotFoundException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());

        String number1String = JOptionPane.showInputDialog("Please enter number 1.");
        String number2String = JOptionPane.showInputDialog("Please enter number 2.");

        int number1 = Integer.parseInt(number1String);
        int number2 = Integer.parseInt(number2String);

        Message message = new Message(new DoSomething(), number1, number2);
        objectOutputStream.writeObject(message);

        ObjectInputStream objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        message = (Message) objectInputStream.readObject();

        JOptionPane.showMessageDialog(null, "The result is: " + message.result);

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException{
        Client client = new Client(InetAddress.getByName("localhost"));
        client.send();
    }
}
