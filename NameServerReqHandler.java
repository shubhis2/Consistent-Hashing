import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import java.net.*;

public class NameServerReqHandler extends Thread {
	NameServer ns;

	public NameServerReqHandler(NameServer ns) {
		this.ns = ns;
	}

	@Override
	public void run() {
		Scanner scn = new Scanner(System.in);
		String received, tosend;
		while (true) {
			try {
				System.out.println("NS>");

				tosend = scn.nextLine();
				received = tosend;
				if (received.toLowerCase().equals("exit")) {
					System.out.println("NameServerReqHand:: in exit");
					ns.exit(ns);
					System.out.println("Closing this " + ".");
					System.out.println("Connection closed");
					break;
				}
				// write on output stream based on the
				// answer from the client
				switch (received.toLowerCase()) {
				case "enter":
					ns.Enter(ns.bootStrapIp, ns.bootStrapPort, ns);
					break;
				default:
					break;
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
