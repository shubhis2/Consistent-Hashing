import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;

public class Client {
	public static void main(String[] args) throws IOException {
		try {
			Scanner scn = new Scanner(System.in);

			// getting localhost ip
			InetAddress ip = InetAddress.getByName("localhost");

			Socket s = new Socket(ip, 3768);

			// obtaining input and out streams
			DataInputStream dis = new DataInputStream(s.getInputStream());
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());

			dos.writeUTF("client has made a connection with the server");
			// the following loop performs the exchange of
			// information between client and client handler
			while (true) {
				// obtaining input and out streams
				dis = new DataInputStream(s.getInputStream());
				dos = new DataOutputStream(s.getOutputStream());
				System.out.print("CHNS>");
				String tosend = scn.nextLine();
				dos.writeUTF(tosend);

				// If client sends exit,close this connection
				// and then break from the while loop

				String[] split = tosend.split(" ");
				if (split[0].equals("exit")) {
					System.out.println("Closing this connection : " + s);
					s.close();
					System.out.println("System exited gracefully");
					System.out.println("Connection closed");
					break;
				} else if (split[0].equals("enter")) {
					System.out.println(dis.readUTF());
					System.out.println(dis.readUTF());
					System.out.println(dis.readUTF());
					ObjectInputStream is = new ObjectInputStream(s.getInputStream());
					System.out.println("Server(s) contacted are:");
					@SuppressWarnings("unchecked")
					ArrayList<Integer> trail = (ArrayList<Integer>) is.readObject();
					for (int i = 0; i < trail.size(); i++) {
						System.out.println(trail.get(i));
					}
				} else if (split[0].equals("lookup")) {
					System.out.println(dis.readUTF());
					ObjectInputStream is = new ObjectInputStream(s.getInputStream());
					@SuppressWarnings("unchecked")
					ArrayList<Integer> trail = (ArrayList<Integer>) is.readObject();
					System.out.println("request is fetched from server with ID: " + trail.get(trail.size() - 1));
					System.out.println("Servers contacted are:");
					for (int i = 0; i < trail.size(); i++) {
						System.out.println(trail.get(i));
					}
				} else if (split[0].equals("insert")) {
					System.out.println(dis.readUTF());
					ObjectInputStream is = new ObjectInputStream(s.getInputStream());
					@SuppressWarnings("unchecked")
					ArrayList<Integer> trail = (ArrayList<Integer>) is.readObject();
					System.out.println("request is fetched from server with ID: " + trail.get(trail.size() - 1));
					System.out.println("Server contacted are:");
					for (int i = 0; i < trail.size(); i++) {
						System.out.println(trail.get(i));
					}
				} else if (split[0].equals("delete")) {
					System.out.println(dis.readUTF());
					ObjectInputStream is = new ObjectInputStream(s.getInputStream());
					@SuppressWarnings("unchecked")
					ArrayList<Integer> trail = (ArrayList<Integer>) is.readObject();
					System.out.println("request is fetched from server with ID: " + trail.get(trail.size() - 1));
					System.out.println("Server contacted are:");
					for (int i = 0; i < trail.size(); i++) {
						System.out.println(trail.get(i));
					}
				}

			}

			// closing resources
			scn.close();
			dis.close();
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
