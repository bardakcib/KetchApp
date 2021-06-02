package Core;

import Tests.*;
import UI_Layer.MainPage;

public class Core {
	public static void main(String[] args) throws Exception {
		try {
			MainPage mainPage = new MainPage();

			Thread thread = new Thread(mainPage);
			thread.start();

			mainPage.setVisible(true);

//			RunTests();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void RunTests() {
		new RSA_Test("selam");
		new GsonTest();
	}
}
