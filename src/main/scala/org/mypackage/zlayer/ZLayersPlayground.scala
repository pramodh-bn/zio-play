package org.mypackage.zlayer

import zio.ZIO
import zio.console._

object ZLayersPlayground extends zio.App {
  // Zio Type, Zio instances are called effects (printing, sockets, reading files)
  // Zio describes effects
  // ZIO[-R, +E, +A] R is input, E exception, A value
  // R => Either[E, A]

  val meaningOfLife = ZIO.succeed(42)
  val aFailure = ZIO.fail("Something went wrong")

  val greeting = for {
    _ <- putStrLn("Hi, What is your name?")
    name <- getStrLn
    _ <- putStrLn(s"Hello $name, Welcome")
  } yield ()

  override def run(args: List[String]) = greeting.exitCode
  // only when you put exitCode, the greeting code will be executed.
  // Otherwise they are just descriptions of effects

}
