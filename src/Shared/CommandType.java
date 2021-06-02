package Shared;

public enum CommandType {
	GroupMessage("GroupMessage"), PrivateMessage("PrivateMessage"),Connected("Connected"), Disconnected("Disconnected"), UserList("UserList");

	String value;

	CommandType(String p) {
		value = p;
	}

	String getValue() {
		return value;
	}

}
