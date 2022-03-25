package application;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.text.Font;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.control.ButtonBar.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.chart.*;
import javafx.geometry.*;
import javafx.util.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.ImageIO;

public class Main extends Application {
	
	public static int initial_lives = 6;
	private static FlowPane spinroot = null;
	private static HBox b_array_box = null;
	private static Button[] button_array;
	private static GridPane grid, histogrid;
	private static MenuBar menuBar;
	private static Menu application, details;
	private static ImageView application_view, details_view;
	private static Group imagegroup;
	private static MenuItem start, load, create, exit, dictionary, rounds, solution;
	private static StringProperty word_num, points_num, successful;
	private static ObservableList<GameInfo> gamelist;
	private static VBox finbox;
	
	static Game currentGame;
	static Dictionary currentDictionary;
	private TableView<GameInfo> table;
	
	public static class GameInfo {		
        private final SimpleStringProperty word;
        private final SimpleStringProperty guesses;
        private final SimpleStringProperty winner;
        
        private GameInfo(String w, int g, String wr) {
            word = new SimpleStringProperty(w);
            guesses = new SimpleStringProperty(String.valueOf(g));
            winner = new SimpleStringProperty(wr);
        }
        
        private GameInfo() {
        	word = new SimpleStringProperty("EMPTY");
            guesses = new SimpleStringProperty("EMPTY");
            winner = new SimpleStringProperty("EMPTY");
        }
        
        public String getWord() {
            return word.get();
        }
        public void setWord(String w) {
            word.set(w);
        }
        public String getGuesses() {
            return guesses.get();
        }
        public void setGuesses(String g) {
            guesses.set(g);
        }
        public String getWinner() {
            return winner.get();
        }
        public void setWinner(String w) {
            winner.set(w);
        }
	}
	
	public String fileToStylesheetString ( File stylesheetFile ) {
	    try {
	        return stylesheetFile.toURI().toURL().toString();
	    } catch ( MalformedURLException e ) {
	        return null;
	    }
	}
	
	public void end(boolean won) {
		String winner;
		if (won)
			winner = "PLAYER";
		else
			winner = "COMPUTER";
		GameInfo lastgame = new GameInfo(Game.target, Game.correct + initial_lives - Game.lives, winner);
		gamelist.add(0, lastgame);
		gamelist.remove(5);
		currentGame = null;		
		grid.getChildren().remove(b_array_box);
		grid.getChildren().remove(spinroot);
		grid.getChildren().remove(finbox);
		points_num.set("-");
		successful.set("-");
	}
	
	private TextInputDialog load_action(boolean error) {
        TextInputDialog td = new TextInputDialog();
        td.setTitle("Load Dictionary");
        if (!error)
        	td.setHeaderText("Type dictionary ID for local search.");
        else 
        	td.setHeaderText("Cannot find given ID. Type again.");
        td.showAndWait();
        td.close();
        return td;
	}
	
	public void initialize_Game(Stage primaryStage) {
		update_image();
		points_num.set("0");
		successful.set("0%");		
		grid.getChildren().remove(spinroot);		
		grid.getChildren().remove(b_array_box);
		grid.getChildren().remove(finbox);
		button_array = new Button[Game.target.length()]; 
		for (int i = 0; i < Game.target.length(); i++) {
			button_array[i] = new Button(" ");
			button_array[i].setId(String.valueOf(i));
		}
		b_array_box = new HBox();
		b_array_box.getChildren().addAll(button_array);
		grid.add(b_array_box, 0, 2);
		for (Button button : button_array) {
			button.setOnAction(e -> {
				activate_button(button.getId());
			});
		}
		update_probs();
	}
	
