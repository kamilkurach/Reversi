package com.yacotaco;

import java.util.ArrayList;

/**
 * Board class.
 *
 * @author Kamil Kurach
 * @author https://github.com/yacotaco
 * @version 1.0
 */
public class Board {
    /** Number of rows. */
    private final int rows = 8;
    /** Number of columns. */
    private final int cols = 8;
    /** Representation of boardGrid in 2d array. */
    private Disc[][] boardGrid = new Disc[rows][cols];
    /**List of all valid moves for current player.
    * List of arrays with coordinates [row, col]. */
    private ArrayList<Integer[]> allValidMoves = new ArrayList<Integer[]>();

    /**
     * Board constructor.
     */
    public Board() {
        initBoard();
    }

    /**
     * Inits boardGrid with two discs for each player.
     * All discs are initially set to -1 which translates
     * in view to empty square on boardGrid.
     */
    public void initBoard() {
        Integer state = -1;
        for (int row = 0; row < boardGrid.length; row++) {
            for (int col = 0; col < boardGrid[row].length; col++) {
                addDisc(row, col, state);
            }
        }
        initDiscs();
    }

    /**
     * Inits two discs for each player.
     */
    private void initDiscs() {
        final int firstWhiteDiscRow = 3;
        final int firstWhiteDiscCol = 3;
        final int whiteDiscState = 0;
        modifyDiscState(firstWhiteDiscRow, firstWhiteDiscCol, whiteDiscState);

        final int secWhiteDiscRow = 4;
        final int secWhiteDiscCol = 4;
        modifyDiscState(secWhiteDiscRow, secWhiteDiscCol, whiteDiscState);

        final int firstBlackDiscRow = 3;
        final int firstBlackDiscCol = 4;
        final int blackDiscState = 1;
        modifyDiscState(firstBlackDiscRow, firstBlackDiscCol, blackDiscState);

        final int secBlackDiscRow = 4;
        final int secBlackDiscCol = 3;
        modifyDiscState(secBlackDiscRow, secBlackDiscCol, blackDiscState);
    }

    /**
     * Adds disc object to boardGrid array.
     *
     * @param row       position in row.
     * @param col       position in column.
     * @param discState disc state.
     */
    private void addDisc(final Integer row, final Integer col,
     final Integer discState) {
        Disc disc = new Disc();
        disc.setRow(row);
        disc.setCol(col);
        disc.setState(discState);
        boardGrid[row][col] = disc;
    }

    /**
     * Gets disc object from boardGrid for given coordinates.
     *
     * @param row position in row.
     * @param col position in column.
     * @return Disc object.
     */
    public Disc getDiscFromBoard(final Integer row, final Integer col) {
        Disc disc = boardGrid[row][col];
        return disc;
    }
    
    /**
     * 
     * @return Returns all valid moves for current player.
     */
    public ArrayList<Integer[]> getAllValidMoves() {
        return allValidMoves;
    }

    /**
     * Modifies state of disc.
     *
     * @param row       position in row.
     * @param col       position in column.
     * @param discState disc state.
     */
    public void modifyDiscState(final Integer row, final Integer col,
     final Integer discState) {
        Disc disc = getDiscFromBoard(row, col);
        disc.setState(discState);
    }

    /**
     * Gets all discs of particular player.
     *
     * @param currentPlayer player disc state (or playerTurn).
     * @see playerTurn.
     * @return list of Disc objects.
     */
    public ArrayList<Disc> getAllPlayerDiscs(final Integer currentPlayer) {
        ArrayList<Disc> list = new ArrayList<Disc>();
        for (int row = 0; row < boardGrid.length; row++) {
            for (int col = 0; col < boardGrid[row].length; col++) {
                Disc disc = getDiscFromBoard(row, col);
                int discState = disc.getState();
                if (discState == currentPlayer) {
                    list.add(disc);
                }
            }
        }
        return list;
    }

    // ************** SEARCH AND VALIDATE MOVES **************

