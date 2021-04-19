import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;

class ClientReqHandler extends Thread {

    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    BootstrapNameServer bs;
    NameServer ns;

    // Constructor for bootstrap client
    public ClientReqHandler(Socket s, DataInputStream dis, DataOutputStream dos, BootstrapNameServer bs) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.bs = bs;
    }

    // Constructor for NameServer client
    public ClientReqHandler(Socket s, DataInputStream dis, DataOutputStream dos, NameServer ns) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.ns = ns;
    }

    @Override
    public void run() {
        if (bs == null) {
            try {
                dos.writeUTF("Name server is connected");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                dos.writeUTF("Bootstrap server is connected");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String received;
        while (true) {
            try {
                // Ask user what he wants
                /*
                 * DataInputStream dis = new DataInputStream(s.getInputStream());
                 * DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                 */
                // receive the answer from client
                received = dis.readUTF();
                if (received.equals("exit")) {
                    ns.exit(ns);
                    System.out.println("Client " + this.s + " sends exit...");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }

                // write on output stream based on the
                // answer from the client
                String[] split = received.split(" ");
                String command = split[0];
                switch (command.toLowerCase()) {
                case "lookup":
                    bs.lookupKey(Integer.parseInt(split[1]), bs);
                    Thread.sleep(1000);
                    dos.writeUTF("The response for the key: " + split[1] + " is " + bs.lookupKeyResponse);
                    ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
                    os.writeObject(bs.lookupTrail);
                    bs.lookupTrail.clear();
                    bs.lookupKeyResponse = null;
                    break;

                case "insert":
                    bs.insertKeyValue(Integer.parseInt(split[1]), split[2], bs);
                    Thread.sleep(1000);
                    dos.writeUTF("The key: " + split[1] + " value: " + split[2] + " has been successfully entered");
                    os = new ObjectOutputStream(s.getOutputStream());
                    os.writeObject(bs.lookupTrail);
                    bs.lookupTrail.clear();
                    break;

                case "enter":
                    ns.Enter(ns.bootStrapIp, ns.bootStrapPort, ns);
                    Thread.sleep(1000);
                    dos.writeUTF("successful entry");
                    dos.writeUTF("The key range is: " + (ns.predessorID + 1) + " - " + ns.id);
                    dos.writeUTF("Predessor ID: " + ns.predessorID + " Successor ID:" + ns.successorID);
                    os = new ObjectOutputStream(s.getOutputStream());
                    os.writeObject(ns.trail);
                    break;
                default:
                    break;
                }
            } catch (IOException | ClassNotFoundException e) {
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