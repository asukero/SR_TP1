package ca.uqac.sr;

import ca.uqac.sr.utils.DoSomething;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by lowgr on 5/21/2017.
 */
public class Server {
    private ServerSocket listener = null;
    private ClassLoader classLoader;

    public Server(int port) throws IOException {
        this.listener = new ServerSocket(port);
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
                    DoSomething object = null;

                    System.out.println("Message received from client:\n" +
                                        "\tmessage type: " + message.sendType.toString() + "\n" +
                                        "\tcompute: " + message.fileName + "\n" +
                                        "\tnumber1: " + message.number1 + "\n" +
                                        "\tnumber2: " + message.number2);


                    switch (message.sendType) {
                        case OBJECT:
                            object = readObject(socket);
                            break;
                        case SOURCE:
                            File fileReceived = readFile(socket, message);
                            compileFile(fileReceived);
                            classLoader = new URLClassLoader(new URL[]{new File("./classes").toURI().toURL()});
                            object = loadFile("ca.uqac.sr.utils." + fileReceived.getName().substring(0, fileReceived.getName().lastIndexOf(".")), message);
                            break;
                        case BYTE:
                            File fileReceivede = readFile(socket, message);
                            classLoader = new URLClassLoader(new URL[]{fileReceivede.toURI().toURL()});
                            object = loadFile("ca.uqac.sr.utils." + fileReceivede.getName().substring(0, fileReceivede.getName().lastIndexOf(".")), message);
                            break;
                        default:
                            break;

                    }
                    if (object != null) {
                        object.compute();
                        rMessage = "Compute sucess ! The result is " + object.getResult() + "\n";

                    } else {
                        rMessage = "Error during compute\n";
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

    private File readFile(Socket socket, Message message) throws IOException, ClassNotFoundException {

        DataInputStream dis = new DataInputStream(socket.getInputStream());
        FileOutputStream fos = new FileOutputStream(message.fileName);
        byte[] buffer = new byte[4096];

        int filesize = (int) message.fileSize; // Send file size in separate msg
        int read = 0;
        int totalRead = 0;
        int remaining = filesize;
        while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            System.out.println("read " + totalRead + " bytes.");
            fos.write(buffer, 0, read);
        }

        File fileReceived = new File(message.fileName);

        //dis.close();

        fos.close();
        return fileReceived;
    }

    public void compileFile(File fileReceived) {
        //Récupération du compilateur Java
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);


        //Configuration du classpath utilisé par le compilateur
        List<String> optionList = new ArrayList<>();
        optionList.add("-g");
        optionList.add("-d");
        optionList.add("././classes");

        Iterable<? extends JavaFileObject> compilationUnit
                = fileManager.getJavaFileObjects(fileReceived);
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                optionList,
                null,
                compilationUnit);

        //Compilation des fichiers java...
        if (task.call()) {
            System.out.println("File " + fileReceived.getPath() + " compiled successfully.");
        } else {
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                System.out.println("Erreur à la ligne" + diagnostic.getLineNumber() + " de " + diagnostic.getSource().toUri());
            }
        }

        try {
            fileManager.close();
        } catch (IOException e) {
            System.out.println("Erreur: Les fichiers sources ne se sont pas fermés correctement");
        }
    }

    private DoSomething loadFile(String filePath, Message message) {
        try {
            //Chargement de la classe à l'aide du ClassLoader
            Class classe = classLoader.loadClass(filePath);
            System.out.println("Class " + classe.getName() + " was loaded successfully.");
            Class[] cArg = new Class[2];
            cArg[0] = int.class;
            cArg[1] = int.class;
            DoSomething object = (DoSomething) classe.getDeclaredConstructor(cArg).newInstance(message.number1, message.number2);
            return object;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            System.out.println("Erreur: La classe " + filePath + " n'a pas été trouvée.");
            return null;
        }

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

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        System.out.println("SERVEUR: interruption recue, fermeture du serveur...");
                        try {
                            server.listener.close();
                        } catch (IOException e) {
                            System.err.println("Erreur lors de la fermeture du socket");
                        }
                    }
                });


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
