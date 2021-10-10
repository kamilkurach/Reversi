package com.yacotaco;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.WritableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller class.
 *
 * @author Kamil Kurach
 * @author https://github.com/yacotaco
 * @version 1.0
 */
public class Controller {
    /**Board class object. */
    private Board board;
    /** View class object. */
    private View view;
    /**Stage class object. */
    private Stage stage;
    /** BoardGrid class object. Part of View class. */
    private View.BoardGrid bg;
    /** DiscView class object. Part of View class. */
    private View.DiscView dv;
    /** SummaryView class object. Part of View class. */
    private View.SummaryView sv;
    /** Player class object. */
    private Player playerOne;
    /** Player class object. */
    private Player playerTwo;
    /** 0 - white player, 1 - black player. */
    private Integer playerTurn;
    /** Timeline class object for game timer. */
    private Timeline timeline;
    /** Flag for game timer. */
    private Boolean isTimerOn;
    /** Timer time value in milliseconds. */
    private final Double turnTime = 30000.0;
    /** Player turn on new game init. */
    private final Integer initPlayerTurn = 0;
    /** Debug marker for flipped discs. */
    private final Boolean debugMarker = true;
    /** Debug marker for valid moves. */
    private final Boolean moveMarker = true;
    /** Flag for AI Player. */
    private Boolean aiPlayer;
   

    /** Controller constructor.
     *
     * @param boardClass Board class
     * @param viewClass  View class
     * @param stageClass JavaFX container
     */
    public Controller(final Board boardClass, final View viewClass,
            final Stage stageClass) {
        this.board = boardClass;
        this.view = viewClass;
        this.stage = stageClass;
        this.bg = view.new BoardGrid();
        this.dv = view.new DiscView();
        this.playerOne = new Player();
        this.playerTwo = new Player();
        this.isTimerOn = false;
        this.aiPlayer = false;
        initController();
    }

    // ************** INITS **************

    /** Inits all handlers. */
    private void initController() {
        onGridClick();
        onExitButtonClick();
        onNewGameButtonClick();
        onSaveButtonClick();
        onLoadButtonClick();
        onTimerButtonClick();
        onAiPlayerButtonClick();
    }

    /** Inits both players names and states (disc color).*/
    private void initPlayer() {
        playerOne.setDiscState(0);
        playerOne.setName("A");
        playerTwo.setDiscState(1);
        playerTwo.setName("B");
    }

    // ************** HELPER FUNCTIONS **************

    /** Sets player turn.
     *
     * @param value 0 - white, 1 - black
     */
    private void setPlayerTurn(final Integer value) {
        this.playerTurn = value;
    }

    /** Changes player turn. */
    private void changePlayerTurn() {
        if (playerTurn == 0) {
            setPlayerTurn(1);
        } else if (playerTurn == 1) {
            setPlayerTurn(0);
        }
    }

    /** Counts points for given player.
     *
     * @param player Player class object.
     */
    private void countPlayerPoints(final Player player) {
        Integer discState = player.getDiscState();
        player.setPoints(board.getAllPlayerDiscs(discState).size());
    }

    /** Gets date and time.
     *
     * @return string with date and time.
     */
    private String getDateTime() {
        String format = "yyyy-MM-dd_HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime localDateTime = LocalDateTime.now();
        String formatDateTime = localDateTime.format(formatter);
        return formatDateTime;
    }

    /** Adds summary to main window.
     *
     * @param whitePlayer Player class object.
     * @param blackPlayer Player class object.
     */
    private void addSummary(final Player whitePlayer,
        final Player blackPlayer) {
        final int index = 3;
        if (timeline != null) {
            timeline.pause();
        }
        sv = view.new SummaryView(whitePlayer, blackPlayer);
        StackPane summary = sv.getSummary();
        Node node = view.getBorderPane().getCenter();
        StackPane sp = (StackPane) node;
        sp.getChildren().add(index, summary);
    }

