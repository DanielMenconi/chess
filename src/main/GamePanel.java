package main;

import java.util.Timer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.JPanel;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel implements Runnable {

	public static final int WIDTH = 1100;
	public static final int HEIGHT = 1000;
	final int FPS = 60;
	private int whiteTime = 300;
	private int blackTime = 300;
	Thread gameThread;
	Board board = new Board();
	Mouse mouse = new Mouse();
	SoundPlayer soundplayer = new SoundPlayer();
	// PIECES
	public static ArrayList<Piece> pieces = new ArrayList<>();
	public static ArrayList<Piece> simPieces = new ArrayList<>();

	public static ArrayList<Piece> blackCaptures = new ArrayList<>();
	public static ArrayList<Piece> whiteCaptures = new ArrayList<>();
	ArrayList<Piece> promoPieces = new ArrayList<>();
	Piece activeP, checkingP;
	public static Piece castlingP;

	// COLOR
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	int currentColor = WHITE;
	public static int theme;

	// boolean
	boolean canMove;
	boolean validSquare;
	boolean promotion;
	boolean gameover = false;
	boolean stalemate;
	boolean firstMove = false;
	boolean clock = false;
	boolean menu = false;
	boolean start = true;
	boolean settings = false;
	boolean pieceCreation = false;
	boolean sound = true;
	boolean music = false;
	boolean highlights = true;
	boolean time = true;

	// sound variables

	boolean castling = false;
	boolean capturing = false;

	// highlighting last move
	int startingSquareCol = 999;
	int startingSquareRow = 999;
	int endingSquareCol = 999;
	int endingSquareRow = 999;
	boolean startingFlag = false;
	int cStartingSquareCol = 999;
	int cStartingSquareRow = 999;
	
	
	
	// moves
	ArrayList<Integer> rowMoves = new ArrayList<Integer>();
	ArrayList<String> colMoves = new ArrayList<String>();

	public GamePanel() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(new Color(48, 46, 43));
		addMouseMotionListener(mouse);
		addMouseListener(mouse);


	}

	public void lunchGame() {
		gameThread = new Thread(this); // we instantiate the Thread
		gameThread.start(); // then call its start method, and basically call the run method
	}

	public void setPieces() {

		// white team
		pieces.add(new Pawn(WHITE, 1, 7));
		pieces.add(new Pawn(WHITE, 2, 7));
		pieces.add(new Pawn(WHITE, 3, 7));
		pieces.add(new Pawn(WHITE, 4, 7));
		pieces.add(new Pawn(WHITE, 5, 7));
		pieces.add(new Pawn(WHITE, 6, 7));
		pieces.add(new Pawn(WHITE, 7, 7));
		pieces.add(new Pawn(WHITE, 8, 7));
		pieces.add(new Rook(WHITE, 1, 8));
		pieces.add(new Rook(WHITE, 8, 8));
		pieces.add(new Knight(WHITE, 2, 8));
		pieces.add(new Knight(WHITE, 7, 8));
		pieces.add(new Bishop(WHITE, 3, 8));
		pieces.add(new Bishop(WHITE, 6, 8));
		pieces.add(new Queen(WHITE, 4, 8));
		pieces.add(new King(WHITE, 5, 8));

		// black team
		pieces.add(new Pawn(BLACK, 1, 2));
		pieces.add(new Pawn(BLACK, 2, 2));
		pieces.add(new Pawn(BLACK, 3, 2));
		pieces.add(new Pawn(BLACK, 4, 2));
		pieces.add(new Pawn(BLACK, 5, 2));
		pieces.add(new Pawn(BLACK, 6, 2));
		pieces.add(new Pawn(BLACK, 7, 2));
		pieces.add(new Pawn(BLACK, 8, 2));
		pieces.add(new Rook(BLACK, 1, 1));
		pieces.add(new Rook(BLACK, 8, 1));
		pieces.add(new Knight(BLACK, 2, 1));
		pieces.add(new Knight(BLACK, 7, 1));
		pieces.add(new Bishop(BLACK, 3, 1));
		pieces.add(new Bishop(BLACK, 6, 1));
		pieces.add(new Queen(BLACK, 4, 1));
		pieces.add(new King(BLACK, 5, 1));
	}

	// receives two list, first we clear the target list and add everything to the
	// target just cleared
	private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {

		target.clear();
		for (int i = 0; i < source.size(); i++) {
			target.add(source.get(i));
		}
	}

	// this is needed to run the game loop and it works with game panel that
	// implements the Runnable interface that this method in it
	@Override
	public void run() {

		// GAME LOOP we use the system nano-time to measure the elapsed time and call
		// update and repaint methods once every 1/60 of a second
		double drawInterval = 1000000000 / FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;

		while (gameThread != null) {

			currentTime = System.nanoTime();

			delta += (currentTime - lastTime) / drawInterval;
			lastTime = currentTime;

			if (delta >= 1) {
				update();
				repaint();
				delta--;
			}
		}

	}

	private void update() { // this handle all of the updating stuff like y and x of the piece

		if (firstMove == true && clock == false) {
			setTimer();
			clock = true;
		}

		if (promotion) {
			promoting();
		} else if (gameover == false && stalemate == false) {

			// MOUSE BUTTON PRESS
			if (mouse.pressed) {
				if (activeP == null) {
					// if the active piece is null, check if you can pick up a piece

					for (Piece piece : simPieces) {
						// if the mouse is on an ally place, pick it up as the activeP
						if (piece.color == currentColor && piece.col == mouse.x / Board.SQUARE_SIZE
								&& piece.row == mouse.y / Board.SQUARE_SIZE) {

							activeP = piece;

						}
					}
				} else {
					// if the player holding a piece, simulate the move
					simulate();
				}
			}
			// MOUSE BUTTON RELEASED ///
			if (mouse.pressed == false) {
				if (activeP != null) {

					if (validSquare) {

						// move confirmed
						startingFlag = false;

						cStartingSquareCol = startingSquareCol;
						cStartingSquareRow = startingSquareRow;

						// update the piece list in case a piece has been captured and remove during the
						// simulation

						copyPieces(simPieces, pieces);

						if (activeP.hittingP != null) {
							capturing = true;
							if (currentColor == WHITE) {
								whiteCaptures.add(activeP.hittingP);
							} else {
								if (currentColor == BLACK) {
									blackCaptures.add(activeP.hittingP);
								}
							}

						} else {

							if (firstMove == false)
								firstMove = true;
						}

						activeP.updatePosition(); // make center on the square

						// tracing ending position
						endingSquareCol = activeP.col;
						
						endingSquareRow = activeP.row;
					

						if (castlingP != null) {
							castlingP.updatePosition();

						}

						if (isKingInCheck() && isCheckMate()) {

							gameover = true;
							if(sound) {
								soundplayer.playSound("res/sounds/checkmate.wav",4.0f);
							}
						

						} else if (isStaleMate() && isKingInCheck() == false) {
							stalemate = true;
						} else { // the game is still going on
							if (canPromote()) {
								promotion = true;

							}

							else {
								if(sound) {
									if (isKingInCheck()) {
										soundplayer.playSound("res/sounds/checksound.wav",4.0f);
									} else {
										if (capturing == true) {
											soundplayer.playSound("res/sounds/capturing.wav",4.0f);
										} else {
											if (castling == true) {
												soundplayer.playSound("res/sounds/castling.wav",4.0f);
											} else {
												soundplayer.playSound("res/sounds/piecemove.wav",4.0f);
											}
										}
									}
								}
								

								changePlayer();
								castling = false;
								capturing = false;

								// memorizing last move
								
								rowMoves.add(9 - endingSquareRow); // Inverti la numerazione della riga

								char file = (char) ('a' + (endingSquareCol - 1)); // Converte la colonna in lettera
								colMoves.add(String.valueOf(file)); // Aggiunge la lettera alla lista

							}

						}
					} else {
						// the move is not valid so reset everything

						copyPieces(pieces, simPieces);
						activeP.resetPosition();
						activeP = null;
						startingFlag = false;

					}

				}
			}
		}

	}

	private void simulate() {

		if (startingFlag == false) {
			startingSquareCol = activeP.col;
			startingSquareRow = activeP.row;
			startingFlag = true;
		}

		canMove = false;
		validSquare = false;

		// reset the piece list every loop
		// this is basically to restore for restoring the removed piece during the
		// simulation
		copyPieces(pieces, simPieces);

		// Reset the castling piece's position
		if (castlingP != null) {
			castlingP.col = castlingP.preCol;
			castlingP.x = castlingP.getX(castlingP.col);
			castlingP = null;
		}

		// if a piece is being held, update its position
		activeP.x = mouse.x - Board.HALF_SQUARE_SIZE; // this is to center the mouse to the piece
		activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
		activeP.col = activeP.getCol(activeP.x);
		activeP.row = activeP.getRow(activeP.y);


		// check if the piece is hovering over a reachable square
		if (activeP.canMove(activeP.col, activeP.row)) {

			canMove = true;

			// if hitting a piece, removing it from a list
			if (activeP.hittingP != null) {
				simPieces.remove(activeP.hittingP.getIndex());
			}

			checkCastling();

			if (isIllegal(activeP) == false && opponentCanCaptureKing() == false) {

				validSquare = true;
			} else {
				if (isIllegal(activeP) == false) {
					System.out.println("illegal");
				}
				if (opponentCanCaptureKing() == false) {
					System.out.println("opponencapture");
				}
			}

		}

	}

	private boolean isIllegal(Piece king) {

		if (king.type == Type.KING) {
						for (Piece piece : simPieces) {
				if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
					return true;
				}
			}
		}

		return false;

	}

	private boolean opponentCanCaptureKing() {

		Piece king = getKing(false);

		for (Piece piece : simPieces) {
			if (piece.color != king.color && piece.canMove(king.col, king.row))
				return true;
		}

		return false;
	}

	private boolean isKingInCheck() {

		Piece king = getKing(true);

		if (activeP.canMove(king.col, king.row)) {

			checkingP = activeP;


			return true;

		} else {
			checkingP = null;
		}

		return false;
	}

	private Piece getKing(boolean opponent) {
		Piece king = null;

		for (Piece piece : simPieces) {
			if (opponent) {
				if (piece.type == Type.KING && piece.color != currentColor) {
					king = piece;
				}
			} else {
				if (piece.type == Type.KING && piece.color == currentColor) {
					king = piece;
				}
			}

		}
		return king;
	}

	private boolean isCheckMate() {

		Piece king = getKing(true);

		if (kingCanMove(king)) {
			return false;
		} else {
			// But you still have a chance
			// check if you can block the attack with your piece

			// check the position of the checking piece and the king in check
			int colDiff = Math.abs(checkingP.col - king.col);
			int rowDiff = Math.abs(checkingP.row - king.row);

			if (colDiff == 0) {
				// The checking piece is attacking vertically

				if (checkingP.row < king.row) {
					// The checking piece is above the king

					for (int row = checkingP.row; row < king.row; row++) {
						for (Piece piece : simPieces) {
							if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}
				}
				if (checkingP.row > king.row) {
					// The checking piece is below the king
					for (int row = checkingP.row; row > king.row; row--) {
						for (Piece piece : simPieces) {
							if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}
				}

			} else if (rowDiff == 0) {
				// The checking piece is attacking horizontally
				if (checkingP.col < king.col) {
					// The checking piece is on left
					for (int col = checkingP.col; col < king.col; col++) {
						for (Piece piece : simPieces) {
							if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}
				if (checkingP.col > king.col) {
					// The checking piece is on right
					for (int col = checkingP.col; col > king.col; col--) {
						for (Piece piece : simPieces) {
							if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}

			} else if (colDiff == rowDiff) {
				// The checking piece is attacking diagonally

				if (checkingP.row < king.row) {

					// The checking piece is above the king
					if (checkingP.col < king.col) {
						// The checking piece is in the upper left
						for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
							for (Piece piece : simPieces) {
								if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
					if (checkingP.col > king.col) {
						// The checking piece is in the upper right
						for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
							for (Piece piece : simPieces) {
								if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
				}
				if (checkingP.row > king.row) {

					// The checking piece is below the king
					if (checkingP.col < king.col) {

						// The checking piece is in the lower left
						for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
							for (Piece piece : simPieces) {
								if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
					if (checkingP.col > king.col) {

						// The checking piece is in the lower right
						for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {

							for (Piece piece : simPieces) {

								if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {

									return false;
								}
							}
						}
					}
				}
			} else {
				// The checking piece is knight (cannot block this)
			}
		}

		return true;
	}

	private boolean kingCanMove(Piece king) {

		// Simulate if there is any square where the king can move to

		if (isValidMove(king, -1, -1)) {
			return true;
		}
		if (isValidMove(king, 0, -1)) {
			return true;
		}
		if (isValidMove(king, 1, -1)) {
			return true;
		}
		if (isValidMove(king, -1, 0)) {
			return true;
		}
		if (isValidMove(king, 1, 0)) {
			return true;
		}
		if (isValidMove(king, -1, 1)) {
			return true;
		}
		if (isValidMove(king, 0, 1)) {
			return true;
		}
		if (isValidMove(king, 1, 1)) {
			return true;
		}

		return false;

	}

	private boolean isValidMove(Piece king, int colPlus, int rowPlus) {

		boolean isValidMove = false;

		// Update the king's position for a second
		king.col += colPlus;
		king.row += rowPlus;

		if (king.canMove(king.row, king.col)) {

			if (king.hittingP != null) {
				simPieces.remove(king.hittingP.getIndex());
			}
			if (isIllegal(king) == false) {
				isValidMove = true;
			}
		}
		// Reset the king position and restore the removed piece
		king.resetPosition();
		copyPieces(pieces, simPieces);

		return isValidMove;
	}

	private boolean isStaleMate() {
		int count = 0;
		// Count the number of pieces
		for (Piece piece : simPieces) {
			if (piece.color != currentColor) {
				count++;
			}
		}

		// if the only one piece (the king) is left
		if (count == 1) {
			if (kingCanMove(getKing(true)) == false) {

				return true;

			}
		}

		return false;
	}

	private void checkCastling() {

		if (castlingP != null) {

			if (castlingP.col == 1) {
				castlingP.col += 3;
			} else if (castlingP.col == 8) {
				castlingP.col -= 2;

			}
			castlingP.x = castlingP.getX(castlingP.col);
			castling = true;
		}
	}

	private void changePlayer() {

		if (currentColor == WHITE) {

			currentColor = BLACK;
			// Reset black's two stepped status
			for (Piece piece : pieces) {
				if (piece.color == BLACK) {
					piece.twoStepped = false;
				}
			}
		} else {
			currentColor = WHITE;
			// Reset white's two stepped status
			for (Piece piece : pieces) {
				if (piece.color == WHITE) {
					piece.twoStepped = false;
				}
			}
		}
		activeP = null;
	}

	private boolean canPromote() {

		if (activeP.type == Type.PAWN) {
			if (currentColor == WHITE && activeP.row == 1) {
				promoPieces.clear();
				promoPieces.add(new Rook(currentColor, activeP.col, 1));
				promoPieces.add(new Knight(currentColor, activeP.col, 2));
				promoPieces.add(new Bishop(currentColor, activeP.col, 3));
				promoPieces.add(new Queen(currentColor, activeP.col, 4));
				return true;
			}
			if (currentColor == BLACK && activeP.row == 8) {
				promoPieces.clear();
				promoPieces.add(new Rook(currentColor, activeP.col, 8));
				promoPieces.add(new Knight(currentColor, activeP.col, 7));
				promoPieces.add(new Bishop(currentColor, activeP.col, 6));
				promoPieces.add(new Queen(currentColor, activeP.col, 5));
				return true;
			}
		}

		return false;
	}

	private void promoting() {

		if (mouse.pressed) {
			for (Piece piece : promoPieces) {
				if (piece.col == mouse.x / Board.SQUARE_SIZE && piece.row == mouse.y / Board.SQUARE_SIZE) {
					switch (piece.type) {
					case ROOK:
						simPieces.add(new Rook(currentColor, activeP.col, activeP.row));
						break;
					case KNIGHT:
						simPieces.add(new Knight(currentColor, activeP.col, activeP.row));
						break;
					case BISHOP:
						simPieces.add(new Bishop(currentColor, activeP.col, activeP.row));
						break;
					case QUEEN:
						simPieces.add(new Queen(currentColor, activeP.col, activeP.row));
						break;
					default:
						break;
					}
					simPieces.remove(activeP.getIndex());
					copyPieces(simPieces, pieces);
					activeP = null;
					promotion = false;
					changePlayer();
				}
			}
		}
	}



	public void setTimer() {
		int sleep = 1000;
		Thread timer = new Thread() {
			@Override
			public void run() {
				while (clock == true) {
					try {
						Thread.sleep(sleep);

						if (currentColor == WHITE) {
							if(whiteTime > 0) {
								
								whiteTime--;
							}
							
						} else {
							if(blackTime > 0) {
								
								blackTime--;
							}
							
						}
						if (blackTime == 0 || whiteTime == 0) {
							if(gameover==false) {
								soundplayer.playSound("res/sounds/checkmate.wav",4.0f);
								gameover = true;
							}
							
						}
					} catch (InterruptedException ex) {
						System.out.println("Errore nel timer");
					}
				}
			}
		};
		timer.start();
	}

	public BufferedImage getImage(String imagePath) {
		try {

			return ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void reset() {
		
		pieces.clear();
		setPieces();
		copyPieces(pieces, simPieces);
		activeP = null;
		
		if(time) {
			whiteTime = 300;
			blackTime = 300;
		}else {
			whiteTime = 600;
			blackTime = 600;
		}
		
		firstMove = false;
		clock = false;
		whiteTime++; // because it passes 1 second before the thread can see that the variable is switched to false, so I need to add 1 second
						 
		whiteCaptures.clear();
		blackCaptures.clear();
		startingSquareCol = 999;
		startingSquareRow = 999;
		endingSquareCol = 999;
		endingSquareRow = 999;
		startingFlag = false;
		cStartingSquareCol = 999;
		cStartingSquareRow = 999;
		currentColor = WHITE;
		colMoves.clear();
		rowMoves.clear();
		gameover = false;
	}

	public void paintComponent(Graphics g) {

		// JComponent method that JPanel inherits and it is used to draw objects on the
		// panel
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g; // we change the g to graphics2d with cast

		if (start) {

			g2.drawImage(getImage("/menu/background"), 0, 0, 1100, 1100, null);

			g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.BOLD, 100));

			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
			g2.setColor(Color.gray);
			g2.fillRoundRect(363, 250, 375, 120, 50, 50);
			g2.fillRoundRect(300, 405, 500, 120, 50, 50);
				
			
			g2.setColor(Color.white);
			g2.drawString("Start", 410, 350);
			g2.drawString("Settings", 330, 500);

			if (mouse.x >= 363 && mouse.x <= (363 + 375) && mouse.y >= 250 && mouse.y <= (250 + 120)) {

				g2.setColor(Color.orange);
				g2.drawString("Start", 410, 350);

				if (mouse.pressed) {
					soundplayer.playSound("res/sounds/click.wav",4.0f);
					start = false;
					menu = true;
					mouse.pressed = false;
				}

			} else {
				if (mouse.x >= 300 && mouse.x <= (300 + 500) && mouse.y >= 405 && mouse.y <= (405 + 120)) {

					g2.setColor(Color.orange);
					g2.drawString("Settings", 330, 500);

					if (mouse.pressed) {
						soundplayer.playSound("res/sounds/click.wav",4.0f);
						start = false;
						settings = true;
						mouse.pressed = false;
					}

				}
			}

		} else {
			if(settings) {
				
				
				
				
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
				g2.setColor(Color.gray);
				g2.fillRoundRect(340, 160, 460, 120, 50, 50);
				g2.fillRoundRect(340, 310, 460, 120, 50, 50);
				g2.fillRoundRect(180, 460, 770, 120, 50, 50);
				
				//time
				g2.fillRoundRect(250, 620, 230, 160, 50, 50);
				g2.fillRoundRect(680, 620, 230, 160, 50, 50);
				
				g2.setColor(Color.white);
				g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.BOLD, 80));
				
				
				g2.drawString("5:00",275,730);
				g2.drawString("10:00",690,730);
				
				
				
				
				
				
				if(sound) {
					g2.drawString("Sound on", 370, 250);
				}else {
					g2.drawString("Sound off", 370, 250);
				}
				
				if(music) {
					g2.drawString("Music on", 360, 400);

				}else {
					g2.drawString("Music off", 360, 400);

				}
					
				g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.BOLD, 70));
				
				if(highlights) {
					g2.drawString("Highlights moves on", 185, 550);
				}else {
					g2.drawString("Highlights moves off", 185, 550);
				}
				
				
				g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.BOLD, 80));		
				
				g2.drawImage(getImage("/menu/backarrow"), -20, 780, 200, 200, null);
				
				
				g2.setColor(Color.orange);
				
				if(time) {
					
					g2.drawString("5:00",275,730);
					
				}else { 
					
						
					g2.drawString("10:00",690,730);
				
					
				}
				
				
				
				if(mouse.x >= 250 && mouse.x <= (250+230) & mouse.y >= 620 && mouse.y <= 620+160){
					if(mouse.pressed) {
						soundplayer.playSound("res/sounds/click.wav",4.0f);
						whiteTime=300;
						blackTime=300;
						time=true;
						mouse.pressed=false;
					}
				}else {
					if(mouse.x >= 680 && mouse.x <= (680+230) & mouse.y >= 620 && mouse.y <= 620+160) {
						if(mouse.pressed) {
							soundplayer.playSound("res/sounds/click.wav",4.0f);
							whiteTime=600;
							blackTime=600;
							time=false;
							mouse.pressed=false;
						}
						
					}
				}
				
				
				
				
				if (mouse.x >= 340 && mouse.x <= 340+460 & mouse.y >= 160 && mouse.y <= 160+120 ) {
					
					
					
					if(sound) {
						
						g2.drawString("Sound on", 370, 250);
					}else {
						g2.drawString("Sound off", 370, 250);
					}
					
					
					
					if(mouse.pressed) {
						soundplayer.playSound("res/sounds/click.wav",4.0f);
						
						if(sound) {
							
							sound = false;
							
						}else {
							
							sound = true;							
						}
						mouse.pressed = false;
					}
					
				}else {
					if(mouse.x >= 340 && mouse.x <= (340+460) && mouse.y >= 310 && mouse.y <= (310+120) ){
						
						
						
						if(music) {
							
							g2.drawString("Music on", 360, 400);
							
						}else {
							g2.drawString("Music off", 360, 400);
						}
						
	
						if(mouse.pressed) {
							
							
							if(music) {
								
								music = false;
								soundplayer.stopSound();
								
							}else {
								
								music = true;
								soundplayer.playSound("res/sounds/music.wav",-10f);
								
							}
							mouse.pressed = false;
						}
					}
					else {
						
						if(mouse.x >= 185 && mouse.x <= (185+770) && mouse.y >= 460 && mouse.y <= (460+120) ){
							g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.BOLD, 70));		
							
							
							if(highlights) {
								
								g2.drawString("Highlights moves on", 185, 550);
								
							}else {
								g2.drawString("Highlights moves off", 185, 550);;
							}
							
							if(mouse.pressed) {
								soundplayer.playSound("res/sounds/click.wav",4.0f);
								
								if(highlights) {
									
									highlights = false;
									
								}else {
									
									highlights = true;
									
								}
								mouse.pressed = false;
							}
							
							
							g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.BOLD, 100));	
						}
					}
				}
				

				
				if (mouse.pressed) {
					if (mouse.x >= -20 && mouse.x <= -20+200 && mouse.y >= 780 && mouse.y <= 780+200) {
						soundplayer.playSound("res/sounds/click.wav",4.0f);
						start = true;
						settings = false;
					}

				}
			}
			else {
				if (menu) {

					g2.drawImage(getImage("/menu/backarrow"), 25, 710, 200, 200, null);
					g2.drawImage(getImage("/menu/classical"), 100, 250, 400, 400, null);
					g2.drawImage(getImage("/menu/advanced"), 600, 250, 400, 400, null);
					g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.BOLD, 45));
					g2.setColor(Color.white);
					g2.drawString("Arcade", 215, 200);
					g2.drawString("Icy Sea", 710, 200);

					g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.BOLD, 100));
					g2.setColor(Color.lightGray);
					g2.drawString("Pick a theme!", 250, 850);
					g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.BOLD, 45));
					g2.setColor(Color.white);

					if (mouse.x >= 100 && mouse.x <= 500 && mouse.y >= 250 && mouse.y <= 650) {
						// classic
						g2.setColor(Color.orange);
						g2.drawString("Arcade", 215, 200);
						if (mouse.pressed) {
							soundplayer.playSound("res/sounds/click.wav",4.0f);
							theme = 0;
							menu = false;

						}

					} else {
						if (mouse.x >= 600 && mouse.x <= 1000 && mouse.y >= 250 && mouse.y <= 650) {
							// advanced
							g2.setColor(Color.orange);
							g2.drawString("Icy Sea", 710, 200);
							if (mouse.pressed) {
								soundplayer.playSound("res/sounds/click.wav",4.0f);
								theme = 1;
								menu = false;
							}

						} else {
							if (mouse.pressed) {
								
								if (mouse.x >= 25 && mouse.x <= 225 && mouse.y >= 710 && mouse.y <= 910) {
									soundplayer.playSound("res/sounds/click.wav",4.0f);
									start = true;
									menu = false;
								}

							}
						}
					}

				} else {

					// piece setting up
					if (pieceCreation == false) {
						setPieces();
						copyPieces(pieces, simPieces);
						pieceCreation = true;
					}

					// BOARD DRAWING
					board.draw(g2);
					
					g2.drawImage(getImage("/menu/backarrow"), -20, 870, 150, 150, null);
					
					
					if(mouse.pressed) {
						if (mouse.x >= -20 && mouse.x <= -20+150 && mouse.y >= 870 && mouse.y <= 870+150) {
							soundplayer.playSound("res/sounds/click.wav",4.0f);
							menu=true;
							reset();
						}
					}
					
					
					
					// square ending and starting squares highligthing in red
					if (cStartingSquareCol != 999 && highlights == true) {
						g2.setColor(Color.red);
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
						g2.fillRect(endingSquareCol * Board.SQUARE_SIZE, endingSquareRow * Board.SQUARE_SIZE,
								Board.SQUARE_SIZE, Board.SQUARE_SIZE);
						g2.fillRect(cStartingSquareCol * Board.SQUARE_SIZE, cStartingSquareRow * Board.SQUARE_SIZE,
								Board.SQUARE_SIZE, Board.SQUARE_SIZE);
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
					
					}

					
					
					//writing moves
					int x = 900;
					int y = 300;
					
					
					
					g2.setColor(Color.LIGHT_GRAY);
					g2.setFont(new Font("Helvetica", Font.PLAIN, 20));
					
					
					
					for (int i = 0; i < rowMoves.size(); i++) {
						
						
						
						if(x==1080) {
							y+=30;
							x=900;
							
						}
						
						if((i%12)==0) {
							g2.setColor(new Color(80, 80, 80));
							g2.fillRect(x, y-17, 200, 20);
							g2.setColor(Color.LIGHT_GRAY);
						}
						
						
						
						
						g2.drawString(colMoves.get(i), x, y);
						x += 10;
						g2.drawString(String.valueOf(rowMoves.get(i)), x, y);
						x += 20;
					}

					// PIECES DRAWING
					for (Piece p : simPieces) {
						p.draw(g2);
					}
					
					
					// CAPTURES DRAWING
					int capturesX = 95;
					int capturesY = 40;
					for (Piece p : blackCaptures) {

						g2.drawImage(p.image, capturesX, capturesY, 60, 60, null);
						capturesX += 45;

					}
					capturesX = 95;
					capturesY = 900;
					for (Piece p : whiteCaptures) {

						g2.drawImage(p.image, capturesX, capturesY, 60, 60, null);
						capturesX += 45;
					}

					
					
					if (activeP != null) {
						if (canMove) {
							if (isIllegal(activeP) || opponentCanCaptureKing()) {
								g2.setColor(Color.gray);

								g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
										Board.SQUARE_SIZE, Board.SQUARE_SIZE);
								g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
							} else {
								g2.setColor(Color.white);
								g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
								g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
										Board.SQUARE_SIZE, Board.SQUARE_SIZE);
								g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
							}

						}

						// Draw the active piece in the end so it won't be hidden by the board or the
						// colored square
						activeP.draw(g2);
					}

					g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.BOLD, 50));

					if (promotion) {
						for (Piece piece : promoPieces) {
							g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row), Board.SQUARE_SIZE,
									Board.SQUARE_SIZE, null);
						}
					}
					g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.PLAIN, 40));
					g2.setColor(Color.white);

					// player times
					if (whiteTime % 60 <= 9) {
						g2.drawString(String.valueOf(whiteTime / 60) + ":" + "0" + String.valueOf(whiteTime % 60), 10, 730);
					} else {
						g2.drawString(String.valueOf(whiteTime / 60) + ":" + String.valueOf(whiteTime % 60), 10, 730);
					}
					if (blackTime % 60 <= 9) {
						g2.drawString(String.valueOf(blackTime / 60) + ":" + "0" + String.valueOf(blackTime % 60), 10, 300);
					} else {
						g2.drawString(String.valueOf(blackTime / 60) + ":" + String.valueOf(blackTime % 60), 10, 300);
					}
				}
			}
		}			
		

		if (gameover) {

			g2.drawImage(getImage("/menu/restart"), 420, 400, 170, 170, null);
			g2.setFont(new Font("JMH Cthulhumbus Arcade", Font.PLAIN, 80));
			g2.setColor(Color.black);
			g2.drawString("Restart", 360, 610);

			if (mouse.pressed) {
				if (mouse.x >= 420 && mouse.x <= 420 + 170 && mouse.y >= 280 && mouse.y <= (400 + 170)) {
					soundplayer.playSound("res/sounds/click.wav",4.0f);
					reset();

				}
			}

		} else {
			if (stalemate) {

				g2.setColor(Color.lightGray);
				g2.drawString("Stalemate", 200, 410);
			}
		}

	}

}
