package it.polito.tdp.artsmia;

import it.polito.tdp.artsmia.model.Model;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Artsmia.fxml"));
			BorderPane root = (BorderPane) loader.load();
			Scene scene = new Scene(root);

			ArtsmiaController controller = loader.getController();
			Model model = new Model() ;
			controller.setModel(model) ;

			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
