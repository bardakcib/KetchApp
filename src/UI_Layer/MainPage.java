package UI_Layer;

import Core.*;
import Shared.*;
import java.awt.*;
import javax.swing.*;
import java.util.List;
import java.awt.event.*;
import java.util.Base64;
import java.util.ArrayList;
import javax.swing.table.*;
import java.io.IOException;
import com.google.gson.Gson;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class MainPage extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;

//	private String hostIP = "228.5.6.7";
	private int port = 1256;
	private String hostIP = "ff02::1";

	private About aboutPage;
	private MyMultiCast cast;
	private JMenuBar menuBar;
	private JTable userTable;
	private JButton sendButton;
	private RSA_Encryption rsa;
	private JComboBox userCombo;
	private Gson gson = new Gson();
	private MulticastSocket socket;
	private JTextField nickNameField;
	private ImageIcon sendButtonIcon;
	private JMenu fileMenu, helpMenu;
	private Image appLogo, sendImage;
	private GenerateKeys generateKeys;
	private KeysGenerated generatedKeys;
	private DefaultTableModel userTableModel;
	private KMessage message, messageReceived;
	private DefaultComboBoxModel userComboModel;
	private JScrollPane channelMessageScroll, messageScroll;
	private JTextArea messageTextArea, channelMessageTextArea;
	private boolean isThreadRunning, isConnected, isGenerated;
	private JLabel logoLabel, nickNameLabel, senderReceiverLabel;
	private JPanel channelMessagePanel, usersPanel, sendButtonPanel, logoPanel, messagePanel, infoPanel;
	private JMenuItem menuItemGenerateKeys, menuItemExit, menuItemDisconnect, menuItemConnect, menuItemAbout;

	public MainPage() throws Exception {
		isConnected = false;

		InitializeGuiElements();

		SetDisconnectedFlags();

		menuItemConnect.setEnabled(false);
	}

	private void InitializeGuiElements() {
		setTitle("Catch Up with KethcApp");

		getContentPane().setLayout(new GridBagLayout());

		appLogo = new ImageIcon("resources\\imageFiles\\logo.png").getImage();
		appLogo = appLogo.getScaledInstance(128, 128, java.awt.Image.SCALE_SMOOTH);

		logoPanel = new JPanel(new BorderLayout());
		logoPanel.setBorder(BorderFactory.createEtchedBorder());

		logoLabel = new JLabel();
		logoLabel.setName("logoImage");
		logoLabel.setIcon(new ImageIcon(appLogo));
		logoLabel.setName("expCollision");

		logoLabel.setVisible(true);
		logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		logoPanel.add(logoLabel, BorderLayout.CENTER);

		messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(BorderFactory.createEtchedBorder());

		infoPanel = new JPanel(new BorderLayout());
		infoPanel.setBorder(BorderFactory.createEtchedBorder());

		senderReceiverLabel = new JLabel();
		senderReceiverLabel.setText("");
		senderReceiverLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		infoPanel.add(senderReceiverLabel);

		messageTextArea = new JTextArea();
		messageTextArea.setFont(messageTextArea.getFont().deriveFont(22f));
		messageTextArea.setWrapStyleWord(true);
		messageTextArea.setLineWrap(true);
		messageTextArea.setEditable(true);
		messageTextArea.setFocusable(true);
		messageTextArea.requestFocus();
		messageTextArea.setOpaque(false);
		messageTextArea.addKeyListener(new KeyAdapter() {
			@Override
			public synchronized void keyReleased(KeyEvent e) {
				if (messageTextArea.getText().length() == 0) {
					sendButton.setEnabled(false);
				} else {
					sendButton.setEnabled(true);
				}
			}
		});

		messageScroll = new JScrollPane(messageTextArea);

		messagePanel.add(messageScroll);

		sendImage = new ImageIcon("resources\\imageFiles\\send.png").getImage();
		sendButtonIcon = new ImageIcon(sendImage.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH));

		sendButton = new JButton(sendButtonIcon);
		sendButton.addActionListener(SendMessageListener());
		sendButtonPanel = new JPanel(new BorderLayout());
		sendButtonPanel.setBorder(BorderFactory.createEtchedBorder());

		String[] petStrings = { "Everyone" };
		userCombo = new JComboBox(petStrings);
		userCombo.setSelectedIndex(0);

		userCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				message.type = CommandType.GroupMessage;
				message.receiver = String.valueOf(userCombo.getSelectedItem());
				UpdateSenderReceiverLabel(message.receiver);
				if (!message.receiver.equals("Everyone"))
					message.type = CommandType.PrivateMessage;
			}
		});

		sendButtonPanel.add(userCombo, BorderLayout.NORTH);
		sendButtonPanel.add(sendButton, BorderLayout.SOUTH);

		usersPanel = new JPanel(new BorderLayout());
		usersPanel.setBorder(BorderFactory.createEtchedBorder());
		JLabel label4 = new JLabel("Users Panel");
		label4.setHorizontalAlignment(SwingConstants.CENTER);
		usersPanel.add(label4, BorderLayout.CENTER);

		channelMessagePanel = new JPanel(new BorderLayout());
		channelMessagePanel.setBorder(BorderFactory.createEtchedBorder());

		channelMessageTextArea = new JTextArea();
		channelMessageTextArea.setFont(channelMessageTextArea.getFont().deriveFont(22f));

		channelMessageTextArea.setWrapStyleWord(true);
		channelMessageTextArea.setLineWrap(true);
		channelMessageTextArea.setEditable(false);
		channelMessageTextArea.setOpaque(false);

		channelMessageScroll = new JScrollPane(channelMessageTextArea);
		channelMessagePanel.add(channelMessageScroll);

		userTableModel = new DefaultTableModel();
		userTable = new JTable(userTableModel);
		userTable.setDefaultEditor(Object.class, null);
		userTableModel.addColumn("Online Users");
		userTableModel.addColumn("Public Keys");
		userTable.getColumnModel().getColumn(1).setMaxWidth(-1);
		userTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if (me.getClickCount() == 2) { // to detect doble click events
					JTable target = (JTable) me.getSource();
					int row = target.getSelectedRow();
					int column = target.getSelectedColumn();

					if (!userTable.getValueAt(row, column).equals(message.sender)) {

						if (!userTable.getValueAt(row, column).equals(message.sender)) {
							message.receiver = String.valueOf(userTable.getValueAt(row, column));
							userCombo.setSelectedItem(message.receiver);
							UpdateSenderReceiverLabel(message.receiver);
						}
					}
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(userTable);
		usersPanel.add(scrollPane);

		InitializeMenuBar();

		InitializePanelGrids();

		setSize(1024, 768);
		setLocationRelativeTo(null); // center screen
		setVisible(true);

		validate(); // add iþlemlerinden sonra validate yapýldý
	}

	private void UpdateSenderReceiverLabel(String receiver) {
		message.privateReceiverPublicKey = "";
		message.type = CommandType.GroupMessage;

		if (!receiver.equals("Everyone")) {
			message.type = CommandType.PrivateMessage;
			for (int i = 0; i < userTable.getRowCount(); i++) {
				if (receiver.equals(userTable.getValueAt(i, 0).toString())) {
					message.privateReceiverPublicKey = userTable.getValueAt(i, 1).toString();
				}
			}
		}

		senderReceiverLabel.setText("user : " + message.sender + " --> reciever : " + receiver);
	}

	private void InitializePanelGrids() {
		this.getContentPane().add(infoPanel, new GridBagConstraints(0, 0, 3, 1, 1.0, 0.02, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		this.getContentPane().add(logoPanel, new GridBagConstraints(0, 1, 1, 1, 0.1, 0.28, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		this.getContentPane().add(messagePanel, new GridBagConstraints(1, 1, 1, 1, 0.89, 0.28, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		this.getContentPane().add(sendButtonPanel, new GridBagConstraints(2, 1, 1, 1, 0.01, 0.28,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		this.getContentPane().add(usersPanel, new GridBagConstraints(0, 2, 1, 1, 0.1, 0.7, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		this.getContentPane().add(channelMessagePanel, new GridBagConstraints(1, 2, 2, 1, 0.9, 0.7,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		validate();
	}

	private void InitializeMenuBar() {
		menuBar = new JMenuBar();

		fileMenu = new JMenu("File");
		helpMenu = new JMenu("Help");

		menuItemGenerateKeys = new JMenuItem("Generate Keys");
		menuItemConnect = new JMenuItem("Connect To Network");
		menuItemDisconnect = new JMenuItem("Disconnect From Network");
		menuItemDisconnect.setEnabled(false);
		menuItemExit = new JMenuItem("Exit");

		menuItemConnect.addActionListener(AddUserToNetwork());
		menuItemDisconnect.addActionListener(Disconnect());
		menuItemExit.addActionListener(ExitApp());

		menuItemGenerateKeys.addActionListener(GenerateKeyPair());

		menuItemAbout = new JMenuItem("About");
		aboutPage = new About(logoPanel);
		menuItemAbout.addActionListener(aboutPage.PanelListener());

		fileMenu.add(menuItemGenerateKeys);
		fileMenu.add(menuItemConnect);
		fileMenu.add(menuItemDisconnect);
		fileMenu.add(menuItemExit);

		helpMenu.add(menuItemAbout);

		menuBar.add(fileMenu);
		menuBar.add(helpMenu);

		setJMenuBar(menuBar);
	}

	private ActionListener AddUserToNetwork() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Object[] buttonNames = { "Connect", "Cancel" };

					int result = JOptionPane.showOptionDialog(logoPanel.getParent(), CustomPanel(),
							"Connect To Network", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
							buttonNames, null);

					if (result == JOptionPane.YES_OPTION) {
						cast = new MyMultiCast();
						message = new KMessage();
						messageReceived = new KMessage();
						message.receiver = "Everyone";

						if (nickNameField.getText().contains(";") || nickNameField.getText().contains("||")) {
							JOptionPane.showConfirmDialog(logoPanel.getParent(), "Please Don't Use Special Characters",
									"Connection Request Failed" + " Failed !!!", JOptionPane.DEFAULT_OPTION,
									JOptionPane.ERROR_MESSAGE);
						} else {
							if (nickNameField.getText().length() < 3 || nickNameField.getText().length() > 10) {
								JOptionPane.showConfirmDialog(logoPanel.getParent(),
										"Nickname length should be between 3 & 10 Characters",
										"Connection Request Failed" + " Failed !!!", JOptionPane.DEFAULT_OPTION,
										JOptionPane.ERROR_MESSAGE);
							} else {
								message.sender = nickNameField.getText();
								UpdateSenderReceiverLabel(message.receiver);
								socket = cast.StartCasting(message.sender, hostIP, port);
								if (!existsInTable(userTable,
										new Object[] { message.sender, generatedKeys.publicKeyStr })) {
									userTableModel.insertRow(0,
											new Object[] { message.sender, generatedKeys.publicKeyStr });
								}
								SetConnectedFlags();
								SendMessage(CommandType.Connected);
								Thread.sleep(500);
								SendMessage(CommandType.UserList);
							}
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
	}

	private void SendMessage(CommandType commandType) {
		message.userList = new ArrayList<UserList>();

		switch (commandType) {
		case GroupMessage:
			message.type = CommandType.GroupMessage;
			message.message = message.sender + " : " + messageTextArea.getText();
			message.privateReceiverPublicKey = "";
			break;
		case PrivateMessage:
			message.type = CommandType.PrivateMessage;
			message.message = "Private message to " + message.receiver + " from " + message.sender + " : "
					+ messageTextArea.getText();
			break;
		case Connected:
			message.receiver = "Everyone";
			message.type = CommandType.Connected;
			message.message = message.sender + " connected";
			message.privateReceiverPublicKey = "";
			break;
		case Disconnected:
			message.receiver = "Everyone";
			message.type = CommandType.Disconnected;
			message.message = message.sender + " left";
			message.privateReceiverPublicKey = "";
			break;
		case UserList:
			message.receiver = "Everyone";
			message.type = CommandType.UserList;
			message.message = "";
			message.privateReceiverPublicKey = "";
			UserList user = new UserList();
			for (int i = 0; i < userTable.getRowCount(); i++) {
				user.nickName = userTable.getValueAt(i, 0).toString();
				user.publicKey = userTable.getValueAt(i, 1).toString();
				message.userList.add(user);
			}
			break;
		default:
			message.type = CommandType.GroupMessage;
			message.message = message.sender + " : " + messageTextArea.getText();
			message.privateReceiverPublicKey = "";
			break;
		}

		if (!messageTextArea.getText().trim().isEmpty() || message.userList != null) {
			try {
				if (cast.sendKMessage(message)) {

					if (message.type.equals(CommandType.GroupMessage)) {
						channelMessageTextArea.append(message.sender + " : " + messageTextArea.getText() + "\n");
						messageTextArea.setText("");
					}
					if (message.type.equals(CommandType.PrivateMessage)) {
						channelMessageTextArea.append("Private message to " + message.receiver + " from "
								+ message.sender + " : " + messageTextArea.getText() + "\n");
						messageTextArea.setText("");
					}
					channelMessageTextArea.setCaretPosition(channelMessageTextArea.getText().length());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		messageTextArea.requestFocus();

	}

	private void UpdateUserList(List<UserList> userList) {
		try {
			byte[] chipper;
			String tempNickname, tempPublicKey;
			int beforeUserUpdate = userTable.getRowCount();
			if (userList != null) {
				for (int i = 0; i < userList.size(); i++) {
					chipper = Base64.getDecoder().decode(userList.get(i).nickName);
					tempNickname = rsa.Decrypt(chipper, null);

					tempPublicKey = userList.get(i).publicKey;

					if (!existsInTable(userTable, new Object[] { tempNickname, tempPublicKey })) {
						userTableModel.insertRow(0, new Object[] { tempNickname, tempPublicKey });
					}
				}

				if (beforeUserUpdate < userTable.getRowCount()) {
					System.out.println("new user found");
					SendMessage(CommandType.UserList);
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		userCombo.removeAllItems();
		userCombo.addItem("Everyone");
		for (int i = 0; i < userTable.getRowCount(); i++) {
			String rowEntry = userTable.getValueAt(i, 0).toString();

			if (!rowEntry.equals(message.sender))
				userCombo.addItem(rowEntry);
		}
	}

	public boolean existsInTable(JTable table, Object[] entry) {

		// Get row and column count
		int rowCount = table.getRowCount();
		int colCount = table.getColumnCount();

		// Get Current Table Entry
		String curEntry = "";
		for (Object o : entry) {
			String e = o.toString();
			curEntry = curEntry + " " + e;
		}

		// Check against all entries
		for (int i = 0; i < rowCount; i++) {
			String rowEntry = "";
			for (int j = 0; j < colCount; j++)
				rowEntry = rowEntry + " " + table.getValueAt(i, j).toString();
			if (rowEntry.equalsIgnoreCase(curEntry)) {
				return true;
			}
		}
		return false;
	}

	private JPanel CustomPanel() {
		JPanel customPanel = new JPanel();
		customPanel.setLayout(new GridLayout(2, 1));
		nickNameLabel = new JLabel("Please Enter Your Nickname");
		nickNameField = new JTextField();
		nickNameField.setPreferredSize(new Dimension(100, 25));

		customPanel.add(nickNameLabel);
		customPanel.add(nickNameField);

		return customPanel;
	}

	private ActionListener SendMessageListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					SendMessage(message.type);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
	}

	private ActionListener GenerateKeyPair() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (!isGenerated) {
						isGenerated = true;
						rsa = new RSA_Encryption(true);
						generatedKeys = new KeysGenerated();
						generatedKeys.publicKeyStr = rsa.GetMyPublicKey();
						generatedKeys.privateKeyStr = rsa.GetMyPrivateKey();
						generateKeys = new GenerateKeys(logoPanel, generatedKeys);
					}

					generateKeys.ShowPage();
					menuItemConnect.setEnabled(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
	}

	private ActionListener Disconnect() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(logoPanel.getParent(),
						"WOAh, You will miss out all the fun. Are you sure you want to disconnect :( ?",
						"WOAH - Disconnect?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					try {
						SendMessage(CommandType.Disconnected);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					SetDisconnectedFlags();
				}
			}
		};
	}

	private ActionListener ExitApp() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(logoPanel.getParent(),
						"You will miss out all the fun. Are you sure you want to quit :( ?", "WOAH - Quit?",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					try {
						SendMessage(CommandType.Disconnected);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					dispose();
					System.exit(0);
				}
			}
		};
	}

	private void SetDisconnectedFlags() {
		sendButton.setEnabled(false);
		menuItemDisconnect.setEnabled(false);
		menuItemConnect.setEnabled(true);
		isConnected = false;

		infoPanel.setVisible(false);
		channelMessagePanel.setVisible(false);
		usersPanel.setVisible(false);
		sendButtonPanel.setVisible(false);
		messagePanel.setVisible(false);

		InitializePanelGrids();
	}

	private void SetConnectedFlags() {
		sendButton.setEnabled(false);
		menuItemDisconnect.setEnabled(true);
		menuItemConnect.setEnabled(false);
		isConnected = true;

		infoPanel.setVisible(true);
		channelMessagePanel.setVisible(true);
		usersPanel.setVisible(true);
		sendButtonPanel.setVisible(true);
		messagePanel.setVisible(true);

		InitializePanelGrids();
	}

	@Override
	public void run() {
		try {
			isThreadRunning = true;
			byte[] chipper;
			String jsonMessage = "";

			while (isThreadRunning) {
				Thread.sleep(500);
				if (isConnected) {
					byte[] buffer = new byte[16348];
					DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
					socket.receive(datagram);
					jsonMessage = new String(buffer, 0, datagram.getLength(), "UTF-8");

					System.out.println("Received : " + jsonMessage);
					messageReceived = gson.fromJson(jsonMessage, KMessage.class);

					if (messageReceived.type.equals(CommandType.UserList)) {
						UpdateUserList(messageReceived.userList);
					} else {
						chipper = Base64.getDecoder().decode(messageReceived.message);

						if (!messageReceived.sender.equals(message.sender)) {

							if (messageReceived.type.equals(CommandType.PrivateMessage)
									&& messageReceived.receiver.equals(message.sender)) {
								messageReceived.message = rsa.Decrypt(chipper, generatedKeys.privateKeyStr);
								channelMessageTextArea.append(messageReceived.message + "\n");
								channelMessageTextArea.setCaretPosition(channelMessageTextArea.getText().length());
							}

							if (!messageReceived.type.equals(CommandType.PrivateMessage)) {
								messageReceived.message = rsa.Decrypt(chipper, null);
								channelMessageTextArea.append(messageReceived.message + "\n");
								channelMessageTextArea.setCaretPosition(channelMessageTextArea.getText().length());
							}

							if (messageReceived.type.equals(CommandType.Disconnected)) {
								for (int i = 0; i < userTable.getRowCount(); i++) {
									String rowEntry = userTable.getValueAt(i, 0).toString();
									System.out.println("here");
									System.out.println(rowEntry);
									if (rowEntry.equals(messageReceived.sender))
										userTableModel.removeRow(i);
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
}