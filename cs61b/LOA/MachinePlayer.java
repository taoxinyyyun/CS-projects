/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import static loa.Piece.*;

/** An automated Player.
 *  @author Tess
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (board.gameOver()) {
            return WINNING_VALUE;
        } else if (depth == 0) {
            return heuristic(board);
        }
        int bestScore = 0;
        for (Move move : board.legalMoves()) {
            if (saveMove && _foundMove == null) {
                _foundMove = move;
            }
            board.makeMove(move);
            int score = findMove(board, depth - 1, false, -sense, alpha, beta);
            board.retract();
            if (sense == 1 && score > bestScore
                    || sense == -1 && score < bestScore) {
                bestScore = score;
                if (saveMove) {
                    _foundMove = move;
                }
            }
            if (sense == 1) {
                alpha = Math.max(score, alpha);
            } else {
                beta = Math.min(score, beta);
            }
            if (alpha >= beta) {
                break;
            }
        }
        return bestScore;
    }


    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 3;
    }


    /** Calculate the heuristic value for the current position.
     * @return the heuristic value of the board
     * @param board the current board. */
    private int heuristic(Board board) {
        Square whiteSide = findCentral(board, WP);
        Square blackSide = findCentral(board, BP);
        int bscore = 0;
        int wscore = 0;
        for (Square sq: Square.ALL_SQUARES) {
            if (board.get(sq) == WP) {
                wscore += sq.distance(whiteSide);
            } else if (board.get(sq) == BP) {
                bscore += sq.distance(blackSide);
            }
        }
        return bscore - wscore;
    }

    /** Find the centre of mass for the side of the current position.
     * @return the square that is the center of mass of this side
     * @param board the board
     * @param side the player. */
    private Square findCentral(Board board, Piece side) {
        int columnSum = 0;
        int rowSum = 0;
        int count = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board.get(Square.sq(i, j)) == side) {
                    columnSum += i;
                    rowSum += j;
                    count += 1;
                }
            }
        }
        return Square.sq((columnSum / count), (rowSum / count));
    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

}
