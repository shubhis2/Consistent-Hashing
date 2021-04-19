import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;

public class NameServerReqHandler extends Thread {
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    NameServer ns;

    // Constructor for NameServer client
    public NameServerReqHandler(Socket s, DataInputStream dis, DataOutputStream dos, NameServer ns) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.ns = ns;
    }

    @Override
    public void run() {
        String received;
        while (true) {
            try {

                // receive the request from name server from client
                received = dis.readUTF();
                if (received.toLowerCase().equals("exit")) {
                    ns.exit(ns);
                    System.out.println("Client " + this.s + " exits");
                    System.out.println("Closing this " + ".");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }

                // write on output stream based on the
                // answer from the client

                switch (received.toLowerCase()) {
                case "enter":
                    ns.Enter(ns.bootStrapIp, ns.bootStrapPort, ns);
                    Thread.sleep(1000);
                    dos.writeUTF("successful entry");
                    dos.writeUTF("The key range is: " + (ns.predessorID + 1) + " - " + ns.id);
                    dos.writeUTF("Predessor ID: " + ns.predessorID + " Successor ID:" + ns.successorID);
                    ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
                    os.writeObject(ns.trail);
                    break;
                default:
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            // closing resources
            this.dis.close();
            this.dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
