package Core;

import Shared.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;

public class MyMultiCast {

	private int port;
	private Gson gson;
	private String jsonString;
	private RSA_Encryption rsa;
	private MulticastSocket socket;
	private InetSocketAddress group;
	private InetAddress multiCastAddr;
	private NetworkInterface networkInterf;

	public MulticastSocket StartCasting(String nickName, String hostIp, int portNumber) throws Exception {
		try {
			gson = new Gson();
			port = portNumber;
			rsa = new RSA_Encryption(false);
			socket = new MulticastSocket(port);
			multiCastAddr = InetAddress.getByName(hostIp);
			group = new InetSocketAddress(multiCastAddr, port);
			networkInterf = NetworkInterface.getByName("bge0");

			socket.setTimeToLive(111);
			socket.joinGroup(group, networkInterf);

		} catch (SocketException se) {
			System.out.println("Error creating socket");
			se.printStackTrace();
		} catch (IOException ie) {
			System.out.println("Error reading/writing from/to socket");
			ie.printStackTrace();
		}

		return socket;
	}

	public boolean sendKMessage(KMessage msg) {
		boolean result = false;

		try {
			switch (msg.type) {
			case UserList: {
				UserList user = new UserList();
				for (int i = 0; i < msg.userList.size(); i++) {
					user.nickName = msg.userList.get(i).nickName;
					user.nickName = rsa.Encrypt(user.nickName, null);
					user.publicKey = msg.userList.get(i).publicKey;
					msg.userList.set(i, user);
				}
				break;
			}
			case PrivateMessage: {
				msg.message = rsa.Encrypt(msg.message, msg.privateReceiverPublicKey);
				break;
			}
			default:
				msg.userList = new ArrayList<UserList>();
				msg.message = rsa.Encrypt(msg.message, null);
				break;
			}

			jsonString = gson.toJson(msg);
			System.out.println("Sent : " + jsonString);

			byte[] msgBytes = jsonString.getBytes(StandardCharsets.UTF_8);
			DatagramPacket datagram = new DatagramPacket(msgBytes, msgBytes.length, multiCastAddr, port);
			socket.send(datagram);

			if (msg.type.equals(CommandType.Disconnected)) {
				socket.leaveGroup(group, networkInterf);
				socket.close();
			}

			result = true;

		} catch (Exception ie) {
			System.out.println(ie.getMessage());
			ie.printStackTrace();
		}

		return result;
	}
}
