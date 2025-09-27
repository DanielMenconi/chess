package piece;

import main.GamePanel;
import main.Type;

public class Bishop extends Piece {

	public Bishop(int color, int col, int row) {
		
	
		super(color, col, row);
		
		type = Type.BISHOP;
		
		if(color == GamePanel.WHITE) {
			if(GamePanel.theme == 1) {
				image = getImage("/piece/w-bishop-p");
			}else {
				image = getImage("/piece/w-bishop");
			}
			
		}
		else {
			if(GamePanel.theme == 1) {
				image = getImage("/piece/b-bishop-p");
			}else {
				image = getImage("/piece/b-bishop");
			}
		}
	}
	public boolean canMove(int targetCol, int targetRow) {
		
		if(isWithinBoard(targetCol,targetRow) && isSameSquare(targetCol,targetRow) == false) {
			
			if(Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)) { // row difference and column difference always need to be equal to be diagonal
				if(isValidSquare(targetCol,targetRow) && pieceIsOnDiagonalLine(targetCol, targetRow) == false) {
					return true;
				}
			}
		}
		return false;
	}
	
	
}
