package org.mypackage

import zio.console.{Console, getStrLn, putStr, putStrLn}
import zio.random.{Random, nextBoolean, nextIntBounded}
import zio.{ExitCode, UIO, URIO, ZEnv, ZIO}
import org.mypackage.domain._
import org.mypackage.domain.State.{Ongoing, Over}

import java.io.IOException

object TicTacToeMe extends zio.App {
  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (for {
      playerPiece <- choosePlayerPiece
      pieceThatGoesFirst <- whichPieceGoesFirst.tap(piece => putStrLn(s"$piece goes first"))
      initialState = State.Ongoing(
        Board.empty,
        if(playerPiece == Piece.X) Player.Human else Player.Computer,
        pieceThatGoesFirst
      )
      _ <- programLoop(initialState)
    } yield ()).exitCode

  /*def choosePlayerPiece: URIO[Console, Piece] = This def can be converted to val because
  since it is immutable, it will always evaulate to the same value
     putStr("Do you want to be X or O ?").flatMap(_ => getStrLn.orDie)
     * instead of flat map, we can use for comprehension
     *
     */
    val choosePlayerPiece: ZIO[Console, IOException, Piece] =
      for {
      /**_ <- putStr("Do you want to be X or O ?").orDie
      input <- getStrLn.orDie
       There is another way of handling sequencing in Zio and that is zip method.
       this is apart from for, flatmap or map option
       */
      input <- putStr("Do you want to be X or O ?") *> getStrLn.orDie // *> is equal to zipRight

      /**piece <- ZIO.fromOption(Piece.make(input)).orElse(putStrLn("Invalid Input").flatMap(_ => choosePlayerPiece)).orDie
       * orElse could be transformed to <> this is the power of syntactic sugar
       * instead of using flatMap, can use zipRight method or *> zipRight always returns the value from the right operation
      **/
      piece <- ZIO.fromOption(Piece.make(input)) <> (putStrLn("Invalid Input")  *> choosePlayerPiece)
    } yield piece


  val whichPieceGoesFirst: URIO[Random, Piece] = nextBoolean.map {
        // Again making this method val from def as it is immutable
    case true => Piece.X
    case false => Piece.O
  }

  def programLoop(state: State): ZIO[Random with Console, IOException, Unit] =
    state match {
      case state @ Ongoing(board, _, _) => drawBoard(board) *> step(state).flatMap(nextState => programLoop(nextState))
      case Over(board)                  => drawBoard(board)
    }

  def drawBoard(board: Board): ZIO[Console, IOException, Unit] =
    putStrLn {
      Field.All
        // (x) -> y way of creating tuples in Scala
        .map(field => board.fields.get(field) -> field.value)
        .map {
          case (Some(piece: Piece), _) => piece.toString
          case (None, value) => value.toString
        }
        .sliding(3, 3)
        .map(fields => s""" ${fields.mkString(" || ")} """)
        .mkString("\n=======||========||======\n")
    }

  def step(state: State.Ongoing): ZIO[Random with Console, IOException, State] =
    for {
      nextMove <- if (state.isComputerTurn) getComputerMove(state.board) else getPlayerMove(state.board)
      nextState <- takeField(state, nextMove)
    } yield nextState

  def getComputerMove(board: Board): ZIO[Random with Console, IOException, Field] =
    nextIntBounded(board.unOccupiedFields.size)
      .map(board.unOccupiedFields(_))
      .tap(_ => putStrLn("Waiting for computer's move, please press Enter ...")) <* getStrLn.orDie

  def getPlayerMove(board: Board): ZIO[Console, IOException, Field] =
    for {
      input <- putStr("What's your next move? [1-9]") *> getStrLn.orDie
      tmpField <- ZIO.fromOption(Field.make(input)) <> (putStrLn("Invalid input") *> getPlayerMove(board))
      field <- if (board.fieldIsNotFree(tmpField)) putStrLn("That field has been already used") *>  getPlayerMove(board)
      else ZIO.succeed(tmpField)
    } yield field

  def takeField(state: State.Ongoing, field: Field): ZIO[Console, IOException, State] =
    for {
      updatedBoard <- ZIO.succeed(state.board.updated(field, state.turn))
      updatedTurn <- ZIO.succeed(state.turn.next)
      gameResult <- getGameResult(updatedBoard)
      nextState <- gameResult match {
        case Some(gameResult: GameResult) => putStrLn(gameResult.show) *> ZIO.succeed(State.Over(updatedBoard))
        case None => ZIO.succeed(state.copy(board = updatedBoard, turn = updatedTurn))
      }
    } yield nextState

  def getGameResult(board: Board): UIO[Option[GameResult]] =
    for {
      crossWin <- isWinner(board, Piece.X)
      noughtWin <- isWinner(board, Piece.O)
      gameResult <- if (crossWin && noughtWin)
        ZIO.die(new IllegalStateException("It should not be possible for both players to win!"))
      else if (crossWin) UIO.succeed(GameResult.Win(Piece.X)).asSome
      else if (noughtWin) UIO.succeed(GameResult.Win(Piece.O)).asSome
      else if (board.isFull) UIO.succeed(GameResult.Draw).asSome
      else UIO.none
    } yield gameResult

  def isWinner(board: Board, piece: Piece): UIO[Boolean] =
    Board.winnerCombinations.map(combinations => combinations.exists(_ subsetOf board.fieldsOccupiedByPiece(piece)))

}
