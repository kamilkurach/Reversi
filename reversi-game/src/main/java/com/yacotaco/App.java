package com.yacotaco;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Board board = new Board();
        View view = new View(stage);
        Controller controller = new Controller(board, view, stage);
    }

    public static void main(String[] args) {
        launch();
    }

}