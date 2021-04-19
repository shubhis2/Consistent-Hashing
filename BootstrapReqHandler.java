import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;

class BootstrapReqHandler extends Thread {

    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    BootstrapNameServer bs;

    // Constructor for bootstrap client
    public BootstrapReqHandler(Socket s, DataInputStream dis, DataOutputStream dos, BootstrapNameServer bs) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.bs = bs;
    }

    @Override
    public void run() {

        String received;
        while (true) {
            try {
                // 
                /*
                 * DataInputStream dis = new DataInputStream(s.getInputStream());
                 * DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                 */
                // receive the answer from client
                received = dis.readUTF();
                if (received.equals("exit")) {
                    System.out.println("Client " + this.s + " exits");
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

                case "delete":
                    bs.deleteKey(Integer.parseInt(split[1]), bs);
                    Thread.sleep(1000);
                    dos.writeUTF("The key: " + split[1] + " has been successfully deleted.");
                    os = new ObjectOutputStream(s.getOutputStream());
                    os.writeObject(bs.lookupTrail);
                    bs.lookupTrail.clear();
                    break;

                default:
                    break;
                }
            } catch (IOException e) {
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
