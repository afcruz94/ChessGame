package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.enums.Color;
import chess.enums.PromotingTypes;
import chess.pieces.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {
    private int turn;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;
    private Color currentPlayer;
    private Board board;
    private List<Piece> piecesOnTheBoard;
    private List<Piece> capturedPieces;

    public ChessMatch() {
        // Create a 8x8 board
        this.board = new Board(8, 8);
        this.turn = 1;
        this.check = false;
        this.checkMate = false;
        this.currentPlayer = Color.WHITE;
        this.piecesOnTheBoard = new ArrayList<>();
        this.capturedPieces = new ArrayList<>();

        initialSetup();
    }

    public int getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckMate() {
        return checkMate;
    }

    public ChessPiece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted() {
        return promoted;
    }

    /**
     * Get the pieces of each position to print
     *
     * @return matrix of pieces
     */
    public ChessPiece[][] getPieces() {
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j);
            }
        }

        return mat;
    }

    /**
     * Creates the pieces in the initial position
     */
    private void initialSetup() {
        placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('d', 8, new Queen(board, Color.BLACK));
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));

    }

    /**
     * Place the piece on the board convert the human-readable position to the matrix position
     *
     * @param column
     * @param row
     * @param piece
     */
    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }

    /**
     * Perform chess move
     *
     * @param startPosition
     * @param targetPosition
     * @return
     */
    public ChessPiece performChessMove(ChessPosition startPosition, ChessPosition targetPosition) {
        Position source = startPosition.toPosition();
        Position target = targetPosition.toPosition();

        validateSourcePosition(source);
        validateTargetPosition(source, target);
        Piece capturedPiece = makeMove(source, target);

        // Check
        if (testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("You can't put yourself in check");
        }

        ChessPiece movedPiece = (ChessPiece) board.piece(target);

        // special move promotion
        promoted = null;
        if (movedPiece instanceof Pawn) {
            if ((movedPiece.getColor() == Color.WHITE && target.getRow() == 0) ||
                    (movedPiece.getColor() == Color.BLACK && target.getRow() == 7)) {
                promoted = (ChessPiece) board.piece(target);
                promoted = replacePromotedPiece("Q");
            }
        }

        this.check = testCheck(opponent(currentPlayer)) ? true : false;

        // Check mate
        if (testCheckMate(opponent(currentPlayer))) this.checkMate = true;
        else nextTurn();

        // special move enPassant
        if (movedPiece instanceof Pawn &&
                (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2)) {
            enPassantVulnerable = movedPiece;
        } else enPassantVulnerable = null;

        return (ChessPiece) capturedPiece;
    }

    /**
     * Make the chess move requested by the user
     *
     * @param source
     * @param target
     * @return
     */
    private Piece makeMove(Position source, Position target) {
        ChessPiece p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();

        Piece capturedPiece = board.removePiece(target);

        board.placePiece(p, target);

        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);
            this.capturedPieces.add(capturedPiece);
        }

        // castling king size rook
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);

            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        // castling queen size rook
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);

            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        // en passant move
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) {
                Position pawnPos;

                if (p.getColor() == Color.WHITE) {
                    pawnPos = new Position(target.getRow() + 1, target.getColumn());
                } else {
                    pawnPos = new Position(target.getRow() - 1, target.getColumn());
                }

                capturedPiece = board.removePiece(pawnPos);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }

        return capturedPiece;
    }

    /**
     * Undo previous move
     *
     * @param source
     * @param target
     * @param capturedPiece
     */
    private void undoMove(Position source, Position target, Piece capturedPiece) {
        ChessPiece p = (ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();

        board.placePiece(p, source);

        if (capturedPiece != null) {
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }

        // castling king side rook
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);

            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        // castling queen side rook
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);

            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        // en passant move
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
                ChessPiece pawn = (ChessPiece) board.removePiece(target);
                Position pawnPos;

                if (p.getColor() == Color.WHITE) {
                    pawnPos = new Position(3, target.getColumn());
                } else {
                    pawnPos = new Position(4, target.getColumn());
                }

                board.placePiece(pawn, pawnPos);
            }
        }
    }

    /**
     * Validate if a piece exists in the required position
     *
     * @param position
     */
    private void validateSourcePosition(Position position) {
        ChessPiece p = (ChessPiece) board.piece(position);

        if (!board.thereIsAPiece(position)) {
            throw new ChessException("There is no piece on source position");
        }
        if (p.getColor() != currentPlayer) {
            throw new ChessException("The chosen piece is not yours");
        }
        if (!board.piece(position).isThereAnyPossibleMove()) {
            throw new ChessException("There is no possible moves for the chosen piece");
        }
    }

    /**
     * Validate if the destination is valid or not
     *
     * @param source
     * @param target
     */
    private void validateTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target)) {
            throw new ChessException("The piece cannot move to the target position");
        }
    }

    /**
     * Returns all the possible moves of the piece
     *
     * @param sourcePosition
     * @return
     */
    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();

        validateSourcePosition(position);

        return board.piece(position).possibleMoves();
    }

    /**
     * Move to the next turn of the game
     */
    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    /**
     * Get the opponent color
     *
     * @param color
     * @return
     */
    private Color opponent(Color color) {
        return color == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(Color color) {
        List<Piece> pieces = (piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)).collect(Collectors.toList());

        for (Piece p : pieces) {
            if (p instanceof King) return (ChessPiece) p;
        }

        throw new IllegalStateException("There is no King " + color + " in the board");
    }

    /**
     * Validate if the opponent is checked
     *
     * @param color
     * @return
     */
    private boolean testCheck(Color color) {
        Position kingPos = king(color).getChessPosition().toPosition();

        List<Piece> opponentPieces = (piecesOnTheBoard.stream()
                .filter(x -> ((ChessPiece) x)
                        .getColor() == opponent(color)))
                .collect(Collectors.toList());

        for (Piece p : opponentPieces) {
            boolean[][] mat = p.possibleMoves();

            if (mat[kingPos.getRow()][kingPos.getColumn()]) return true;
        }

        return false;
    }

    /**
     * Validate checkmate
     *
     * @param color
     * @return
     */
    private boolean testCheckMate(Color color) {
        if (!testCheck(color)) return false;

        List<Piece> list = (piecesOnTheBoard.stream()
                .filter(x -> ((ChessPiece) x)
                        .getColor() == color))
                .collect(Collectors.toList());

        for (Piece p : list) {
            boolean[][] mat = p.possibleMoves();

            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if (mat[i][j]) {
                        Position source = ((ChessPiece) p).getChessPosition().toPosition();
                        Position target = new Position(i, j);

                        Piece capturePiece = makeMove(source, target);

                        boolean testCheck = testCheck(color);

                        undoMove(source, target, capturePiece);

                        if (!testCheck) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Replace pawn piece by another (Bishop/Knight/Rook/Queen)
     *
     * @param type
     * @return
     */
    public ChessPiece replacePromotedPiece(String type) {
        if (promoted == null) throw new IllegalStateException("There is no piece to be promoted");

        Position pos = promoted.getChessPosition().toPosition();
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);

        ChessPiece newPiece = newPiece(type, promoted.getColor());

        board.placePiece(newPiece, pos);
        piecesOnTheBoard.add(newPiece);

        return newPiece;
    }

    /**
     * Validate if the new entered piece is valid
     *
     * @param type
     * @param color
     * @return
     */
    private ChessPiece newPiece(String type, Color color) {
        if (type.equals("B")) return new Bishop(board, color);
        if (type.equals("N")) return new Knight(board, color);
        if (type.equals("Q")) return new Queen(board, color);
        return new Rook(board, color);
    }
}
