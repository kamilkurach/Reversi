package com.yacotaco;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

/**
 * View
 */
public class View {
    private BorderPane borderPane;
    private final double WIDTH = 75.0;
    private final double HEIGHT = 75.0;
    private final double RADIUS = 27.0;
    private final double MAIN_WIDTH = 1200;
    private final double MAIN_HEIGHT = 800;

    /**
     * @param borderPane organize elements in border layout
     */

    public View(Stage stage) {
        this.borderPane = new BorderPane();
        new BoardGrid();
        stage.setScene(new Scene(borderPane, MAIN_WIDTH, MAIN_HEIGHT));
        stage.show();
    }

    public class BoardGrid {
        private GridPane boardGridPane;
        private StackPane square;

        /**
         * @param boardGridPane 8x8 grid with StackPane object in each cell
         * @param square        StackPane object holds rectangle and circle objects
         */

        public BoardGrid() {
            this.boardGridPane = new GridPane();
            initBordView();
        }

        private void initBordView() {
            boardGridPane.setAlignment(Pos.CENTER);
            int rowNum = 8;
            int colNum = 8;
            for (int row = 0; row < rowNum; row++) {
                for (int col = 0; col < colNum; col++) {
                    square = new StackPane();
                    if ((row + col) % 2 == 0) {
                        Color lightGreen = Color.web("#00cc00", 1.0);
                        square.getChildren().addAll(new Rectangle(WIDTH, HEIGHT, lightGreen));
                    } else {
                        Color darkGreen = Color.web("#008000", 1.0);
                        square.getChildren().addAll(new Rectangle(WIDTH, HEIGHT, darkGreen));
                    }
                    boardGridPane.add(square, col, row);
                }
            }
            borderPane.setCenter(boardGridPane);
        }

        public GridPane getBoardGridPane() {
            return boardGridPane;
        }

        public void setBoardGridPane(GridPane boardGridPane) {
            this.boardGridPane = boardGridPane;
        }

        public Rectangle validMoveMarker() {
            Rectangle rectangle = new Rectangle(WIDTH, HEIGHT, Color.web("#EE4540", 0.25));
            rectangle.setStroke(Color.web("#EE4540", 1.0));
            rectangle.setStrokeWidth(3);
            rectangle.setStrokeType(StrokeType.INSIDE);
            return rectangle;
        }
    }

    public class DiscView {

        public Circle makeDisc(Integer discState) {
            Circle circle = new Circle();
            if (discState == 0) {
                // white disc
                circle.setCenterX(WIDTH);
                circle.setCenterY(HEIGHT);
                circle.setRadius(RADIUS);
                circle.setFill(Color.WHITE);
            } else if (discState == 1) {
                // black disc
                circle.setCenterX(WIDTH);
                circle.setCenterY(HEIGHT);
                circle.setRadius(RADIUS);
                circle.setFill(Color.BLACK);
            }
            return circle;
        }
    }
}
