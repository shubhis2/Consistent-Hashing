import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class BootstrapNameServer {
	int id;
	String serverIP;
	int serverPort;
	int predecessorID;
	String predecessorIp;
	int predecessorPort;
	int successorID;
	String successorIp;
	int successorPort;
	String slash;
	String OS;
	String lookupKeyResponse;
	ArrayList<Integer> lookupTrail = new ArrayList<Integer>();
	SortedMap<Integer, String> data = new TreeMap<Integer, String>();

	public BootstrapNameServer(String fileName) throws UnknownHostException {
		// check this
		OS = System.getProperty("os.name").toLowerCase();
		if (OS.indexOf("win") >= 0)
			slash = "\\";
		else if ((OS.indexOf("mac") >= 0 || OS.indexOf("nux") >= 0))
			slash = "/";

		File file = new File(System.getProperty("user.dir") + slash + fileName);
		try {
			Scanner sc = new Scanner(file);
			this.id = Integer.parseInt(sc.nextLine());
			this.serverPort = Integer.parseInt(sc.nextLine());

			while (sc.hasNextLine()) {
				String[] line = sc.nextLine().split(" ");
				this.data.put(Integer.parseInt(line[0]), line[1]);
			}
			sc.close();

			for (Map.Entry<Integer, String> entry : data.entrySet()) {
				System.out.println(entry.getKey() + " " + entry.getValue());
			}
			InetAddress inetAddress = InetAddress.getLocalHost();
			this.serverIP = inetAddress.getHostAddress();
			this.successorID = 0;
			this.successorIp = null;
			this.successorPort = -1;
			this.predecessorID = 0;
			this.predecessorPort = -1;
			this.predecessorIp = null;
			this.lookupKeyResponse = null;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void lookupKey(Integer key, BootstrapNameServer bs) {
		// check if the key belongs here
		bs.lookupTrail.add(bs.id);
		if (key > bs.predecessorID) {
			// key is at bootstrap
			String value = bs.data.get(key);
			if (value != null) {
				bs.lookupKeyResponse = value;
			} else {
				bs.lookupKeyResponse = "key not found";
			}
		}
		// forward the request to successor
		else {
			bs.forwardLookupRequest(key, bs.successorID, bs.successorIp, bs.successorPort, bs.lookupTrail);
		}
	}

	public void insertKeyValue(Integer key, String Value, BootstrapNameServer bs) {
		// check if the key is to be inserted here
		bs.lookupTrail.add(bs.id);
		if (key < 1024 && key > 0) {
			if (key > bs.predecessorID) {
				bs.data.put(key, Value);
			} else {
				bs.forwardInsertRequest(key, Value, bs.successorID, bs.successorIp, successorPort, lookupTrail);
			}
		}

	}

	public void deleteKey(Integer key, BootstrapNameServer bs) {
		bs.lookupTrail.add(bs.id);
		// check if the key is deleted here
		if (key > bs.predecessorID) {
			bs.data.remove(key);
		} else {
			bs.forwardDeleteRequest(key, bs.successorID, bs.successorIp, bs.successorPort, bs.lookupTrail);
		}
	}

	public void forwardDeleteRequest(int key, int successorID, String successorIp, int successorPort,
			ArrayList<Integer> lookupTrail) {
		Socket s;
		try {
			s = new Socket(successorIp, successorPort);
			System.out.println("delete request has been forwarded to ID: " + successorID);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.writeUTF("Boostrap server has forwarded delete request");
			dos.writeUTF("Forwarding delete request");
			dos.writeInt(key);
			ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
			os.writeObject(lookupTrail);
			s.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void forwardInsertRequest(int key, String Value, int successorID, String successorIp, int successorPort,
			ArrayList<Integer> lookupTrail) {
		Socket s;
		try {
			s = new Socket(successorIp, successorPort);
			System.out.println(" insert request has been forwarded to ID: " + successorID);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.writeUTF("Boostrap server has forwarded insert request");
			dos.writeUTF("Forwarding insert request");
			dos.writeInt(key);
			dos.writeUTF(Value);
			ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
			os.writeObject(lookupTrail);
			s.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void forwardLookupRequest(int key, int successorID, String successorIp, int successorPort,
			ArrayList<Integer> lookupTrail) {
		Socket s;
		try {
			s = new Socket(successorIp, successorPort);
			System.out.println(" look up request has been forwarded to ID: " + successorID);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.writeUTF("Boostrap server has forwarded lookup request");
			dos.writeUTF("Forwarding lookup request");
			dos.writeInt(key);
			ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
			os.writeObject(lookupTrail);
			s.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void forwardEntryRequest(int requestID, String requestIp, int requestPort, int successorID,
			String successorIp, int successorPort, ArrayList<Integer> trail) {
		Socket s;
		try {
			s = new Socket(successorIp, successorPort);
			System.out.println("request has been forwarded to " + successorID);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.writeUTF("Name server has forwarded a request id:" + requestID);
			dos.writeUTF("Forwarding entry request");
			dos.writeInt(requestID);
			dos.writeUTF(requestIp);
			dos.writeInt(requestPort);
			trail.add(id);
			ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
			os.writeObject(trail);
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String args[]) throws IOException {
		if (args.length != 1) {
			System.out.println("Enter config file as cmd line parameter");
			return;
		}
		BootstrapNameServer bs = new BootstrapNameServer(args[0]);
		ServerSocket ss = new ServerSocket(bs.serverPort);
		// server is listening on port --- for other server
		// running infinite loop for getting
		// client request
		// server request
		System.out.println("\nServer id: " + bs.id + "\nListening on port: " + bs.serverPort);
		while (true) {
			Socket s = null;
			try {
				// socket object to receive incoming client requests
				s = ss.accept();
				// obtaining input and out streams
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				String received = dis.readUTF();
				System.out.println(received);
				if (received.contains("client")) {
					// create a new thread object
					System.out.println("Assigning new thread for this client");
					// Invoking the start() method
					Thread c = new BootstrapReqHandler(s, dis, dos, bs);
					c.start();
				} else {
					String message = dis.readUTF();

					switch (message) {
					case "enter":

						/*
						 * First new NameServer
						 */
						if (bs.successorID == 0 && bs.predecessorID == 0) {
							// System.out.println("bsNS: enter: suc: 0 pred: 0");
							dos.writeInt(0);
							int requestID = dis.readInt();
							String requestIp = dis.readUTF();
							int requestPort = dis.readInt();
							// indicating first name server into the system
							// setting up the successors and predecessors
							bs.successorID = requestID;
							bs.successorIp = requestIp;
							bs.successorPort = requestPort;
							bs.predecessorID = requestID;
							bs.predecessorIp = requestIp;
							bs.predecessorPort = requestPort;
							dos.writeInt(bs.id);
							dos.writeUTF(bs.serverIP);
							dos.writeInt(bs.serverPort);
							// create a submap for the request
							SortedMap<Integer, String> subMap = new TreeMap<Integer, String>();
							ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
							for (Map.Entry<Integer, String> entry : bs.data.entrySet()) {
								int currentID = entry.getKey();
								String currentValue = entry.getValue();
								if (currentID < requestID) {
									subMap.put(currentID, currentValue);
								}
							}
							os.writeObject(subMap);

							for (Map.Entry<Integer, String> entry : subMap.entrySet()) {
								int currentID = entry.getKey();
								String currentValue = entry.getValue();
								if (bs.data.containsKey(currentID)) {
									bs.data.remove(currentID, currentValue);
								}

							}
						} else {
							dos.writeInt(1);
							int requestID = dis.readInt();
							String requestIp = dis.readUTF();
							int requestPort = dis.readInt();
							ObjectInputStream is = new ObjectInputStream(s.getInputStream());
							@SuppressWarnings("unchecked")
							ArrayList<Integer> trail = (ArrayList<Integer>) is.readObject();
							System.out.println(
									"bsNS::forwardEntryRequest:: req_id: " + requestID + " suc_id: " + bs.successorID);
							bs.forwardEntryRequest(requestID, requestIp, requestPort, bs.successorID, bs.successorIp,
									bs.successorPort, trail);
							if (requestID > bs.id && requestID < bs.successorID) {
								bs.successorID = requestID;
								bs.successorIp = requestIp;
								bs.successorPort = requestPort;
							}
						}
						break;
					case "Forwarding entry request":

						int requestID = dis.readInt();
						String requestIp = dis.readUTF();
						int requestPort = dis.readInt();
						ObjectInputStream is = new ObjectInputStream(s.getInputStream());
						@SuppressWarnings("unchecked")
						ArrayList<Integer> t = (ArrayList<Integer>) is.readObject();
						if (requestID > bs.predecessorID) {
							// successor found
							// update keys
							int newPredecessorID = bs.predecessorID;
							String newPredecessorIp = bs.predecessorIp;
							int newPredecessorPort = bs.predecessorPort;
							SortedMap<Integer, String> subMap = new TreeMap<Integer, String>();
							ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
							t.add(bs.id);
							for (Map.Entry<Integer, String> entry : bs.data.entrySet()) {
								int currentID = entry.getKey();
								String currentValue = entry.getValue();
								if (currentID > bs.predecessorID && currentID <= requestID) {
									subMap.put(currentID, currentValue);
								}
							}
							for (Map.Entry<Integer, String> entry : subMap.entrySet()) {
								int currentID = entry.getKey();
								String currentValue = entry.getValue();
								if (bs.data.containsKey(currentID)) {
									bs.data.remove(currentID, currentValue);
								}
							}
							// update successor and predecessor
							bs.predecessorID = requestID;
							bs.predecessorIp = requestIp;
							bs.predecessorPort = requestPort;
							// sending data to the request server
							Socket a = new Socket(requestIp, requestPort);
							dis = new DataInputStream(a.getInputStream());
							dos = new DataOutputStream(a.getOutputStream());
							dos.writeUTF("Name server has arrived a response for id:" + requestID);
							dos.writeUTF("Accepting Forwarding Request");
							dos.writeInt(bs.id);
							dos.writeUTF(bs.serverIP);
							dos.writeInt(bs.serverPort);
							dos.writeInt(newPredecessorID);
							dos.writeUTF(newPredecessorIp);
							dos.writeInt(newPredecessorPort);
							os = new ObjectOutputStream(a.getOutputStream());
							os.writeObject(subMap);
							os.writeObject(t);
							a.close();
						} else {
							bs.forwardEntryRequest(requestID, requestIp, requestPort, bs.successorID, bs.successorIp,
									bs.successorPort, t);
						}
						break;
					case "Update predecessor request":
						dis = new DataInputStream(s.getInputStream());
						bs.successorID = dis.readInt();
						bs.successorIp = dis.readUTF();
						bs.successorPort = dis.readInt();
						break;

					case "Update successor request":
						dis = new DataInputStream(s.getInputStream());
						bs.predecessorID = dis.readInt();
						bs.predecessorIp = dis.readUTF();
						bs.predecessorPort = dis.readInt();
						is = new ObjectInputStream(s.getInputStream());
						@SuppressWarnings("unchecked")
						SortedMap<Integer, String> temp = (TreeMap<Integer, String>) is.readObject();
						for (Map.Entry<Integer, String> entry : temp.entrySet()) {
							bs.data.put(entry.getKey(), entry.getValue());
						}
						break;
					case "Update lookup response":
						dis = new DataInputStream(s.getInputStream());
						bs.lookupKeyResponse = dis.readUTF();
						is = new ObjectInputStream(s.getInputStream());
						bs.lookupTrail = (ArrayList<Integer>) is.readObject();
						break;

					case "Update insert response":
						is = new ObjectInputStream(s.getInputStream());
						bs.lookupTrail = (ArrayList<Integer>) is.readObject();
						break;

					case "Update delete response":
						is = new ObjectInputStream(s.getInputStream());
						bs.lookupTrail = (ArrayList<Integer>) is.readObject();
						break;
					case "exit":
						break;
					}

				}

			} catch (Exception e) {
				s.close();
				e.printStackTrace();
			}
		}
	}
}
