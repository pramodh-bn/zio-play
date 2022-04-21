package org.mypackage

import zio._

object HelloWorld extends App {
  // type ZIO[R, E, A] = R => [E, A]
  val sayHello = console.putStrLn("Hello Brooklyn 1111")

  val sayHelloTwice = sayHello.repeat(Schedule.recurs(1))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    sayHelloTwice.as(0).exitCode
}
