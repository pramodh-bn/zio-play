package org.mypackage.domain

import scala.annotation.meta.field

final case class Board(fields: Map[field, Piece]) {
  self =>
}
object Board {

}
