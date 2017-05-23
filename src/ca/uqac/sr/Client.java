package ca.uqac.sr;

import ca.uqac.sr.utils.DoSomething;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by lowgr on 5/21/2017.
 */
public class Client {
    private Socket socket = null;
    private SendType sendType;
    private OutputStream outServerStream;
    private InputStream inServerStream;
    private File fileToSend;
    private Class objectClass;

    public Client(InetAddress inetAddress, int port) throws IOException {
        this.socket = new Socket(inetAddress, port);
        outServerStream = this.socket.getOutputStream();
        inServerStream = this.socket.getInputStream();

    }

    public void send() {
        try {

            String number1String = JOptionPane.showInputDialog("Please enter number 1.");
            String number2String = JOptionPane.showInputDialog("Please enter number 2.");

            int number1 = Integer.parseInt(number1String);
            int number2 = Integer.parseInt(number2String);


            String returnMessage;
            switch (sendType) {
                case OBJECT:
                    sendMessage(new Message(sendType, number1, number2, objectClass.getName()));
                    returnMessage = sendObject(number1, number2);
                    break;
                case BYTE:
                    sendMessage(new Message(sendType, number1, number2, fileToSend.length(), fileToSend.getName()));
                    returnMessage = sendFile();
                    break;
                case SOURCE:
                    sendMessage(new Message(sendType, number1, number2, fileToSend.length(), fileToSend.getName()));
                    returnMessage = sendFile();
                    break;
                default:
                    returnMessage = "No message has been returned.";
                    break;
            }

            JOptionPane.showMessageDialog(null, returnMessage);

        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void sendMessage(Message message) throws IOException {
        ObjectOutputStream outToServer = new ObjectOutputStream(outServerStream);
        outToServer.writeObject(message);
        outToServer.flush();
    }

    private String getReturnMessage() throws IOException, ClassNotFoundException {
        ObjectInputStream inFromServer = new ObjectInputStream(inServerStream);
        String rMessage = (String) inFromServer.readObject();
        inFromServer.close();
        return rMessage;
    }

    private String sendObject(int number1, int number2) {
        try {
            ObjectOutputStream outToServer = new ObjectOutputStream(outServerStream);
            Class[] cArg = new Class[2];
            cArg[0] = int.class;
            cArg[1] = int.class;
            DoSomething objectToSend = (DoSomething) objectClass.getDeclaredConstructor(cArg).newInstance(number1, number2);

            System.out.println("Sending object " + objectToSend.getClass().getName() + " to server...");
            outToServer.writeObject(objectToSend);
            System.out.println("Done.");

            String rMessage = getReturnMessage();
            outToServer.flush();
            return rMessage;
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            return "Error: " + ex.getMessage() + "\n" +
                    "The object has not been send.";
        }


    }

    private String sendFile() throws IOException, ClassNotFoundException {

        FileInputStream fis = new FileInputStream(fileToSend);
        System.out.println("Sending file " + fileToSend.getName() + " to server...");
        DataOutputStream dos = new DataOutputStream(outServerStream);
        byte[] buffer = new byte[4096];

        while (fis.read(buffer) > 0) {
            dos.write(buffer);
        }

        System.out.println("Done.");

        String rMessage = getReturnMessage();

        fis.close();
        dos.close();


        return rMessage;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Client client = null;
        try {
            // verification des arguments
            if (args.length != 4) {
                throw new IllegalArgumentException("Veuillez indiquer 4 arguments");
            } else {
                client = new Client(InetAddress.getByName(args[0]), new Integer(args[1]));
                switch (args[2]) {
                    case "-o":
                        client.sendType = SendType.OBJECT;
                        if (!(args[3].equals("Add") || args[3].equals("Divide") || args[3].equals("Multiply") || args[3].equals("Substract"))) {
                            throw new IllegalArgumentException("Argument incorrect " + args[3]);
                        } else {
                            client.objectClass = Class.forName("ca.uqac.sr.utils." + args[3]);
                        }

                        break;
                    case "-s":
                        client.sendType = SendType.SOURCE;
                        client.fileToSend = new File(args[3]);
                        if (!client.fileToSend.exists()) {
                            throw new FileNotFoundException();
                        }
                        break;
                    case "-b":
                        client.sendType = SendType.BYTE;
                        client.fileToSend = new File(args[3]);
                        if (!client.fileToSend.exists()) {
                            throw new FileNotFoundException();
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Argument incorrect " + args[3]);

                }
                client.send();
                client.socket.close();
            }
        } catch (Exception ex) {
            if(client != null){
                client.socket.close();
            }
            System.out.println(ex.getMessage());
            System.out.println("Usage:\n" +
                    "\tjava -jar Client.jar [hostname] [port number] -o [Add|Divide|Multiply|Substract]\n" +
                    "\tjava -jar Client.jar [hostname] [port number] -s [Source.java]\n" +
                    "\tjava -jar Client.jar [hostname] [port number] -b [Source.class]\n");
        }
    }
}
