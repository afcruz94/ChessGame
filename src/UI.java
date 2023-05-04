import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.enums.Color;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class UI {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";

    /**
     * Read input chess position
     *
     * @param sc
     * @return
     */
    public static ChessPosition readChessPosition(Scanner sc) {
        try {
            String pos = sc.nextLine();
            char column = pos.charAt(0);
            int row = Integer.parseInt(pos.substring(1));

            return new ChessPosition(column, row);
        } catch (RuntimeException e) {
            throw new InputMismatchException(e.getMessage());
        }
    }

    /**
     * Prints the board
     *
     * @param pieces
     */
    public static void printBoard(ChessPiece[][] pieces) {
        for (int i = 0; i < pieces.length; i++) {
            System.out.print((8 - i) + " ");
            for (int j = 0; j < pieces.length; j++) {
                printPiece(pieces[i][j], false);
            }
            System.out.println(" ");
        }
        System.out.println("  a b c d e f g h");
    }

    public static void printBoard(ChessPiece[][] pieces, boolean[][] possibleMoves) {
        for (int i = 0; i < pieces.length; i++) {
            System.out.print((8 - i) + " ");
            for (int j = 0; j < pieces.length; j++) {
                printPiece(pieces[i][j], possibleMoves[i][j]);
            }
            System.out.println(" ");
        }
        System.out.println("  a b c d e f g h");
    }

    /**
     * Verify if the piece exists or print a free space
     * @param piece
     */
    private static void printPiece(ChessPiece piece, boolean background) {
        if (background) System.out.print(ANSI_BLUE_BACKGROUND);

        if (piece == null) {
            System.out.print("-" + ANSI_RESET);
        } else {
            if (piece.getColor() == Color.WHITE) {
                System.out.print(ANSI_WHITE + piece + ANSI_RESET);
            } else {
                System.out.print(ANSI_YELLOW + piece + ANSI_RESET);
            }
        }
        System.out.print(" ");
    }

    /**
     * Clear console screen
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Print match info
     * @param match
     * @param captured
     */
    public static void printMatch(ChessMatch match, List<ChessPiece> captured) {
        printBoard(match.getPieces());
        System.out.println();

        printCapturedPieces(captured);
        System.out.println();

        System.out.println("Turn: " + match.getTurn());

        if (!match.getCheckMate()) {
            System.out.println("Waiting Player: " + match.getCurrentPlayer());

            if (match.getCheck()) {
                System.out.println("CHECK!!");
            }
        } else {
            System.out.println("CHECKMATE!!");
            System.out.println("Winner is: " + match.getCurrentPlayer());
        }
    }

    /**
     * Get and print the captured pieces of each opponent
     * @param captured
     */
    private static void printCapturedPieces(List<ChessPiece> captured) {
        List<ChessPiece> white = captured.stream().filter(x -> x.getColor() == Color.WHITE).collect(Collectors.toList());
        List<ChessPiece> black = captured.stream().filter(x -> x.getColor() == Color.BLACK).collect(Collectors.toList());

        System.out.print("Captured Pieces: Whites ");
        System.out.print(ANSI_WHITE);
        System.out.print(Arrays.toString(white.toArray()));
        System.out.print(ANSI_RESET);
        System.out.println();
        System.out.print("Captured Pieces: Black ");
        System.out.print(ANSI_YELLOW);
        System.out.print(Arrays.toString(black.toArray()));
        System.out.println(ANSI_RESET);
    }
}