    /** Gets all horizontal moves.
     *
     * @param disc Disc object.
     * @return list of arrays with moves coordinates.
     */
    private ArrayList<Integer[]> getHorizontalMoves(final Disc disc) {
        Integer discRow = disc.getRow();
        Integer discCol = disc.getCol();
        int colRight = discCol + 1;
        int opponentDiscState = 0;
        int nextDiscState = -1;
        ArrayList<Integer[]> result = new ArrayList<Integer[]>();

        if (disc.getState() == 0) {
            opponentDiscState = 1;
        } else if (disc.getState() == 1) {
            opponentDiscState = 0;
        }

        // search right
        if (colRight <= boardGrid.length - 1) {
            nextDiscState = this.getDiscFromBoard(discRow, colRight)
                .getState();
        }

        while (nextDiscState == opponentDiscState) {
            colRight++;

            if (colRight > boardGrid.length - 1) {
                break;
            }

            nextDiscState = this.getDiscFromBoard(discRow, colRight)
                .getState();

            if (nextDiscState == -1) {
                Integer[] move = new Integer[2];
                move[0] = discRow;
                move[1] = colRight;
                result.add(move);
                break;
            }
        }

        // search left
        int colLeft = discCol - 1;
        int nextDiscStateLeft = -1;

        if (colLeft >= 0) {
            nextDiscStateLeft = this.getDiscFromBoard(discRow, colLeft)
                .getState();
        }

        while (nextDiscStateLeft == opponentDiscState) {
            colLeft--;

            if (colLeft < 0) {
                break;
            }

            nextDiscStateLeft = this.getDiscFromBoard(discRow, colLeft)
                .getState();

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

    /** Gets all vertical moves.
     *
     * @param disc Disc object.
     * @return list of arrays with moves coordinates.
     */
    private ArrayList<Integer[]> getVerticalMoves(final Disc disc) {
        Integer discRow = disc.getRow();
        Integer discCol = disc.getCol();
        int rowUp = discRow - 1;
        int opponentDiscState = 0;
        int nextDiscStateUp = -1;
        ArrayList<Integer[]> result = new ArrayList<Integer[]>();

        if (disc.getState() == 0) {
            opponentDiscState = 1;
        } else if (disc.getState() == 1) {
            opponentDiscState = 0;
        }

        // search up
        if (rowUp >= 0) {
            nextDiscStateUp = this.getDiscFromBoard(rowUp, discCol).getState();
        }

        while (nextDiscStateUp == opponentDiscState) {
            rowUp--;
            if (rowUp < 0) {
                break;
            }

            nextDiscStateUp = this.getDiscFromBoard(rowUp, discCol).getState();

            if (nextDiscStateUp == -1) {
                Integer[] move = new Integer[2];
                move[0] = rowUp;
                move[1] = discCol;
                result.add(move);
                break;
            }
        }

        int rowDown = discRow + 1;
        int nextDiscStateDown = -1;

        // search down
        if (rowDown <= boardGrid.length - 1) {
            nextDiscStateDown = this.getDiscFromBoard(rowDown, discCol)
                .getState();
        }

        while (nextDiscStateDown == opponentDiscState) {
            rowDown++;
            if (rowDown > boardGrid.length - 1) {
                break;
            }

            nextDiscStateDown = this.getDiscFromBoard(rowDown, discCol)
                .getState();

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

    /** Gets all diagonal moves.
     *
     * @param disc Disc object.
     * @return list of arrays with moves coordinates.
     */
    private ArrayList<Integer[]> getDiagonalMoves(final Disc disc) {
        ArrayList<Integer[]> result = new ArrayList<Integer[]>();
        Integer row = disc.getRow();
        Integer col = disc.getCol();
        Integer discState = disc.getState();
        int nextDiscState = -1;
        int opponentDiscState = 0;
        int prevDiscState = discState;

        if (disc.getState() == 0) {
            opponentDiscState = 1;
        } else if (disc.getState() == 1) {
            opponentDiscState = 0;
        }

        // diagonal up right
        for (int i = row - 1; i >= 0; i--) {
            col++;

            if (col > boardGrid.length - 1) {
                break;
            }

            nextDiscState = this.getDiscFromBoard(i, col).getState();

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
        for (int i = row + 1; i < boardGrid.length; i++) {
            col--;

            if (col < 0) {
                break;
            }

            nextDiscState = this.getDiscFromBoard(i, col).getState();

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

            nextDiscState = this.getDiscFromBoard(row, i).getState();

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
        for (int i = col + 1; i < boardGrid.length; i++) {
            row++;

            if (row > boardGrid.length - 1) {
                break;
            }

            nextDiscState = this.getDiscFromBoard(row, i).getState();

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

    /** Collects all valid moves for current player.
     *
     * @param newPlayerTurn current player.
     */
    public void getValidMoves(final Integer newPlayerTurn) {
        allValidMoves.clear();
        // generate posible moves for player
        ArrayList<Disc> list = this.getAllPlayerDiscs(newPlayerTurn);
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
        this.removeDuplicatedValidMoves();
    }

    /** Removes duplicated valid moves. */
    private void removeDuplicatedValidMoves() {
        for (int i = 0; i < allValidMoves.size(); i++) {
            Integer[] iMove = allValidMoves.get(i);
            int iRow = iMove[0];
            int iCol = iMove[1];
            for (int j = 0; j < allValidMoves.size(); j++) {
                Integer[] jMove = allValidMoves.get(j);
                int jRow = jMove[0];
                int jCol = jMove[1];
                if (i != j) {
                    if (iRow == jRow && iCol == jCol) {
                        allValidMoves.remove(j);
                    }
                }
            }
        }
    }

    /** Prints current state of the boardGrid to console. */
    public void printBoard() {
        for (int row = 0; row < boardGrid.length; row++) {
            for (int col = 0; col < boardGrid[row].length; col++) {
                Disc disc = getDiscFromBoard(row, col);
                System.out.print(disc.getState() + "\t");
            }
            System.out.println();
        }
    }

    /**
     * Gets boardGrid.
     *
     * @return 2d array of Disc objects.
     */
    public Disc[][] getBoardGrid() {
        return boardGrid;
    }
}
