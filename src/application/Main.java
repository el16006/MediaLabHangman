package application;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.*;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.control.ButtonBar.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.chart.*;
import javafx.geometry.*;
import javafx.util.*;
import java.io.*;
import java.lang.Object.*;
import java.net.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

final class UndersizeException extends Exception {
	public UndersizeException (int size) {
		super("Only " + size + " words found.");
	}
}
final class InvalidCountException extends Exception {
	public InvalidCountException (String word) {
		super("Found duplicate of " + word);
	}
}
final class InvalidRangeException extends Exception {
	public InvalidRangeException (int length) {
		super("Found a word with a length of " + length);
	}
}
final class UnbalancedException extends Exception {
	public UnbalancedException (int count, int size) {
		super("Only " + (int)(count*100/size) + "% of words contain 9 or more letters");
	}
}

public class Main extends Application {
	static Game currentGame;
	static Dictionary currentDictionary;
	
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
	
	@Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MediaLab Hangman");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        MenuBar menuBar = new MenuBar();
        Menu application = new Menu("Application");
        Menu details = new Menu("Details");
        ImageView application_view = new ImageView("file:application.png");
        ImageView details_view = new ImageView("file:details.png");
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
        
        MenuItem start = new MenuItem("Start");
        MenuItem load = new MenuItem("Load");
        MenuItem create = new MenuItem("Create");
        MenuItem exit = new MenuItem("Exit");
        
        MenuItem dictionary = new MenuItem("Dictionary");
        MenuItem rounds = new MenuItem("Rounds");
        MenuItem solution = new MenuItem("Solution");
        
        application.setOnShowing(e -> { System.out.println("Showing Menu 1"); });
        details.setOnShowing(e -> { System.out.println("Showing Menu 2"); });
        application.getItems().add(start);
        application.getItems().add(load);
        application.getItems().add(create);
        application.getItems().add(exit);
        
        details.getItems().add(dictionary);
        details.getItems().add(rounds);
        details.getItems().add(solution);
        
        start.setOnAction(e -> {
        	if (currentDictionary == null) {
        		Alert alert = new Alert(AlertType.ERROR);
        		alert.setTitle("Error");
        		alert.setHeaderText("No dictionary loaded");
        		alert.showAndWait();
        	} else
        		currentGame = new Game(currentDictionary);
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
					currentGame = new Game(currentDictionary);
            	} catch (InvalidCountException e1) {
					Dialog<ButtonType> duplicates = new Dialog<>();
					duplicates.setTitle("Proceed by removing duplicates");
					ButtonType ok = new ButtonType("Continue", ButtonData.OK_DONE);
		            duplicates.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
		            GridPane gridP = new GridPane();
		            gridPane.setHgap(10);
		            gridPane.setVgap(10);
		            gridPane.setPadding(new Insets(20, 10, 10, 10));
		            gridPane.add(new Label(e1.getMessage() + ". Would you like to just remove all duplicates?"), 0, 0);
		            duplicates.getDialogPane().setContent(gridP);
		            Optional<ButtonType> pressed = duplicates.showAndWait();
		            pressed.ifPresent(button-> {
		            	try {
		            		currentDictionary = new Dictionary(pair.getKey(), pair.getValue(), false);
							currentGame = new Game(currentDictionary);		            		
		            	} catch (Exception e2) {
		            		Alert alert = new Alert(AlertType.ERROR);
		            		alert.setTitle("Error");
		            		alert.setHeaderText(e2.getMessage());
		            		alert.setContentText("Try another library ID");
		            		alert.showAndWait();
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
        
        dictionary.setOnAction(e -> {System.out.println("Menu Item 1 Selected");});
        rounds.setOnAction(e -> {System.out.println("Menu Item 1 Selected");});
        solution.setOnAction(e -> {System.out.println("Menu Item 1 Selected");});
        
        grid.add(menuBar, 0, 0, 2, 1);
        Text words = new Text("Words");
        grid.add(words, 0, 1, 2, 1);
        Text wordnum = new Text();
        grid.add(wordnum, 0, 2, 2, 1);
        Text points = new Text("Points");
        grid.add(points, 2, 1, 2, 1);
        Text pointsnum = new Text();
        grid.add(pointsnum, 2, 2, 2, 1);
        Text success = new Text("Success");
        grid.add(success, 4, 1, 2, 1);
        Text successrate = new Text();
        grid.add(successrate, 4, 2, 2, 1);
        grid.setGridLinesVisible(true);
        Scene scene = new Scene(grid);
        primaryStage.setScene(scene);
        StackPane root = new StackPane();
        primaryStage.show();
    }
	
	public static void main(String[] args) {
		currentGame = null;
		currentDictionary = null;
		String id = "OL31390631M"; 
		//currentDictionary = new Dictionary(id, id);
		//currentGame = new Game(id);
		launch(args);
	}
}
