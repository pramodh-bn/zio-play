package org.mypackage

import zio.{ExitCode, UIO, URIO, ZIO}

object ZioFibers extends zio.App {
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
  val showerTime = ZIO.succeed("Bob Taking a Shower")
  val boilingWater = ZIO.succeed("Boiling some water")
  val prepareCoffee = ZIO.succeed("Prepare some coffee")

  // These 3 processes are synchronous
  // Create a method synchronous routine
  // ZIO follows monad rules
  def printThread = s"[${Thread.currentThread().getName}]"

  def synchronousRoutine() = for {
    _ <- ZIO.succeed(println("Synchronous"))
    _ <- showerTime.debug(printThread)
    _ <- boilingWater.debug(printThread)
    _ <- prepareCoffee.debug(printThread)
  } yield ()

  // fiber concept - schedulable computation
  // very lightweight ds spawned or created
  // upto zio to schedule it to result in parallelism
  // notion of virtual thread
  // Fiber[E,A]

  def concurrentShowerWhileBoilingWater() = for {
    _ <- ZIO.succeed(println("in Concurrent"))
    _ <- showerTime.debug(printThread).fork
    _ <- boilingWater.debug(printThread)
    _ <- prepareCoffee.debug(printThread)
  } yield()



  override def run(args: List[String]) =
    synchronousRoutine().exitCode
    concurrentShowerWhileBoilingWater().exitCode

}