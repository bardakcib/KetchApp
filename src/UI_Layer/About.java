package UI_Layer;

import java.awt.Image;
import java.awt.Dimension;
import javax.swing.JPanel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class About {

	JPanel temp;
	private Image appLogo;

	public About(JPanel parent) {
		temp = parent;
	}

	public ActionListener PanelListener() {
		appLogo = new ImageIcon("resources\\imageFiles\\logo.png").getImage();

		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImageIcon icon = new ImageIcon(appLogo.getScaledInstance(128, 128, java.awt.Image.SCALE_SMOOTH));

				JOptionPane.showConfirmDialog(temp.getParent(), CustomAboutPanel(), "About Ketch App",
						JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
			}
		};
	}

	// creating a panel to insert our pop up which created by JOptionPane to show
	public JPanel CustomAboutPanel() {
		JPanel panel = new JPanel();

		panel.setPreferredSize(new Dimension(700, 600));

		String messege = "Welcome tp Ketch App" + "\n\nThis is a term project for CSE471 - Sprint 2021"
				+ "\n\nCreated by Bedirhan Bardakci (20150702053)";
		try {
			messege = messege + "\n" + new String(Files.readAllBytes(Paths.get("resources\\references.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		JTextArea messageLabel = new JTextArea();
		messageLabel.setSize(620, 600);
		messageLabel.setFont(messageLabel.getFont().deriveFont(18f)); // will only change size to 12pt
		messageLabel.setWrapStyleWord(true);
		messageLabel.setLineWrap(true);
		messageLabel.setEditable(false);
		messageLabel.setOpaque(false);
		messageLabel.setText(messege);
		panel.add(messageLabel);
		return panel;
	}
}
