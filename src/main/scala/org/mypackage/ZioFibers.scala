package org.mypackage

import zio.ZIO.debug
import zio.{ExitCode, UIO, URIO, ZIO}
import zio.duration.*

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

  def concurrentRoutine() = for {
    showerFiber <- showerTime.debug(printThread).fork
    boilingWaterFiber <- boilingWater.debug(printThread).fork
    zippedFiber = showerFiber.zip(boilingWaterFiber)
    result <- zippedFiber.join.debug(printThread)
    _ <- ZIO.succeed(s"$result done").debug(printThread) *> prepareCoffee.debug(printThread)
    // *> = then operator or also sequential operator
  } yield()

  /*
  * Now we are going to talk about interruptions
  *
  *
  * */

  val callFromAlice = ZIO.succeed("Call from Alice")
  val boilingWaterWithTime = boilingWater.debug(printThread) *> ZIO.sleep(5.seconds) *> ZIO.succeed("Boiled Water ready")

  def concurrentRoutineWithAliceCall() = for {
    _ <- showerTime.debug(printThread)
    boilingFiber <- boilingWaterWithTime.fork
    _ <- callFromAlice.debug(printThread).fork *> ZIO.sleep(2.seconds) *> boilingFiber.interrupt.debug(printThread)
    _ <- ZIO.succeed("No Coffee, going with Alice").debug(printThread)
  } yield ()

  override def run(args: List[String]) =
    synchronousRoutine().exitCode
    concurrentShowerWhileBoilingWater().exitCode
    concurrentRoutine().exitCode
    concurrentRoutineWithAliceCall().exitCode

}
