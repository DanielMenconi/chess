package piece;

import main.GamePanel;
import main.Type;

public class Queen extends Piece {

	public Queen(int color, int col, int row) {
		
		super(color, col, row);
		
		type = Type.QUEEN;
		
		if(color == GamePanel.WHITE) {
			if(GamePanel.theme == 1) {
				image = getImage("/piece/w-queen-p");
			}else {
				image = getImage("/piece/w-queen");
			}
			
		}
		else {
			if(GamePanel.theme == 1) {
				image = getImage("/piece/b-queen-p");
			}else {
				image = getImage("/piece/b-queen");
			}
		}
	}
	public boolean canMove(int targetCol, int targetRow) {
		
		if(isWithinBoard(targetCol,targetRow) && isSameSquare(targetCol,targetRow) == false) {
			
			//Vertical & Horizontal
			if(targetCol == preCol || targetRow == preRow) {
				if(isValidSquare(targetCol,targetRow) && pieceIsOnStraightLine(targetCol,targetRow) == false) {
					return true;
				}
			}
			
		}
		
		// Diagonal
		if(Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)) {
			if(isValidSquare(targetCol,targetRow) && pieceIsOnDiagonalLine(targetCol,targetRow) == false) {
				if(targetCol!=preCol || targetRow!=preRow) // works but doens't know why!	!	
				return true;
			}
		}
		return false;
	}
	
}
