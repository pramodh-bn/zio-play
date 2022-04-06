package org.mypackage

import zio.console.{Console, putStrLn}
import zio.random.Random
import zio.{ExitCode, UIO, URIO, ZEnv}
import org.mypackage.domain.*

object TicTacToe extends App {
  def run(args: List[String]): URIO[ZEnv, ExitCode] = ???

  def choosePlayerPiece: URIO[Console, Piece] = ???

  def whichPieceGoesFirst: URIO[Random, Piece] = ???

  def programLoop(state: State): URIO[Random with Console, Unit] = ???

  def drawBoard(board: Board): URIO[Console, Unit] =
    putStrLn {
      Field.All
        .map(field => board.fields.get(field) -> field.value)
        .map {
          case (Some(piece: Piece), _) => piece.toString
          case (None, value)           => value.toString
        }
        .sliding(3, 3)
        .map(fields => s"""${fields.mkString(" || ")} """)
        .mkString("\n=======||========||======\n")
    }

  def step(state: State.Ongoing): URIO[Random with Console, State] = ???

  def getComputerMove(board: Board): URIO[Random with Console, Field] = ???

  def getPlayerMove(board: Board): URIO[Console, Field] = ???

  def takeField(state: State.Ongoing, field: Field): URIO[Console, State] = ???

  def getGameResult(board: Board): UIO[Option[GameResult]] = ???

  def isWinner(board: Board, piece: Piece): UIO[Boolean] = ???

}