	public void initialize_UI(Stage primaryStage) {
		primaryStage.setTitle("MediaLab Hangman");
        grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setMinHeight(400);
        grid.setMinWidth(400);
        grid.setPadding(new Insets(5, 5, 5, 5));
        menuBar = new MenuBar();
        application = new Menu("Application");
        details = new Menu("Details");
        application_view = new ImageView(Main.class.getResource("../appfiles/application.png").toString());
        details_view = new ImageView(Main.class.getResource("../appfiles/details.png").toString()); 
        application_view.setFitWidth(20);
        application_view.setPreserveRatio(true);
        application_view.setSmooth(true);
        application_view.setCache(true);
        details_view.setFitWidth(20);
        details_view.setPreserveRatio(true);
        details_view.setSmooth(true);
        details_view.setCache(true);       
        application.setGraphic(application_view);
        details.setGraphic(details_view);   
        menuBar.getMenus().add(application);
        menuBar.getMenus().add(details);
        //Don't forget to add listeners.
        
        start = new MenuItem("Start");
        load = new MenuItem("Load");
        create = new MenuItem("Create");
        exit = new MenuItem("Exit");
        
        dictionary = new MenuItem("Dictionary");
        rounds = new MenuItem("Rounds");
        solution = new MenuItem("Solution");
        
        application.setOnShowing(e -> { System.out.println("Showing Menu 1"); });
        details.setOnShowing(e -> { System.out.println("Showing Menu 2"); });
        application.getItems().add(start);
        application.getItems().add(load);
        application.getItems().add(create);
        application.getItems().add(exit);
        
        details.getItems().add(dictionary);
        details.getItems().add(rounds);
        details.getItems().add(solution);
        grid.add(menuBar, 0, 0, 1, 1);
        
        Label words = new Label("Words");
        Label words2 = new Label(word_num.toString());
        words2.textProperty().bind(word_num);
        VBox wordsnum = new VBox();
        wordsnum.getChildren().addAll(words, words2);
        grid.add(wordsnum, 2, 0);
        
        Label points = new Label("Points");
        Label points2= new Label(points_num.toString());
        points2.textProperty().bind(points_num);
        VBox pointsnum = new VBox();
        pointsnum.getChildren().addAll(points, points2);
        grid.add(pointsnum, 3, 0);
        
        Label success = new Label("Success");
        Label success2 = new Label(successful.toString());
        success2.textProperty().bind(successful);
        VBox successrate = new VBox();
        successrate.getChildren().addAll(success, success2);
        grid.add(successrate, 4, 0);
        
        grid.setGridLinesVisible(false);         
	}
	
