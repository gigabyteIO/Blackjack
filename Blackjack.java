import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Blackjack extends Application{
	
	public static void main(String[] args) {
        launch(args);
    }

    //---------------------------------------------------------------------

	private static final int CARD_WIDTH = 90;   // Each card image is 90 pixels wide.
	private static final int CARD_HEIGHT = 126; // Each card image is 126 pixels tall.
	
	private Button hitButton;		// Button used to deal the player another card.
	private Button standButton;		// Button used to tell the game to deal the dealer cards.
	private Button newGameButton;	// Button used to create a new game by shuffling deck, clearing hands, and enabling/disabling buttons.
	private TextField betInput;		// Input where the player places their bet.
	
	private String totalMoney;		// The total money the player has to bet with.
	private String blackJackPrompt;	// Prompt that tells the player the states of the game.
	
	private boolean gameInProgress;		// Tracks if there is a game in progress.
	
	private Deck deck;					// The deck of 52 cards dealt to the player/dealer.
	private BlackjackHand playerHand;	// The players Blackjack hand.
	private BlackjackHand dealerHand;	// The dealers Blackjack hand.
	
	private Canvas board;     // The canvas on which cards and message are drawn.
	private GraphicsContext g;// The graphics context used to draw on the canvas.
    private Image cardImages; // A single image contains the images of every card.
	
	
	public void start(Stage stage) throws Exception {
		cardImages = new Image("cards.png");
		board = new Canvas(CARD_WIDTH * 8, CARD_HEIGHT * 4);
		g = board.getGraphicsContext2D();
	
		deck = new Deck();
		playerHand = new BlackjackHand();
		dealerHand = new BlackjackHand();
		
		BorderPane root = new BorderPane();
		root.setCenter(board);
		root.setBottom(makeBottom());
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Blackjack");
		stage.setResizable(false);
		stage.show();	
		
		doNewGame();
	}

	private void drawBoard() {
		// Fills background with green and gives a dark red border 3 pixels wide.
		g.setFill(Color.DARKRED);
		g.fillRect(0, 0, board.getWidth(), board.getHeight());
		
		g.setFill(Color.DARKGREEN);
		g.fillRect(3, 3, board.getWidth() - 6, board.getHeight());
		
		g.setFill(Color.ANTIQUEWHITE);
		g.setFont(new Font(20));
		g.fillText("Dealer's Cards:", 50, 50);
		
		g.fillText("Your Cards:", 50, 100 + CARD_HEIGHT);
		
		g.fillText(blackJackPrompt, 50, (3.5 * CARD_HEIGHT));
		
		/* Dealer's cards. */
		if(!gameInProgress) {
			for(int i = 0; i < dealerHand.getCardCount(); i++) {
				drawCard(g, dealerHand.getCard(i), 50 + (i * CARD_WIDTH), 70);
			}
		}
		else {
			drawCard(g, null, 50, 70);
			drawCard(g, dealerHand.getCard(1), 50 + CARD_WIDTH, 70);		
		}
		
		/* Player's cards. */
		for(int i = 0; i < playerHand.getCardCount(); i++) {
			drawCard(g, playerHand.getCard(i), 50 + (i * CARD_WIDTH), 120 + CARD_HEIGHT);
		}
	}
	
	 /**
     * Creates the bottom panel that holds the button for playing a new game and sets up Action listeners.
     * @return An HBox containing the buttons to control the game.
     */
    private HBox makeBottom() {
		
    	newGameButton = new Button("New Game");
    	newGameButton.setDisable(true);
    	hitButton = new Button("Hit!"); 	
    	standButton = new Button("Stand!");
    	
    	newGameButton.setOnAction( e -> {
    		doNewGame();
    	});
    	
    	hitButton.setOnAction( e -> {
    		doHit();
    	});
    	
    	standButton.setOnAction( e -> {
    		doStand();
    	});

    	HBox bottomBar = new HBox( hitButton, standButton, newGameButton );
    	bottomBar.setAlignment(Pos.CENTER);
    	bottomBar.setSpacing(5);
    	bottomBar.setStyle( // CSS styling for the HBox
    			"-fx-border-width: 3px; -fx-border-color: darkred; -fx-padding: 8px; -fx-background-color: beige" );

    	return bottomBar;	
    }

    /**
     * Standing means the player has chosen to not receive any more cards.
     * The game is set to over and the dealer is dealt cards until they hit or go over
     * 16.
     */
	private void doStand() {	
		
		setGameInProgress(false);
		
		while(dealerHand.getBlackjackValue() < 16) {
			dealerHand.addCard(deck.dealCard());
		}
		
		if(dealerHand.getBlackjackValue() > 21) {
			blackJackPrompt = "Dealer went over 21. You win!";
		}
		else if(dealerHand.getBlackjackValue() == playerHand.getBlackjackValue()) {
			blackJackPrompt = "You have " + playerHand.getBlackjackValue() + "." + 
					 		 " The dealer has " + dealerHand.getBlackjackValue() + 
					 		 ". Dealer wins on ties! You lose!";
		}
		else if(dealerHand.getBlackjackValue() < playerHand.getBlackjackValue()) {
			blackJackPrompt = "You have " + playerHand.getBlackjackValue() + "." + 
							 " The dealer has " + dealerHand.getBlackjackValue() + ". You win!";
		}
		else if(dealerHand.getBlackjackValue() > playerHand.getBlackjackValue()) {
			blackJackPrompt = "You have " + playerHand.getBlackjackValue() + "." + 
							 " The dealer has " + dealerHand.getBlackjackValue() + ". You lose!";
		}
		
		drawBoard();
	}

	/**
	 * Hitting means the player would like another card.
	 */
	private void doHit() {
		playerHand.addCard(deck.dealCard());	
		
		if(playerHand.getBlackjackValue() <= 21) {
			blackJackPrompt = "You have " + playerHand.getBlackjackValue() + ". Hit or Stand?";
		}
		else if(playerHand.getBlackjackValue() > 21) {
			blackJackPrompt = "You have " + playerHand.getBlackjackValue() + ". You lose!";
			setGameInProgress(false);
		}
		
		drawBoard();
	}

	/**
	 * Enables or disables buttons based on if there is a game in progress.
	 * @param inProgress True if there is a game in progress, false otherwise.
	 */
	private void setGameInProgress(boolean inProgress) {
		
		gameInProgress = inProgress;
		
		if(gameInProgress) {
			newGameButton.setDisable(true);
			hitButton.setDisable(false);
			standButton.setDisable(false);
		}
		else {
			newGameButton.setDisable(false);
			hitButton.setDisable(true);
			standButton.setDisable(true);
		}	
	}
	
	/**
	 * Shuffles deck, clears hands, and redraws the boards for a new game.
	 */
	private void doNewGame() {
		deck.shuffle();
		playerHand.clear();
		dealerHand.clear();	
		
		/* Deal two cards to dealer and player. */
		dealerHand.addCard(deck.dealCard());	
		dealerHand.addCard(deck.dealCard());
		
		playerHand.addCard(deck.dealCard());
		playerHand.addCard(deck.dealCard());
		
		/** Checks if dealer or player has a Blackjack **/
		if(dealerHand.getBlackjackValue() == 21) {			
			setGameInProgress(false);
			blackJackPrompt = "The dealer has 21! You lose!";
		}
		else if(playerHand.getBlackjackValue() == 21 && dealerHand.getBlackjackValue() != 21) {
			setGameInProgress(false);
			blackJackPrompt = "You have 21! You win!";
		}
		else {
			setGameInProgress(true);
			blackJackPrompt = "You have " + playerHand.getBlackjackValue() + ". Hit or Stand?";
		}
		
		
		drawBoard();
	}
	
	/**
     * Draws a card with top-left corner at (x,y).  If card is null,
     * then a face-down card is drawn.  The card images are from 
     * the file cards.png; this program will fail without it.
     */
    private void drawCard(GraphicsContext g, Card card, int x, int y) {
        int cardRow, cardCol;
        if (card == null) {  
            cardRow = 4;   // row and column of a face down card
            cardCol = 2;
        }
        else {
            cardRow = 3 - card.getSuit();
            cardCol = card.getValue() - 1;
        }
        double sx,sy;  // top left corner of source rect for card in cardImages
        sx = 79 * cardCol;
        sy = 123 * cardRow;
        g.drawImage( cardImages, sx,sy,79,123, x,y,79,123 );
    } // end drawCard()
}
