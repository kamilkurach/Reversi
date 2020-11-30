package com.yacotaco;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
 * Controller
 */
public class Controller {
    private Board board;
    private View view;
    private Stage stage;
    private View.BoardGrid bg;
    private View.DiscView dv;
    private View.SummaryView sv;
    private Player playerOne;
    private Player playerTwo;
    private Integer playerTurn;
    private Timeline timeline;
    private Boolean isTimerSwitched;
    private final Double TURN_TIME = 30000.0;
    private final Integer initPlayerTurn = 0;
    private final Boolean DEBUG_MARKER = true;
    private final Boolean MOVE_MARKER = true;
    private final Boolean AI_PLAYER = false;
    private ArrayList<Integer[]> allValidMoves = new ArrayList<Integer[]>();
    private ArrayList<Disc> flipedDiscsToMark = new ArrayList<Disc>();

    /**
     * @param board Board class
     * @param view  View class
     * @param stage JavaFX container
     */

    public Controller(Board board, View view, Stage stage) {
        this.board = board;
        this.view = view;
        this.stage = stage;
        this.bg = view.new BoardGrid();
        this.dv = view.new DiscView();
        this.playerOne = new Player();
        this.playerTwo = new Player();
        this.isTimerSwitched = false;
        initController();
    }

    // ************** INITS **************

    private void initController() {
        onGridClick();
        onExitButtonClick();
        onNewGameButtonClick();
        onSaveButtonClick();
        onLoadButtonClick();
        onTimerButtonClick();
    }

    private void initPlayer() {
        playerOne.setDiscState(0);
        playerOne.setName("A");
        playerTwo.setDiscState(1);
        playerTwo.setName("B");
    }

    // ************** HELPER FUNCTIONS **************

    private void setPlayerTurn(Integer state) {
        this.playerTurn = state;
    }

    private void changePlayerTurn(Integer playerTurn) {
        if (playerTurn == 0) {
            setPlayerTurn(1);
        } else if (playerTurn == 1) {
            setPlayerTurn(0);
        }
    }

    private void countPlayerPoints(Player player) {
        Integer discState = player.getDiscState();
        player.setPoints(board.getAllPlayerDiscs(discState).size());
    }

    private String getDateTime() {
        String format = "yyyy-MM-dd_HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime localDateTime = LocalDateTime.now();
        String formatDateTime = localDateTime.format(formatter);
        return formatDateTime;
    }

    private void addSummary(Player playerOne, Player playerTwo) {
        if (timeline != null) {
            timeline.pause();
        }
        sv = view.new SummaryView(playerOne, playerTwo);
        StackPane summary = sv.getSummary();
        Node node = view.getBorderPane().getCenter();
        StackPane sp = (StackPane) node;
        sp.getChildren().add(3, summary);
    }

    private void removeSummary() {
        Node node = view.getBorderPane().getCenter();
        StackPane sp = (StackPane) node;
        if (sp.getChildren().size() == 4) {
            sp.getChildren().remove(3);
        }
    }

    // ************** VIEW UPDATE **************

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

            if (DEBUG_MARKER == true) {
                View.DebugMarkers dm = view.new DebugMarkers();
                sp.getChildren().add(dv.makeDisc(discState));
                for (Disc disc : flipedDiscsToMark) {
                    if (disc.getRow() == row && disc.getCol() == col) {
                        StackPane spWithMarker = new StackPane();
                        sp.getChildren().remove(1);
                        spWithMarker.getChildren().addAll(dv.makeDisc(discState), dm.flipDebugMarker());
                        sp.getChildren().add(spWithMarker);
                    }
                }
            } else if (DEBUG_MARKER == false) {
                sp.getChildren().add(dv.makeDisc(discState));
            }

