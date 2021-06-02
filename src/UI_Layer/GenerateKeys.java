package UI_Layer;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JLabel;
import Shared.KeysGenerated;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import java.awt.HeadlessException;

public class GenerateKeys {
	private JPanel temp;
	private String pubKeyStr, priKeyStr;
	private JLabel priKeyLabel, pubKeyLabel;
	private JTextArea priKeyArea, pubKeyArea;

	public GenerateKeys(JPanel parent, KeysGenerated keys) {
		temp = parent;
		pubKeyStr = keys.publicKeyStr;
		priKeyStr = keys.privateKeyStr;
	}

	public void ShowPage() throws HeadlessException, Exception {
		Object[] buttonNames = { "Ok" };

		JOptionPane.showOptionDialog(temp.getParent(), CustomPanelPublicPrivateKey(), "Generate Public & Private Keys",
				JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttonNames, null);
	}

	private JPanel CustomPanelPublicPrivateKey() throws Exception {
		JPanel customPanelPublicPrivateKey = new JPanel();

		customPanelPublicPrivateKey.setPreferredSize(new Dimension(900, 600));
		priKeyLabel = new JLabel("Private Key");
		pubKeyLabel = new JLabel("Public Key");
		pubKeyLabel.setPreferredSize(new Dimension(900, 25));
		priKeyLabel.setPreferredSize(new Dimension(900, 25));

		pubKeyArea = new JTextArea();
		pubKeyArea.setLineWrap(true);
		pubKeyArea.setEditable(false);
		pubKeyArea.setPreferredSize(new Dimension(900, 100));

		priKeyArea = new JTextArea();
		priKeyArea.setLineWrap(true);
		priKeyArea.setEditable(false);
		priKeyArea.setPreferredSize(new Dimension(900, 420));

		pubKeyArea.setText(pubKeyStr);
		priKeyArea.setText(priKeyStr);

		customPanelPublicPrivateKey.add(pubKeyLabel);
		customPanelPublicPrivateKey.add(pubKeyArea);
		customPanelPublicPrivateKey.add(priKeyLabel);
		customPanelPublicPrivateKey.add(priKeyArea);

		return customPanelPublicPrivateKey;
	}
}