    /** Removes summary from main window. */
    private void removeSummary() {
        final int maxElements = 4;
        final int summaryElementIndex = 3;
        Node node = view.getBorderPane().getCenter();
        StackPane sp = (StackPane) node;
        if (sp.getChildren().size() == maxElements) {
            sp.getChildren().remove(summaryElementIndex);
        }
    }

    /** Generates random move for opponent. */
    private void randomMoveGenerator() {
        if (aiPlayer.equals(true) && playerTurn.equals(1)) {
            int max = board.getAllValidMoves().size();
            int random = (int) (Math.random() * max);
            Integer[] move = board.getAllValidMoves().get(random);
            runOnClick(move[0], move[1]);
        }
    }

    /** Writes board states and player turn to file.
     *
     * @param file file class object.
     * @param boardGrid representation of board in 2d array.
     * @throws IOException exception file writer.
     */
    private void writeBoardStateToFile(final File file,
     final Disc[][] boardGrid) throws IOException {
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);

        for (int row = 0; row < boardGrid.length; row++) {
            for (int col = 0; col < boardGrid[row].length; col++) {
                Disc disc = board.getDiscFromBoard(row, col);
                String s = row + "," + col + "," + disc.getState() + "\n";
                bw.write(s);
            }
        }

        String playerTurnString = Integer.toString(playerTurn);
        bw.write(playerTurnString);
        bw.close();
    }

    // ************** VIEW UPDATE **************

    /** Updates view of all elements in main window.
     *
     */
    private void updateBoardView() {

        switchOnNoValidMoves();

        for (Node square : bg.getBoardGridPane().getChildren()) {
            Integer col = bg.getBoardGridPane().getColumnIndex(square);
            Integer row = bg.getBoardGridPane().getRowIndex(square);
            Integer discState = board.getDiscFromBoard(row, col).getState();
            StackPane sp = (StackPane) square;

            if (sp.getChildren().size() == 2) {
                sp.getChildren().remove(1);
            }

            if (debugMarker.equals(true)) {
                View.DebugMarkers dm = view.new DebugMarkers();
                sp.getChildren().add(dv.makeDisc(discState));
                for (Disc disc : board.getFlipedDiscsToMark()) {
                    int discRow = disc.getRow();
                    int discCol = disc.getCol();
                    if (discRow == row && discCol == col) {
                        StackPane spWithMarker = new StackPane();
                        sp.getChildren().remove(1);
                        spWithMarker.getChildren().addAll(
                            dv.makeDisc(discState),
                            dm.flipDebugMarker());
                        sp.getChildren().add(spWithMarker);
                    }
                }
            } else if (debugMarker.equals(false)) {
                sp.getChildren().add(dv.makeDisc(discState));
            }

            if (moveMarker.equals(true)) {
                for (Integer[] move : board.getAllValidMoves()) {
                    int validMoveRow = move[0];
                    int validMoveCol = move[1];
                    if (row == validMoveRow && col == validMoveCol) {
                        sp.getChildren().add(bg.validMoveMarker());
                    }
                }
            }

            if (sp.getChildren().size() > 2) {
                sp.getChildren().remove(1, sp.getChildren().size() - 1);
            }
        }
        updatePointsCounters();

        updatePlayerTurnIndicators();

        if (isTimerOn.equals(true)) {
            setGameTimer();
        }

        if (board.getAllValidMoves().isEmpty()) {
            addSummary(playerOne, playerTwo);
        }
        
        board.clearFlipedDiscsToMark();
    }

    /** Resets game timer view. */
    private void resetTimerViewOnTimelineStop() {
        // reset timer view
        if (playerTurn == 0) {
            view.getTopBorderPane().getTimerViewWhite().setTimerValue("0");
            view.getTopBorderPane().getTimerViewWhite().removeHighlight();
        } else if (playerTurn == 1) {
            view.getTopBorderPane().getTimerViewBlack().setTimerValue("0");
            view.getTopBorderPane().getTimerViewBlack().removeHighlight();
        }
    }

    /** Updates indicator for current player. */
    private void updatePlayerTurnIndicators() {
        final int maxElements = 3;
        int elementsInWhiteCounter = view.getTopBorderPane().getWhiteCounter()
            .getChildren().size();
        int elementsInBlackCounter = view.getTopBorderPane().getBlackCounter()
            .getChildren().size();

        if (playerTurn == 0) {
            view.getTopBorderPane().getWhiteCounter().getChildren()
                .add(2, dv.makePlayerIndicator());
        } else if (playerTurn == 1) {
            view.getTopBorderPane().getBlackCounter().getChildren()
                .add(2, dv.makePlayerIndicator());
        }

        if (elementsInWhiteCounter == maxElements) {
            view.getTopBorderPane().getWhiteCounter().getChildren().remove(2);
        } else if (elementsInBlackCounter == maxElements) {
            view.getTopBorderPane().getBlackCounter().getChildren().remove(2);
        }
    }

    /** Updates counter for current player. */
    private void updatePointsCounters() {
        countPlayerPoints(playerOne);
        countPlayerPoints(playerTwo);
        // update white disc
        Node nodeWhite = view.getTopBorderPane().getWhiteCounter()
            .getChildren().get(1);
        Text textWhite = (Text) nodeWhite;
        textWhite.setText(Integer.toString(playerOne.getPoints()));

        // update black disc
        Node nodeBlack = view.getTopBorderPane().getBlackCounter()
            .getChildren().get(1);
        Text textBlack = (Text) nodeBlack;
        textBlack.setText(Integer.toString(playerTwo.getPoints()));
        view.highLightPoints(textWhite, textBlack, playerTurn);
    }

    // ************** TIMER **************

    /** Resets game timer. */
    private void resetTimer() {
        timeline.stop();
        resetTimerViewOnTimelineStop();
    }

    /** Organizes all game actions around timeline. */
    private void setGameTimer() {
        /** Time in seconds before timer changes color to red. */
        final int timeoutSec = 10;
        timeline.setCycleCount(1);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(turnTime),
            new KeyValue(new WritableValue<Integer>() {

            @Override
            public Integer getValue() {
                return null;
            }

            @Override
            public void setValue(final Integer value) {
                if (playerTurn == 0) {
                    Duration currentTime = timeline.getCurrentTime();
                    Duration totoalTurnTime = timeline.getTotalDuration();
                    int seconds = (int) (totoalTurnTime.toSeconds()
                        - currentTime.toSeconds());

                    view.getTopBorderPane().getTimerViewWhite()
                        .setTimerValue(Integer.toString(seconds));
                    view.getTopBorderPane().getTimerViewBlack()
                        .setTimerValue("0");
                    view.getTopBorderPane().getTimerViewBlack()
                        .removeHighlight();

                    if (seconds > timeoutSec) {
                        view.getTopBorderPane().getTimerViewWhite()
                            .addHighlight();
                    } else {
                        view.getTopBorderPane().getTimerViewWhite()
                            .timeoutHighlight();
                    }

                } else if (playerTurn == 1) {
                    Duration currentTime = timeline.getCurrentTime();
                    Duration totoalTurnTime = timeline.getTotalDuration();
                    int seconds = (int) (totoalTurnTime.toSeconds()
                         - currentTime.toSeconds());

                    view.getTopBorderPane().getTimerViewBlack()
                        .setTimerValue(Integer.toString(seconds));
                    view.getTopBorderPane().getTimerViewWhite()
                        .setTimerValue("0");
                    view.getTopBorderPane().getTimerViewWhite()
                        .removeHighlight();

                    if (seconds > timeoutSec) {
                        view.getTopBorderPane().getTimerViewBlack()
                            .addHighlight();
                    } else {
                        view.getTopBorderPane().getTimerViewBlack()
                            .timeoutHighlight();
                    }
                }
            }

        }, null)));

        timeline.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent event) {
                if (playerTurn == 0) {
                    view.getTopBorderPane().getTimerViewWhite()
                        .removeHighlight();
                    changePlayerTurn();
                    board.getValidMoves(playerTurn);
                    updateBoardView();

                } else if (playerTurn == 1) {
                    view.getTopBorderPane().getTimerViewBlack()
                        .removeHighlight();
                    changePlayerTurn();
                    board.getValidMoves(playerTurn);
                    updateBoardView();
                }
            }
        });

        timeline.play();
    }

    
    /** Switches player if there is no valid move. */
    private void switchOnNoValidMoves() {
        // switch player if there are no valid moves
        if (board.getAllValidMoves().isEmpty()) {
            changePlayerTurn();
            updatePointsCounters();
            updatePlayerTurnIndicators();
            board.getValidMoves(playerTurn);
        }
    }

    /**  Checks if placed move is on list.
     *
     * @param row row coordinate.
     * @param col column coordiante.
     * @return boolean value.
     */
    private boolean validatePlacedMove(final Integer row, final Integer col) {
        boolean result = false;
        for (Integer[] move : board.getAllValidMoves()) {
            int validMoveRow = move[0];
            int validMoveCol = move[1];
            if (row == validMoveRow && col == validMoveCol) {
                result = true;
            }
        }
        return result;
    }

    // ************** CLICK HANDLERS **************

    /** Runs game updates after placed move.
     *
     * @param row row coordinates.
     * @param col column coordinates.
     */
    private void runOnClick(final Integer row, final Integer col) {

        boolean validMove = validatePlacedMove(row, col);

        // player can place disc only on empty square
        if (Boolean.TRUE.equals(validMove)) {
            board.modifyDiscState(row, col, playerTurn);
            board.flipHorizontalDiscs(row, col, playerTurn);
            board.flipVerticalDiscs(row, col, playerTurn);
            board.flipDiagonalDiscs(row, col, playerTurn);

            if (isTimerOn.equals(true)) {
                resetTimer();
            }

            // change player after update
            changePlayerTurn();

            board.getValidMoves(playerTurn);

            updateBoardView();
        }
    }

    /** Click handler for placed move. */
    private void onGridClick() {
        bg.getBoardGridPane().getChildren().forEach(square -> {
            square.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    Node node = (Node) event.getSource();
                    Integer col = bg.getBoardGridPane().getColumnIndex(node);
                    Integer row = bg.getBoardGridPane().getRowIndex(node);

                    runOnClick(row, col);
                    randomMoveGenerator();

                }
            });
        });
    }

    /** Exit button click handler. */
    private void onExitButtonClick() {
        view.getTopBorderPane().getExitButton()
            .setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {

                if (timeline != null) {
                    timeline.pause();
                }

                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setContentText("Do you want to exit game?");
                Optional<ButtonType> option = alert.showAndWait();
                boolean buttonType = ButtonType.OK.equals(option.get());
                if (Boolean.TRUE.equals(buttonType)) {
                    System.exit(0);
                } else {
                    if (timeline != null && isTimerOn.equals(true)) {
                        timeline.play();
                    }
                }

            }
        });
    }

    /** New game button click handler. */
    private void onNewGameButtonClick() {
        view.getTopBorderPane().getNewGameButton()
            .setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(final MouseEvent event) {

                if (isTimerOn.equals(true)) {
                    if (timeline != null) {
                        timeline.stop();
                        timeline = new Timeline();
                    } else {
                        timeline = new Timeline();
                    }
                } else if (isTimerOn.equals(false)) {
                    timeline = new Timeline();
                    timeline.pause();
                }

                initPlayer();
                setPlayerTurn(initPlayerTurn);
                board.initBoard();
                board.getValidMoves(playerTurn);
                updateBoardView();
                removeSummary();
                aiPlayer = false;
            }
        });
    }

    /** Save button click handler. */
    private void onSaveButtonClick() {
        view.getTopBorderPane().getSaveButton()
            .setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(final MouseEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName("REVERSI_GAME_SAVE_"
                     + getDateTime());
                Disc[][] boardGrid = board.getBoardGrid();

                if (timeline != null) {
                    timeline.pause();
                }

                try {
                    File file = fileChooser.showSaveDialog(stage);
                    if (file != null) {

                        writeBoardStateToFile(file, boardGrid);

                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setContentText("File saved!");

                        Optional<ButtonType> option = alert.showAndWait();
                        boolean buttonType = ButtonType.OK.equals(option.get());
                        if (Boolean.TRUE.equals(buttonType)) {
                            if (timeline != null && isTimerOn.equals(true)) {
                                timeline.play();
                            }
                        }
                    } else {
                        if (timeline != null && isTimerOn.equals(true)) {
                            timeline.play();
                        }
                    }
                } catch (IOException | NullPointerException e) {
                    if (e.getMessage() == null) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setContentText("Board is empty!");
                        alert.show();
                    } else {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setContentText("Can't save file!");
                        alert.show();
                    }
                }
            }
        });
    }

    /** Load button click handler. */
    private void onLoadButtonClick() {
        view.getTopBorderPane().getLoadButton()
            .setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(final MouseEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Game File");

                if (timeline != null) {
                    timeline.pause();
                }
                // loading file when previous game ended
                removeSummary();

                try {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        FileReader fr = new FileReader(file);
                        BufferedReader br = new BufferedReader(fr);
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (line.length() > 1) {
                                String[] splitLine = line.split(",");
                                int row = Integer.valueOf(splitLine[0]);
                                int col = Integer.valueOf(splitLine[1]);
                                int discState = Integer.valueOf(splitLine[2]);
                                board.getDiscFromBoard(row, col)
                                    .setState(discState);
                            } else {
                                int playerState = Integer.valueOf(line);
                                setPlayerTurn(playerState);
                            }
                        }
                        br.close();

                        if (timeline != null) {
                            resetTimer();
                            if (isTimerOn.equals(false)) {
                                view.getTopBorderPane().getTimerViewWhite()
                                    .switchOffTimer();
                                view.getTopBorderPane().getTimerViewBlack()
                                    .switchOffTimer();
                            }
                            board.getValidMoves(playerTurn);
                            updateBoardView();
                        } else {
                            timeline = new Timeline();
                            initPlayer();
                            board.getValidMoves(playerTurn);
                            updateBoardView();
                        }
                    } else {
                        if (timeline != null && isTimerOn.equals(true)) {
                            timeline.play();
                        }
                    }
                } catch (NumberFormatException | IOException
                    | NullPointerException e) {

                    if (e.getMessage() == null) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setAlertType(AlertType.ERROR);
                        alert.setContentText("File is empty!");
                        alert.show();
                    } else {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setAlertType(AlertType.ERROR);
                        alert.setContentText("Can't read file!");
                        alert.show();
                    }
                }
            }
        });
    }

    /** Timed game button click handler. */
    private void onTimerButtonClick() {
        view.getTopBorderPane().getNewTimedGameButton()
            .setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(final MouseEvent event) {
                isTimerOn = true;
                view.getTopBorderPane().getTimerViewWhite().setTimerValue("0");
                view.getTopBorderPane().getTimerViewBlack().setTimerValue("0");
                view.getTopBorderPane().getTimerViewWhite().switchOnTimer();
                view.getTopBorderPane().getTimerViewBlack().switchOnTimer();

                if (event.getClickCount() == 2 && timeline != null) {
                    isTimerOn = false;
                    timeline.stop();
                    view.getTopBorderPane().getTimerViewWhite()
                        .switchOffTimer();
                    view.getTopBorderPane().getTimerViewBlack()
                        .switchOffTimer();
                } else if (event.getClickCount() == 2) {
                    isTimerOn = false;
                    view.getTopBorderPane().getTimerViewWhite()
                        .switchOffTimer();
                    view.getTopBorderPane().getTimerViewBlack()
                        .switchOffTimer();
                }
            }

        });
    }

    /** AI Player button click handler. */
    private void onAiPlayerButtonClick() {
        view.getTopBorderPane().getAiPlayerButton()
            .setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(final MouseEvent event) {
                aiPlayer = true;
            }

        });
    }
}
