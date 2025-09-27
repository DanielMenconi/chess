package main;

import javax.swing.JFrame;

public class Main {
	public static void main(String[] args) {
		 
		JFrame window = new JFrame("Chess"); // game window
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // when we close the window we close also the program 
		window.setResizable(false); // it is not possible to resize
		
		// Add GamePanel to the window
		GamePanel gp = new GamePanel();
		window.add(gp);
		window.pack(); // the window adjust its size to the GamePanel
			
		
		window.setLocationRelativeTo(null); // the window will be seen at the center of the monitor otherwise it will be top-left
		window.setVisible(true); // now we can actually see it
		
		gp.lunchGame();
		
	}
}
