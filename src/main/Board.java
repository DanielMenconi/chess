package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class Board {

	final double MAX_COL = 9.5;
	final double MAX_ROW = 9.5;
	public static final int SQUARE_SIZE = 100;
	public static final int HALF_SQUARE_SIZE = SQUARE_SIZE / 2;
	String[] letters = { "a", "b", "c", "d", "e", "f", "g", "h" };
	int[] numbers = { 8, 7, 6, 5, 4, 3, 2, 1 };

	public void draw(Graphics2D g2) { // here we create the chess board square by square

		int i = 0, j = 0, c = 0;
		int integerCol, integerRow;
		boolean numberF = true;

		for (float row = 1.5f; row < MAX_ROW ; row++) {

			
			numberF = true;

			for (float col = 1.5f; col < MAX_COL ; col++) {

				

				if (c == 0) {
					if (GamePanel.theme == 0) {
						g2.setColor(new Color(236, 236, 209, 255));
					} else {
						g2.setColor(new Color(217,228,232,255));

					}

					c = 1;
				} else {
					if (GamePanel.theme == 0) {
						g2.setColor(new Color(116, 149, 83, 255));
					} else {
						g2.setColor(new Color(116,151,173,255));
					}

					c = 0;
				}

				integerCol = (int) col * SQUARE_SIZE; // this is because i needed to move the board to make space in the
														// sides but to do it I needed float so now to use in Fillrect I
														// need to convert them
				integerRow = (int) row * SQUARE_SIZE;

				g2.fillRect(integerCol, integerRow, SQUARE_SIZE, SQUARE_SIZE);

				if (row == 8.5) {
					if (i % 2 == 0) {
						g2.setColor(Color.black);
					} else {
						g2.setColor(Color.gray);
					}

					g2.setFont(new Font("Helvetica", Font.BOLD, 20));
					g2.drawString(letters[i], integerCol+88, integerRow + 95);
					i++;
				}
				
				if (numberF) {
					if (j % 2 == 1) {
						g2.setColor(Color.black);
					} else {
						g2.setColor(Color.gray);
					}
					numberF = false;
					g2.setFont(new Font("Helvetica", Font.BOLD, 20));
					g2.drawString(String.valueOf(numbers[j]), integerCol, integerRow+15);
					j++;
					
				}
				
			}

			// this makes me not change color when i go from one row to another
			if (c == 0) {
				c = 1;
			} else {
				c = 0;
			}
		}
	}
}
