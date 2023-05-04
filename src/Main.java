import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.enums.PromotingTypes;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ChessMatch chessMatch = new ChessMatch();
        List<ChessPiece> captured = new ArrayList<>();

        while (!chessMatch.getCheckMate()) {
            UI.clearScreen();

            try {
                UI.printMatch(chessMatch, captured);
                System.out.println();

                System.out.print("Source: ");
                ChessPosition source = UI.readChessPosition(sc);

                boolean[][] possibleMoves = chessMatch.possibleMoves(source);
                UI.printBoard(chessMatch.getPieces(), possibleMoves);

                System.out.print("Target: ");
                ChessPosition target = UI.readChessPosition(sc);
                UI.clearScreen();
                chessMatch.getPieces();

                ChessPiece capturedPiece = chessMatch.performChessMove(source, target);
                if (capturedPiece != null) captured.add(capturedPiece);

                if (chessMatch.getPromoted() != null) {
                    System.out.print("Enter piece for promotion (B/N/R/Q): ");
                    String type = sc.nextLine().toUpperCase();

                    while (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
                        System.out.print("Insert a valid value! Enter only B or N or R or Q: ");
                        type = sc.nextLine().toUpperCase();
                    }

                    chessMatch.replacePromotedPiece(type);
                }

            } catch (ChessException | InputMismatchException | InvalidParameterException e) {
                System.out.println(e.getMessage());
                sc.nextLine();
            }
        }

        UI.clearScreen();
        UI.printMatch(chessMatch, captured);
    }
}