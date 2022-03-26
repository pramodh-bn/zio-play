package org.mypackage

import zio.{UIO, ZIO}

object ZioFibers {
  // Effect Pattern
  // computation = value + effect in the world
  // substitution model
  val aValue = {
    println("hello, Scala")
    42
  }

  def incrementValue(x: Int) = x + 1
  incrementValue(42) == incrementValue(aValue)

  // IO Monad
  // Zio datastructure R for environment, E for exception or error type, A produce value type of A
  // ZIO[R, E, A]
  val zmol: UIO[Int] = ZIO.succeed(42)

  // concurrency - daily routine of Bob

}
