package ca.uqac.sr;

import ca.uqac.sr.utils.Add;
import ca.uqac.sr.utils.DoSomething;

import javax.swing.*;
import java.io.*;
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
                    sendMessage(new Message(sendType, number1, number2));
                    returnMessage = sendObject(number1, number2);
                    break;
                case BYTE:
                    sendMessage(new Message(sendType, number1, number2, fileToSend.length()));
                    returnMessage = sendFile();
                    break;
                case SOURCE:
                    sendMessage(new Message(sendType, number1, number2, fileToSend.length()));
                    returnMessage = sendFile();
                    break;
                default:
                    returnMessage = "No message has been returned.";
                    break;
            }

            JOptionPane.showMessageDialog(null, returnMessage);

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
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

    private String sendObject(int number1, int number2) throws IOException, ClassNotFoundException {
        ObjectOutputStream outToServer = new ObjectOutputStream(outServerStream);

        DoSomething objectToSend = new Add(number1, number2);

        System.out.println("Sending object " + objectToSend.getClass().getName() + " to server...");
        outToServer.writeObject(objectToSend);
        System.out.println("Done.");

        outToServer.flush();

        return getReturnMessage();

    }

    private String sendFile() throws IOException, ClassNotFoundException {

        byte[] byteArray = new byte[(int) fileToSend.length()];
        FileInputStream fis = new FileInputStream(fileToSend);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(byteArray, 0, byteArray.length);

        System.out.println("Sending file " + fileToSend.getName() + " to server...");
        outServerStream.write(byteArray, 0, byteArray.length);

        System.out.println("Done.");

        outServerStream.flush();

        return getReturnMessage();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {


        try {
            // verification des arguments
            if (args.length < 3 || args.length > 4) {
                throw new IllegalArgumentException("Veuillez indiquer 3 ou 4 arguments");
            } else {
                Client client = new Client(InetAddress.getByName(args[0]), new Integer(args[1]));
                switch (args[2]) {
                    case "-o":
                        client.sendType = SendType.OBJECT;
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
            System.out.println(ex.getMessage());
            System.out.println("Usage:\n" +
                    "\tjava -jar Client.jar [hostname] [port number] -o\n" +
                    "\tjava -jar Client.jar [hostname] [port number] -s [Source.java]\n" +
                    "\tjava -jar Client.jar [hostname] [port number] -b [Source.class]\n");
        }
    }
}
