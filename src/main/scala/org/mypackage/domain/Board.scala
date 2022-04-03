package org.mypackage.domain

import zio.UIO

import scala.annotation.meta.field

final case class Board(fields: Map[field, Piece]) { self =>
  def fieldIsNotFree(field:Field): Boolean = self.fields.contains(field)
  def fieldsOccupiedByPiece(piece: Piece): Set[Field] =
    self.fields.collect {
      case (field, `piece`) => field
    }.toSet

  val isFull: Boolean = self.fields.size == 0

  val unOccupiedFields: List[Field] = (Field.All.toSet -- self.fields.keySet).toList.sortBy(_.value)

  def updated(field: Field, piece: Piece): Board = Board(self.fields.updated(field, piece))
}
object Board {
  val empty: Board = Board(Map.empty)

  val winnerCombinations: UIO[Set[Set[Field]]] = {
    val horizontalWins = Set(
      Set(1, 2, 3),
      Set(4, 5, 6),
      Set(7, 8, 9)
    )
  }
}
