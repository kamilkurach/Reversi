package com.yacotaco;

import java.lang.reflect.Array;
import java.util.ArrayList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * Controller
 */
public class Controller {
    private Board board;
    private View view;
    private View.BoardGrid bg;
    private View.DiscView dv;
    private Player playerOne;
    private Player playerTwo;
    private Integer playerTurn;
    private ArrayList<Integer[]> allValidMoves = new ArrayList<Integer[]>();

    /**
     * @param board     Board class passed to controller
     * @param view      View class passed to controller
     * @param bg        BoardGrid view nested class
     * @param dv        DiscView view nested class
     * @param playerOne Player class
     * @param playerTwo Player class
     */

    public Controller(Board board, View view) {
        this.board = board;
        this.view = view;
        this.bg = view.new BoardGrid();
        this.dv = view.new DiscView();
        this.playerOne = new Player();
        this.playerTwo = new Player();
        initController();
    }

    private void initController() {
        initPlayer();
        onGridClick();
        setPlayerTurn(1);
        getValidMoves(playerTurn);
        updateBoardView();
    }

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

    private void initPlayer() {
        playerOne.setDiscState(0);
        playerOne.setName("A");
        playerTwo.setDiscState(1);
        playerTwo.setName("B");
    }

    private void updateBoardView() {
        for (Node square : bg.getBoardGridPane().getChildren()) {
            Integer col = bg.getBoardGridPane().getColumnIndex(square);
            Integer row = bg.getBoardGridPane().getRowIndex(square);
            Integer discState = board.getDiscFromBoard(row, col).getState();
            StackPane sp = (StackPane) square;

            if (sp.getChildren().size() == 2) {
                sp.getChildren().remove(1);
            }

            sp.getChildren().add(dv.makeDisc(discState));

            for (Integer[] move : allValidMoves) {
                if (row == move[0] && col == move[1]) {
                    sp.getChildren().add(bg.validMoveMarker());
                }
            }

            if (sp.getChildren().size() > 3) {
                sp.getChildren().remove(2, sp.getChildren().size() - 1);
            }

            // debug
            // System.out.println(row + " " + col + " " + sp.getChildren().size());
        }
    }

    private ArrayList<Integer[]> getHorizontalMoves(Disc disc) {
        Integer discRow = disc.getRow();
        Integer discCol = disc.getCol();
        Integer colRight = discCol + 1;
        Integer oponentDiscState = 0;
        ArrayList<Integer[]> result = new ArrayList<Integer[]>();
        Integer nextDiscStateRight = -1;

        if (disc.getState() == 0) {
            oponentDiscState = 1;
        } else if (disc.getState() == 1) {
            oponentDiscState = 0;
        }

        // search right
        if (colRight > board.getBoardGrid().length - 1) {
            return result;
        } else {
            nextDiscStateRight = board.getDiscFromBoard(discRow, colRight).getState();
        }

        while (nextDiscStateRight == oponentDiscState) {
            colRight++;
            if (colRight > board.getBoardGrid().length - 1) {
                break;
            }

            nextDiscStateRight = board.getDiscFromBoard(discRow, colRight).getState();
            if (nextDiscStateRight == -1) {
                Integer[] move = new Integer[2];
                move[0] = discRow;
                move[1] = colRight;
                result.add(move);
                // System.out.println("right " + discRow + " " + colRight + " disc state " + disc.getState());
                break;
            }
        }

        // search left
        Integer colLeft = discCol - 1;
        Integer nextDiscStateLeft = -1;

        if (colLeft < 0) {
            return result;
        } else {
            nextDiscStateLeft = board.getDiscFromBoard(discRow, colLeft).getState();
        }

        while (nextDiscStateLeft == oponentDiscState) {
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
                // System.out.println("left " + discRow + " " + colLeft + " disc state " + disc.getState());
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
            System.out.println(disc.getRow() + " " + disc.getCol() + " player " + disc.getState());
            ArrayList<Integer[]> hMoves = getHorizontalMoves(disc);
            for (Integer[] move : hMoves) {
                allValidMoves.add(move);
            }
        }
    }

    private void onGridClick() {
        bg.getBoardGridPane().getChildren().forEach(square -> {
            square.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    Node node = (Node) event.getSource();
                    Integer col = bg.getBoardGridPane().getColumnIndex(node);
                    Integer row = bg.getBoardGridPane().getRowIndex(node);

                    // player can place disc only on empty square
                    if (board.getDiscFromBoard(row, col).getState() == -1) {
                        board.modifyDiscState(row, col, playerTurn);
                    }

                    // change player after update
                    changePlayerTurn(playerTurn);
                    // debug
                    System.out.println("--------------");
                    getValidMoves(playerTurn);

                    updateBoardView();
                }
            });
        });
    }
}