            if (MOVE_MARKER == true) {
                for (Integer[] move : allValidMoves) {
                    if (row == move[0] && col == move[1]) {
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

        if (isTimerSwitched == true) {
            setGameTimer();
        }

        if (allValidMoves.size() == 0) {
            addSummary(playerOne, playerTwo);
        }

        flipedDiscsToMark.clear();
    }

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

    private void updatePlayerTurnIndicators() {
        int elementsInWhiteCounter = view.getTopBorderPane().getWhiteCounter().getChildren().size();
        int elementsInBlackCounter = view.getTopBorderPane().getBlackCounter().getChildren().size();

        if (playerTurn == 0) {
            view.getTopBorderPane().getWhiteCounter().getChildren().add(2, dv.makePlayerIndicator());
        } else if (playerTurn == 1) {
            view.getTopBorderPane().getBlackCounter().getChildren().add(2, dv.makePlayerIndicator());
        }

        if (elementsInWhiteCounter == 3) {
            view.getTopBorderPane().getWhiteCounter().getChildren().remove(2);
        } else if (elementsInBlackCounter == 3) {
            view.getTopBorderPane().getBlackCounter().getChildren().remove(2);
        }
    }

    private void updatePointsCounters() {
        countPlayerPoints(playerOne);
        countPlayerPoints(playerTwo);
        // update white disc
        Node nodeWhite = view.getTopBorderPane().getWhiteCounter().getChildren().get(1);
        Text textWhite = (Text) nodeWhite;
        textWhite.setText(Integer.toString(playerOne.getPoints()));

        // update black disc
        Node nodeBlack = view.getTopBorderPane().getBlackCounter().getChildren().get(1);
        Text textBlack = (Text) nodeBlack;
        textBlack.setText(Integer.toString(playerTwo.getPoints()));
        view.highLightPoints(textWhite, textBlack, playerTurn);
    }

    // ************** TIMER **************

    private void resetTimer() {
        timeline.stop();
        resetTimerViewOnTimelineStop();
    }

    private void setGameTimer() {
        timeline.setCycleCount(1);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(TURN_TIME), new KeyValue(new WritableValue<Integer>() {

            @Override
            public Integer getValue() {
                return null;
            }

            @Override
            public void setValue(Integer value) {
                if (playerTurn == 0) {
                    Duration currentTime = timeline.getCurrentTime();
                    Duration totoalTurnTime = timeline.getTotalDuration();
                    Double seconds = totoalTurnTime.toSeconds() - currentTime.toSeconds();

                    view.getTopBorderPane().getTimerViewWhite().setTimerValue(Integer.toString(seconds.intValue()));
                    view.getTopBorderPane().getTimerViewBlack().setTimerValue("0");
                    view.getTopBorderPane().getTimerViewBlack().removeHighlight();

                    if (seconds > 10) {
                        view.getTopBorderPane().getTimerViewWhite().addHighlight();
                    } else {
                        view.getTopBorderPane().getTimerViewWhite().timeoutHighlight();
                    }

                } else if (playerTurn == 1) {
                    Duration currentTime = timeline.getCurrentTime();
                    Duration totoalTurnTime = timeline.getTotalDuration();
                    Double seconds = totoalTurnTime.toSeconds() - currentTime.toSeconds();

                    view.getTopBorderPane().getTimerViewBlack().setTimerValue(Integer.toString(seconds.intValue()));
                    view.getTopBorderPane().getTimerViewWhite().setTimerValue("0");
                    view.getTopBorderPane().getTimerViewWhite().removeHighlight();

                    if (seconds > 10) {
                        view.getTopBorderPane().getTimerViewBlack().addHighlight();
                    } else {
                        view.getTopBorderPane().getTimerViewBlack().timeoutHighlight();
                    }
                }
            }

        }, null)));

        timeline.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (playerTurn == 0) {
                    view.getTopBorderPane().getTimerViewWhite().removeHighlight();
                    changePlayerTurn(playerTurn);
                    getValidMoves(playerTurn);
                    updateBoardView();

                } else if (playerTurn == 1) {
                    view.getTopBorderPane().getTimerViewBlack().removeHighlight();
                    changePlayerTurn(playerTurn);
                    getValidMoves(playerTurn);
                    updateBoardView();
                }
            }
        });

        timeline.play();
    }

    // ************** SEARCH AND VALIDATE MOVES **************

    private ArrayList<Integer[]> getHorizontalMoves(Disc disc) {
        Integer discRow = disc.getRow();
        Integer discCol = disc.getCol();
        Integer colRight = discCol + 1;
        Integer opponentDiscState = 0;
        Integer nextDiscState = -1;
        ArrayList<Integer[]> result = new ArrayList<Integer[]>();

        if (disc.getState() == 0) {
            opponentDiscState = 1;
        } else if (disc.getState() == 1) {
            opponentDiscState = 0;
        }

        // search right
        if (colRight <= board.getBoardGrid().length - 1) {
            nextDiscState = board.getDiscFromBoard(discRow, colRight).getState();
        }

        while (nextDiscState == opponentDiscState) {
            colRight++;

            if (colRight > board.getBoardGrid().length - 1) {
                break;
            }

            nextDiscState = board.getDiscFromBoard(discRow, colRight).getState();

            if (nextDiscState == -1) {
                Integer[] move = new Integer[2];
                move[0] = discRow;
                move[1] = colRight;
                result.add(move);
                break;
            }
        }

        // search left
        Integer colLeft = discCol - 1;
        Integer nextDiscStateLeft = -1;

        if (colLeft >= 0) {
            nextDiscStateLeft = board.getDiscFromBoard(discRow, colLeft).getState();
        }

        while (nextDiscStateLeft == opponentDiscState) {
            colLeft--;

            if (colLeft < 0) {
                break;
            }

            nextDiscStateLeft = board.getDiscFromBoard(discRow, colLeft).getState();

            if (nextDiscStateLeft == -1) {
                Integer[] move = new Integer[2];
                move[0] = discRow;
                move[1] = colLeft;
                result.add(move);
                break;
            }
        }
        return result;
    }

    private ArrayList<Integer[]> getVerticalMoves(Disc disc) {
        Integer discRow = disc.getRow();
        Integer discCol = disc.getCol();
        Integer rowUp = discRow - 1;
        Integer opponentDiscState = 0;
        Integer nextDiscStateUp = -1;
        ArrayList<Integer[]> result = new ArrayList<Integer[]>();

        if (disc.getState() == 0) {
            opponentDiscState = 1;
        } else if (disc.getState() == 1) {
            opponentDiscState = 0;
        }

        // search up
        if (rowUp >= 0) {
            nextDiscStateUp = board.getDiscFromBoard(rowUp, discCol).getState();
        }

        while (nextDiscStateUp == opponentDiscState) {
            rowUp--;
            if (rowUp < 0) {
                break;
            }

            nextDiscStateUp = board.getDiscFromBoard(rowUp, discCol).getState();

            if (nextDiscStateUp == -1) {
                Integer[] move = new Integer[2];
                move[0] = rowUp;
                move[1] = discCol;
                result.add(move);
                break;
            }
        }

        Integer rowDown = discRow + 1;
        Integer nextDiscStateDown = -1;

        // search down
        if (rowDown <= board.getBoardGrid().length - 1) {
            nextDiscStateDown = board.getDiscFromBoard(rowDown, discCol).getState();
        }

        while (nextDiscStateDown == opponentDiscState) {
            rowDown++;
            if (rowDown > board.getBoardGrid().length - 1) {
                break;
            }

            nextDiscStateDown = board.getDiscFromBoard(rowDown, discCol).getState();

            if (nextDiscStateDown == -1) {
                Integer[] move = new Integer[2];
                move[0] = rowDown;
                move[1] = discCol;
                result.add(move);
                break;
            }
        }
        return result;
    }

    private ArrayList<Integer[]> getDiagonalMoves(Disc disc) {
        ArrayList<Integer[]> result = new ArrayList<Integer[]>();
        Integer row = disc.getRow();
        Integer col = disc.getCol();
        Integer discState = disc.getState();
        Integer nextDiscState = -1;
        Integer opponentDiscState = 0;
        Integer prevDiscState = discState;

        if (disc.getState() == 0) {
            opponentDiscState = 1;
        } else if (disc.getState() == 1) {
            opponentDiscState = 0;
        }

        // diagonal up right
        for (int i = row - 1; i >= 0; i--) {
            col++;

            if (col > board.getBoardGrid().length - 1) {
                break;
            }

            nextDiscState = board.getDiscFromBoard(i, col).getState();

            if (nextDiscState == discState) {
                break;
            }

            if (nextDiscState == opponentDiscState) {
                prevDiscState = opponentDiscState;
                continue;
            }

            if (nextDiscState == -1 && prevDiscState != discState) {
                Integer[] move = new Integer[2];
                move[0] = i;
                move[1] = col;
                result.add(move);
                break;
            } else if (nextDiscState == -1 && prevDiscState == discState) {
                break;
            }
        }

        row = disc.getRow();
        col = disc.getCol();
        discState = disc.getState();
        nextDiscState = -1;
        opponentDiscState = 0;
        prevDiscState = discState;

        if (disc.getState() == 0) {
            opponentDiscState = 1;
        } else if (disc.getState() == 1) {
            opponentDiscState = 0;
        }

        // diagonal down left
        for (int i = row + 1; i < board.getBoardGrid().length; i++) {
            col--;

            if (col < 0) {
                break;
            }

            nextDiscState = board.getDiscFromBoard(i, col).getState();

            if (nextDiscState == discState) {
                break;
            }

            if (nextDiscState == opponentDiscState) {
                prevDiscState = opponentDiscState;
                continue;
            }

            if (nextDiscState == -1 && prevDiscState != discState) {
                Integer[] move = new Integer[2];
                move[0] = i;
                move[1] = col;
                result.add(move);
                break;
            } else if (nextDiscState == -1 && prevDiscState == discState) {
                break;
            }
        }

        row = disc.getRow();
        col = disc.getCol();
        discState = disc.getState();
        nextDiscState = -1;
        opponentDiscState = 0;
        prevDiscState = discState;

        if (disc.getState() == 0) {
            opponentDiscState = 1;
        } else if (disc.getState() == 1) {
            opponentDiscState = 0;
        }

        // diagonal up left
        for (int i = col - 1; i >= 0; i--) {
            row--;

            if (row < 0) {
                break;
            }

            nextDiscState = board.getDiscFromBoard(row, i).getState();

            if (nextDiscState == discState) {
                break;
            }

            if (nextDiscState == opponentDiscState) {
                prevDiscState = opponentDiscState;
                continue;
            }

            if (nextDiscState == -1 && prevDiscState != discState) {
                Integer[] move = new Integer[2];
                move[0] = row;
                move[1] = i;
                result.add(move);
                break;
            } else if (nextDiscState == -1 && prevDiscState == discState) {
                break;
            }
        }

        row = disc.getRow();
        col = disc.getCol();
        discState = disc.getState();
        nextDiscState = -1;
        opponentDiscState = 0;
        prevDiscState = discState;

        if (disc.getState() == 0) {
            opponentDiscState = 1;
        } else if (disc.getState() == 1) {
            opponentDiscState = 0;
        }

        // diagonal down right
        for (int i = col + 1; i < board.getBoardGrid().length; i++) {
            row++;

            if (row > board.getBoardGrid().length - 1) {
                break;
            }

            nextDiscState = board.getDiscFromBoard(row, i).getState();

            if (nextDiscState == discState) {
                break;
            }

            if (nextDiscState == opponentDiscState) {
                prevDiscState = opponentDiscState;
                continue;
            }

            if (nextDiscState == -1 && prevDiscState != discState) {
                Integer[] move = new Integer[2];
                move[0] = row;
                move[1] = i;
                result.add(move);
                break;
            } else if (nextDiscState == -1 && prevDiscState == discState) {
                break;
            }
        }
        return result;
    }

    private void getValidMoves(Integer playerTurn) {
        allValidMoves.clear();
        // generate posible moves for player
        ArrayList<Disc> list = board.getAllPlayerDiscs(playerTurn);
        for (Disc disc : list) {
            ArrayList<Integer[]> hMoves = getHorizontalMoves(disc);
            for (Integer[] move : hMoves) {
                allValidMoves.add(move);
            }

            ArrayList<Integer[]> vMoves = getVerticalMoves(disc);
            for (Integer[] move : vMoves) {
                allValidMoves.add(move);
            }

            ArrayList<Integer[]> dMoves = getDiagonalMoves(disc);
            for (Integer[] move : dMoves) {
                allValidMoves.add(move);
            }
        }
    }

    private void switchOnNoValidMoves() {
        // switch player if there are no valid moves
        if (allValidMoves.size() == 0) {
            changePlayerTurn(playerTurn);
            updatePointsCounters();
            updatePlayerTurnIndicators();
            getValidMoves(playerTurn);
        }
    }

    private boolean validatePlacedMove(Integer row, Integer col) {
        boolean result = false;
        for (Integer[] move : allValidMoves) {
            if (row == move[0] && col == move[1]) {
                result = true;
            }
        }
        return result;
    }

    // ************** FLIP OPPONENT DISCS **************

    private void flipHorizontalDiscs(Integer row, Integer col, Integer playerTurn) {
        Integer nextDiscState = -1;
        Integer primaryDiscState = playerTurn;
        ArrayList<Disc> discsToFlip = new ArrayList<Disc>();

        // add loop to check if placed move "close" opponent discs on right
        for (int i = col + 1; i <= board.getBoardGrid().length - 1; i++) {
            nextDiscState = board.getDiscFromBoard(row, i).getState();
            if (nextDiscState != primaryDiscState) {
                Disc opponentDisc = board.getDiscFromBoard(row, i);
                discsToFlip.add(opponentDisc);
            }

            if (nextDiscState == -1) {
                discsToFlip.clear();
                break;
            } else if (nextDiscState == primaryDiscState) {
                for (Disc disc : discsToFlip) {
                    board.getDiscFromBoard(disc.getRow(), disc.getCol()).setState(primaryDiscState);
                    flipedDiscsToMark.add(disc);
                }
                break;
            } else if (i + 1 > board.getBoardGrid().length - 1) {
                discsToFlip.clear();
                break;
            }
        }

        // add loop to check if placed move "close" opponent discs on left
        for (int i = col - 1; i >= 0; i--) {
            nextDiscState = board.getDiscFromBoard(row, i).getState();
            if (nextDiscState != primaryDiscState) {
                Disc opponentDisc = board.getDiscFromBoard(row, i);
                discsToFlip.add(opponentDisc);
            }

            if (nextDiscState == -1) {
                discsToFlip.clear();
                break;
            } else if (nextDiscState == primaryDiscState) {
                for (Disc disc : discsToFlip) {
                    board.getDiscFromBoard(disc.getRow(), disc.getCol()).setState(primaryDiscState);
                    flipedDiscsToMark.add(disc);
                }
                break;
            } else if (i - 1 < 0) {
                discsToFlip.clear();
                break;
            }
        }
    }

    private void flipVerticalDiscs(Integer row, Integer col, Integer playerTurn) {
        Integer nextDiscState = -1;
        Integer primaryDiscState = playerTurn;
        ArrayList<Disc> discsToFlip = new ArrayList<Disc>();

        // add loop to check if placed move "close" opponent discs up
        for (int i = row - 1; i >= 0; i--) {
            nextDiscState = board.getDiscFromBoard(i, col).getState();
            if (nextDiscState != primaryDiscState) {
                Disc opponentDisc = board.getDiscFromBoard(i, col);
                discsToFlip.add(opponentDisc);
            }

            if (nextDiscState == -1) {
                discsToFlip.clear();
                break;
            } else if (nextDiscState == primaryDiscState) {
                for (Disc disc : discsToFlip) {
                    board.getDiscFromBoard(disc.getRow(), disc.getCol()).setState(primaryDiscState);
                    flipedDiscsToMark.add(disc);
                }
                break;
            } else if (i - 1 < 0) {
                discsToFlip.clear();
                break;
            }
        }

        // add loop to check if placed move "close" opponent discs down
        for (int i = row + 1; i <= board.getBoardGrid().length - 1; i++) {
            nextDiscState = board.getDiscFromBoard(i, col).getState();
            if (nextDiscState != primaryDiscState) {
                Disc opponentDisc = board.getDiscFromBoard(i, col);
                discsToFlip.add(opponentDisc);
            }

            if (nextDiscState == -1) {
                discsToFlip.clear();
                break;
            } else if (nextDiscState == primaryDiscState) {
                for (Disc disc : discsToFlip) {
                    board.getDiscFromBoard(disc.getRow(), disc.getCol()).setState(primaryDiscState);
                    flipedDiscsToMark.add(disc);
                }
                break;
            } else if (i + 1 > board.getBoardGrid().length - 1) {
                discsToFlip.clear();
                break;
            }
        }
    }

    private void flipDiagonalDiscs(Integer row, Integer col, Integer playerTurn) {
        Integer nextDiscState = -1;
        Integer primaryDiscState = playerTurn;
        Integer tmpRow = row;
        Integer tmpCol = col;
        ArrayList<Disc> discsToFlip = new ArrayList<Disc>();

        // add loop to check if placed move "close" opponent discs (diagonal up right)
        for (int i = row - 1; i >= 0; i--) {
            col++;

            if (col > board.getBoardGrid().length - 1) {
                break;
            }

            nextDiscState = board.getDiscFromBoard(i, col).getState();
            if (nextDiscState != primaryDiscState) {
                Disc opponentDisc = board.getDiscFromBoard(i, col);
                discsToFlip.add(opponentDisc);
            }

            if (nextDiscState == -1) {
                discsToFlip.clear();
                break;
            } else if (nextDiscState == primaryDiscState) {
                for (Disc disc : discsToFlip) {
                    board.getDiscFromBoard(disc.getRow(), disc.getCol()).setState(primaryDiscState);
                    flipedDiscsToMark.add(disc);
                }
                break;
            } else if (i - 1 < 0 || col + 1 > board.getBoardGrid().length - 1) {
                discsToFlip.clear();
                break;
            }
        }

        row = tmpRow;
        col = tmpCol;

        // add loop to check if placed move "close" opponent discs (diagonal down left)
        for (int i = row + 1; i < board.getBoardGrid().length; i++) {
            col--;

            if (col < 0) {
                break;
            }

            nextDiscState = board.getDiscFromBoard(i, col).getState();
            if (nextDiscState != primaryDiscState) {
                Disc opponentDisc = board.getDiscFromBoard(i, col);
                discsToFlip.add(opponentDisc);
            }

            if (nextDiscState == -1) {
                discsToFlip.clear();
                break;
            } else if (nextDiscState == primaryDiscState) {
                for (Disc disc : discsToFlip) {
                    board.getDiscFromBoard(disc.getRow(), disc.getCol()).setState(primaryDiscState);
                    flipedDiscsToMark.add(disc);
                }
                break;
            } else if (i + 1 > board.getBoardGrid().length - 1 || col - 1 < 0) {
                discsToFlip.clear();
                break;
            }
        }

        row = tmpRow;
        col = tmpCol;

        // add loop to check if placed move "close" opponent discs (diagonal up left)
        for (int i = row - 1; i >= 0; i--) {
            col--;

            if (col < 0) {
                break;
            }

            nextDiscState = board.getDiscFromBoard(i, col).getState();
            if (nextDiscState != primaryDiscState) {
                Disc opponentDisc = board.getDiscFromBoard(i, col);
                discsToFlip.add(opponentDisc);
            }

            if (nextDiscState == -1) {
                discsToFlip.clear();
                break;
            } else if (nextDiscState == primaryDiscState) {
                for (Disc disc : discsToFlip) {
                    board.getDiscFromBoard(disc.getRow(), disc.getCol()).setState(primaryDiscState);
                    flipedDiscsToMark.add(disc);
                }
                break;
            } else if (i - 1 < 0 || col - 1 < 0) {
                discsToFlip.clear();
                break;
            }
        }

        row = tmpRow;
        col = tmpCol;

        // add loop to check if placed move "close" opponent discs (diagonal down right)
        for (int i = row + 1; i < board.getBoardGrid().length; i++) {
            col++;

            if (col > board.getBoardGrid().length - 1) {
                break;
            }

            nextDiscState = board.getDiscFromBoard(i, col).getState();
            if (nextDiscState != primaryDiscState) {
                Disc opponentDisc = board.getDiscFromBoard(i, col);
                discsToFlip.add(opponentDisc);
            }

            if (nextDiscState == -1) {
                discsToFlip.clear();
                break;
            } else if (nextDiscState == primaryDiscState) {
                for (Disc disc : discsToFlip) {
                    board.getDiscFromBoard(disc.getRow(), disc.getCol()).setState(primaryDiscState);
                    flipedDiscsToMark.add(disc);
                }
                break;
            } else if (i + 1 > board.getBoardGrid().length - 1 || col + 1 > board.getBoardGrid().length - 1) {
                discsToFlip.clear();
                break;
            }
        }
    }

    // ************** CLICK HANDLERS **************

    private void onGridClick() {
        bg.getBoardGridPane().getChildren().forEach(square -> {
            square.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    Node node = (Node) event.getSource();
                    Integer col = bg.getBoardGridPane().getColumnIndex(node);
                    Integer row = bg.getBoardGridPane().getRowIndex(node);

                    boolean validMove = validatePlacedMove(row, col);

                    // player can place disc only on empty square
                    if (board.getDiscFromBoard(row, col).getState() == -1 && validMove == true) {
                        board.modifyDiscState(row, col, playerTurn);
                        flipHorizontalDiscs(row, col, playerTurn);
                        flipVerticalDiscs(row, col, playerTurn);
                        flipDiagonalDiscs(row, col, playerTurn);

                        if (isTimerSwitched == true) {
                            resetTimer();
                        }

                        // change player after update
                        changePlayerTurn(playerTurn);

                        getValidMoves(playerTurn);

                        updateBoardView();
                    }
                }
            });
        });
    }

    private void onExitButtonClick() {
        view.getTopBorderPane().getExitButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if (timeline != null) {
                    timeline.pause();
                }

                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setContentText("Do you want to exit game?");
                Optional<ButtonType> option = alert.showAndWait();
                if (ButtonType.OK.equals(option.get()) == true) {
                    System.exit(0);
                } else {
                    if (timeline != null && isTimerSwitched == true) {
                        timeline.play();
                    }
                }

            }
        });
    }

    private void onNewGameButtonClick() {
        view.getTopBorderPane().getNewGameButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if (isTimerSwitched == true) {
                    if (timeline != null) {
                        timeline.stop();
                        timeline = new Timeline();
                    } else {
                        timeline = new Timeline();
                    }
                } else if (isTimerSwitched == false) {
                    timeline = new Timeline();
                    timeline.pause();
                }

                initPlayer();
                setPlayerTurn(initPlayerTurn);
                board.initBoard();
                getValidMoves(playerTurn);
                updateBoardView();
                removeSummary();
            }
        });
    }

    private void onSaveButtonClick() {
        view.getTopBorderPane().getSaveButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName("REVERSI_GAME_SAVE_" + getDateTime());
                Disc[][] boardGrid = board.getBoardGrid();

                if (timeline != null) {
                    timeline.pause();
                }

                try {
                    File file = fileChooser.showSaveDialog(stage);
                    if (file != null) {
                        FileWriter fw = new FileWriter(file);
                        BufferedWriter bw = new BufferedWriter(fw);

                        for (int row = 0; row < boardGrid.length; row++) {
                            for (int col = 0; col < boardGrid[row].length; col++) {
                                Disc disc = board.getDiscFromBoard(row, col);
                                String s = row + "," + col + "," + disc.getState() + "\n";
                                try {
                                    bw.write(s);
                                } catch (IOException e) {
                                    Alert alert = new Alert(AlertType.INFORMATION);
                                    alert.setContentText("Can't write to file!");
                                    alert.show();
                                }
                            }
                        }
                        String playerTurnString = Integer.toString(playerTurn);
                        bw.write(playerTurnString);
                        bw.close();

                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setContentText("File saved!");

                        Optional<ButtonType> option = alert.showAndWait();
                        if (ButtonType.OK.equals(option.get()) == true) {
                            if (timeline != null && isTimerSwitched == true) {
                                timeline.play();
                            }
                        }
                    } else {
                        if (timeline != null && isTimerSwitched == true) {
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

    private void onLoadButtonClick() {
        view.getTopBorderPane().getLoadButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
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
                                board.getDiscFromBoard(row, col).setState(discState);
                            } else {
                                int playerState = Integer.valueOf(line);
                                setPlayerTurn(playerState);
                            }
                        }
                        br.close();

                        if (timeline != null) {
                            resetTimer();
                            if (isTimerSwitched == false) {
                                view.getTopBorderPane().getTimerViewWhite().switchOffTimer();
                                view.getTopBorderPane().getTimerViewBlack().switchOffTimer();
                            }
                            getValidMoves(playerTurn);
                            updateBoardView();
                        } else {
                            timeline = new Timeline();
                            initPlayer();
                            getValidMoves(playerTurn);
                            updateBoardView();
                        }
                    } else {
                        if (timeline != null) {
                            timeline.play();
                        }
                    }
                } catch (NumberFormatException | IOException | NullPointerException e) {
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

    private void onTimerButtonClick() {
        view.getTopBorderPane().getNewTimedGameButton().setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                isTimerSwitched = true;
                view.getTopBorderPane().getTimerViewWhite().setTimerValue("0");
                view.getTopBorderPane().getTimerViewBlack().setTimerValue("0");

                if (event.getClickCount() == 2 && timeline != null) {
                    isTimerSwitched = false;
                    timeline.stop();
                    view.getTopBorderPane().getTimerViewWhite().switchOffTimer();
                    view.getTopBorderPane().getTimerViewBlack().switchOffTimer();
                } else if (event.getClickCount() == 2) {
                    isTimerSwitched = false;
                    view.getTopBorderPane().getTimerViewWhite().switchOffTimer();
                    view.getTopBorderPane().getTimerViewBlack().switchOffTimer();
                }
            }

        });
    }
}