	public void activate_button(String id) {
		int pos = Integer.valueOf(id);
		//Show 
		ObservableList<Character> letters = FXCollections.observableList(Game.sort_by_probability(pos));
		System.out.println(letters);
		Label label = new Label("Select letter");
        final Spinner<Character> spinner = new Spinner<Character>();
        SpinnerValueFactory<Character> valueFactory = 
                new SpinnerValueFactory.ListSpinnerValueFactory<Character>((ObservableList<Character>) letters);
        valueFactory.setWrapAround(true);
        spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        spinner.setValueFactory(valueFactory);              
        spinner.setPrefWidth(74);
        spinner.setEditable(false);
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> {
        	Game.play(pos, spinner.getValue());
        	grid.getChildren().remove(spinroot);
        	successful.set(String.valueOf(Game.correct*100/(Game.correct + initial_lives - Game.lives)) + "%");
        	points_num.set(String.valueOf(Game.points));
        	update_probs();
        	update_image();
        	if (Game.target.charAt(pos) == spinner.getValue()) {
        		button_array[Integer.valueOf(id)].setText(Character.toString(spinner.getValue()));
        		button_array[Integer.valueOf(id)].setDisable(true);
        		if (currentGame.won()) {
        			Alert alert = new Alert(AlertType.INFORMATION);
            		alert.setTitle("You win");
            		alert.setHeaderText("Congratulations, you win");
            		alert.showAndWait();
            		Game.lives = 6;
            		update_image();
        			end(true);
        		}
        	}
        	if (Game.lives <= 0) {
        		Alert alert = new Alert(AlertType.ERROR);
        		alert.setTitle("You lose");
        		alert.setHeaderText("You lose, try again");
        		alert.show();
        		end(false);
        	}
        });
        grid.getChildren().remove(spinroot);
        spinroot = new FlowPane();
        spinroot.setPrefWrapLength(200);
        spinroot.setHgap(10);
        spinroot.setVgap(10);
        spinroot.setPadding(new Insets(10));
        spinroot.getChildren().addAll(label, spinner, okButton);
        grid.add(spinroot, 0, 3);
	}
	
	public void update_image() {

		Image image;
		try {
			grid.getChildren().remove(imagegroup);
			ImageView imageView = new ImageView(Main.class.getResource("../appfiles/hangman" + String.valueOf(Game.lives) + ".png").toString()); 
		    //imageView.setX(50); 
		    //imageView.setY(25); 
		    imageView.setFitHeight(300); 
		    imageView.setFitWidth(300); 
		    imageView.setPreserveRatio(true);  
		    imagegroup = new Group(imageView); 
		    grid.add(imagegroup, 0, 1);
		} catch (Exception e) {
			System.out.println("Cannot find image files");
			e.printStackTrace();
		}  
	}
	
	public void update_probs() {
		grid.getChildren().remove(finbox);
		Label[] letterLabels = new Label[Game.target.length()];
		Label[] stringLabels = new Label[Game.target.length()];
		VBox[] pairs = new VBox[Game.target.length()];
		for (int i = 0; i < Game.target.length(); i++) {
			String probstring = Game.probability_string(i);
			letterLabels[i] = new Label("Letter " + String.valueOf(i + 1) + ":");
			if (!Game.active[i])
				stringLabels[i] = new Label(String.valueOf(Game.target.charAt(i)));
			else
				stringLabels[i] = new Label(probstring);
			pairs[i] = new VBox();
			pairs[i].getChildren().addAll(letterLabels[i], stringLabels[i]);
		}
		Label title = new Label("Letters sorted by probability");
		finbox = new VBox();
		finbox.getChildren().add(title);
		finbox.getChildren().addAll(pairs);
		grid.add(finbox, 1, 1, 4, 3);
	}
	
	public void set_actions(Stage primaryStage) {
		start.setOnAction(e -> {
        	if (currentDictionary == null) {
        		Alert alert = new Alert(AlertType.ERROR);
        		alert.setTitle("Error");
        		alert.setHeaderText("No dictionary loaded");
        		alert.showAndWait();
        	} else {
        		currentGame = new Game(currentDictionary);
        		initialize_Game(primaryStage);
        	}
    	});		
        load.setOnAction(e -> {
        	File f = null;
        	String id = null;
        	TextInputDialog td = null;
        	td = load_action(false);
        	if (td.getResult() != null) {
        		id = td.getEditor().getText();
        		f = new File("hangman_DICTIONARY-" + id + ".txt");
        	}        
        	while(f != null && (!f.exists() || f.isDirectory())) {
        		td = load_action(true);
            	if (td.getResult() != null) {
            		id = td.getEditor().getText();
            		f = new File("hangman_DICTIONARY-" + id + ".txt");
            	} else 
            		break;
        	}
        	if (f!= null && f.exists() && !f.isDirectory()) {
        		try {
					currentDictionary = new Dictionary(id, id, true);
					currentGame = new Game(currentDictionary);
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
				}
        		currentGame = new Game(currentDictionary);
        		word_num.set(String.valueOf(currentDictionary.wordList.size()));
        	}
        });
        create.setOnAction(e -> {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Create new local dictionary");
            ButtonType okbutton = new ButtonType("Continue", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okbutton, ButtonType.CANCEL);

            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(10);
            gridPane.setPadding(new Insets(20, 10, 10, 10));

            TextField dict_id = new TextField();
            dict_id.setPromptText("Dictionary ID");
            TextField lib_id = new TextField();
            lib_id.setPromptText("Open Library ID");

            gridPane.add(new Label("Insert dictionary and library IDs"), 0, 0);
            gridPane.add(dict_id, 0, 1);        
            gridPane.add(lib_id, 1, 1);

            dialog.getDialogPane().setContent(gridPane);

            Platform.runLater(() -> dict_id.requestFocus());

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okbutton) {
                    return new Pair<>(dict_id.getText(), lib_id.getText());
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();

            result.ifPresent(pair -> {
            	try {
					currentDictionary = new Dictionary(pair.getKey(), pair.getValue(), false);
					
            	} catch (InvalidCountException e1) {
					Dialog<ButtonType> duplicates = new Dialog<>();
					duplicates.setTitle("Proceed by removing duplicates");
					ButtonType ok = new ButtonType("Continue", ButtonData.OK_DONE);
		            duplicates.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		            GridPane gridP = new GridPane();
		            gridP.setHgap(10);
		            gridP.setVgap(10);
		            gridP.setPadding(new Insets(20, 10, 10, 10));
		            gridP.add(new Label(e1.getMessage() + ". Would you like to just remove all duplicates?"), 0, 0, 1, 1);
		            duplicates.getDialogPane().setContent(gridP);
		            Optional<ButtonType> pressed = duplicates.showAndWait();
		            pressed.ifPresent(button-> {
		            	if (button != ButtonType.CANCEL){
			            	try {
			            		currentDictionary = new Dictionary(pair.getKey(), pair.getValue(), true);
			            	} catch (Exception e2) {
			            		Alert alert = new Alert(AlertType.ERROR);
			            		alert.setTitle("Error");
			            		alert.setHeaderText(e2.getMessage());
			            		alert.setContentText("Try another library ID");			            		
			            		alert.showAndWait();
			            	}
		            	}
		            });		 
				} catch (Exception e3) {
					Alert alert = new Alert(AlertType.ERROR);
            		alert.setTitle("Error");
            		alert.setHeaderText(e3.getMessage());
            		alert.setContentText("Try another library ID");
            		alert.showAndWait();
				}           
            });
        });
        exit.setOnAction(e -> {
        	primaryStage.close();
        });       
        dictionary.setOnAction(e -> {
        	if (currentDictionary == null) {
        		Alert alert = new Alert(AlertType.ERROR);
        		alert.setTitle("Error");
        		alert.setHeaderText("No dictionary loaded");
        		alert.show();
        	} else {
	        	Stage histostage = new Stage();
	        	histostage.setTitle("Dictionary statistics");
	        	CategoryAxis xAxis = new CategoryAxis();
	            xAxis.setLabel("Letter count");
	            NumberAxis yAxis = new NumberAxis();
	            yAxis.setLabel("Number of words");            
	            BarChart barChart = new BarChart(xAxis, yAxis);
	            XYChart.Series histoseries = new XYChart.Series();
	            histoseries.setName("Current dictionary distribution");
	            HashMap<Integer, Integer> histo = currentDictionary.histogram();
	            for (HashMap.Entry<Integer, Integer> entry : histo.entrySet()) {
	            	histoseries.getData().add(new XYChart.Data(entry.getKey().toString(), entry.getValue()));
	            	System.out.println("Added " + entry.getKey().toString() + "/" + entry.getValue().toString());            	
	            }
	        	barChart.getData().add(histoseries);
	        	VBox vbox = new VBox(barChart);
	        	
	        	histogrid = new GridPane();
	            histogrid.setAlignment(Pos.TOP_RIGHT);
	            histogrid.setHgap(10);
	            histogrid.setVgap(10);
	            histogrid.setPadding(new Insets(25, 25, 25, 25));
	            
	            Label let6 = new Label("6 letters");
	            histogrid.add(let6, 2, 0);
	            Label let6count = new Label(String.valueOf(currentDictionary.percentages[0]) + "%");
	            histogrid.add(let6count, 2, 1);
	            
	            Label let7 = new Label("7-9 letters");
	            histogrid.add(let7, 3, 0);
	            Label let7count = new Label(String.valueOf(currentDictionary.percentages[1]) + "%");
	            histogrid.add(let7count, 3, 1);
	            
	            Label let10 = new Label("10+ letters");
	            histogrid.add(let10, 4, 0);
	            Label let10count = new Label(String.valueOf(currentDictionary.percentages[2]) + "%");
	            histogrid.add(let10count, 4, 1);
	            
	        	StackPane rootPane = new StackPane();
	            Scene scene = new Scene(rootPane, 200, 100);
	            rootPane.getChildren().addAll(vbox, histogrid);
	            histostage.setScene(scene);
	            histostage.setHeight(300);
	            histostage.setWidth(500);
	            histostage.show();    
        	}
        });
        rounds.setOnAction(e -> {
        	 table = new TableView<GameInfo>();
        	 Stage stage = new Stage();
        	 Scene scene = new Scene(new Group());
        	 String fontSheet = fileToStylesheetString( new File ("../application/application.css") );
        	 scene.getStylesheets().add(fontSheet);
             stage.setTitle("Previous game record");
             stage.setWidth(320);
             stage.setHeight(240);
      
             final Label label = new Label("Last 5 games");
             label.setFont(new Font("Arial", 20));
      
             table.setEditable(false);
      
             TableColumn wordCol = new TableColumn("Target word");
             wordCol.setMinWidth(100);
             wordCol.setCellValueFactory(
                     new PropertyValueFactory<GameInfo, String>("word"));
      
             TableColumn guessesCol = new TableColumn("Guesses");
             guessesCol.setMinWidth(100);
             guessesCol.setCellValueFactory(
                     new PropertyValueFactory<GameInfo, String>("guesses"));
      
             TableColumn winnerCol = new TableColumn("Winner");
             winnerCol.setMinWidth(100);
             winnerCol.setCellValueFactory(
                     new PropertyValueFactory<GameInfo, String>("winner"));
      
             table.setItems(gamelist);
             table.getColumns().addAll(wordCol, guessesCol, winnerCol);
      
             final VBox vbox = new VBox();
             vbox.setSpacing(5);
             vbox.setPadding(new Insets(10, 0, 0, 10));
             vbox.getChildren().addAll(label, table);
      
             ((Group) scene.getRoot()).getChildren().addAll(vbox);
      
             stage.setScene(scene);
             stage.show();
        });
        solution.setOnAction(e -> {
        	if (currentGame == null) {
        		Alert alert = new Alert(AlertType.ERROR);
	    		alert.setTitle("Error");
	    		alert.setHeaderText("No game currently active");
	    		alert.show();
        	} else {
	        	Alert alert = new Alert(AlertType.INFORMATION);
	    		alert.setTitle("This round will count as a loss");
	    		alert.setHeaderText("Target word was " + currentGame.target);
	    		alert.show();
	    		currentGame.lives = 6;
	    		update_image();
	    		end(false);	    	
        	}
        });   
	}
	
	@Override
    public void start(Stage primaryStage) {
		
		GameInfo[] initial_g_list = new GameInfo[5];
		for (int i = 0; i < 5; i++) {
			initial_g_list[i] = new GameInfo();
		}
		gamelist = FXCollections.observableArrayList(initial_g_list);
		points_num = new SimpleStringProperty();
		word_num = new SimpleStringProperty();
		successful = new SimpleStringProperty();
		points_num.set("-");
		word_num.set("-");
		successful.set("-");
		
        initialize_UI(primaryStage);
        set_actions(primaryStage);     
        Scene scene = new Scene(grid);
        System.out.println("fff");
        System.out.println(Main.class.getResource("../appfiles/notebook.jpg").toString());
        var image = new Image(Main.class.getResource("../appfiles/notebook.jpg").toString(), true);
        var bgImage = new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(1.0, 1.0, true, true, false, false)
        );
        grid.setBackground(new Background(bgImage));
        primaryStage.setScene(scene);
        primaryStage.setWidth(680);
        primaryStage.setHeight(460);
        primaryStage.show();
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}
