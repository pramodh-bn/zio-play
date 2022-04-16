package org.mypackage.zlayer

import org.mypackage.zlayer.UserDb.UserDbEnv
import org.mypackage.zlayer.UserEmailer.UserEmailerEnv
import org.mypackage.zlayer.ZLayersPlayground.UserSubscription.UserSubscriptionEnv
import zio.{ExitCode, Has, Task, URIO, ZIO, ZLayer}
import zio.console._

case class User(name: String, email: String)
object UserEmailer {
  type UserEmailerEnv = Has[UserEmailer.Service]
  // Service definition
  trait Service {
    def notify(user: User, message: String): Task[Unit] // Asynchronous something like Future, ZIO[Any, Throwable, Unit]
  }
  // Service implementation
  val live: ZLayer[Any, Nothing, UserEmailerEnv] = ZLayer.succeed(new Service {
    override def notify(user: User, message: String): Task[Unit] = Task {
      println(s"[User Emailer] Sending message $message to ${user.email}")
    }
  })

  // front end facing API
  def notify(user: User, message: String): ZIO[UserEmailerEnv, Throwable, Unit] =
    ZIO.accessM(hasService => hasService.get.notify(user, message))
}

object UserDb {
  type UserDbEnv = Has[UserDb.Service]

  trait Service {
    def insert(user: User): Task[Unit]
  }

  val live: ZLayer[Any, Nothing, UserDbEnv] = ZLayer.succeed(new Service {
    override def insert(user: User) = Task {
      println(s"[Data Base] insert into public.user values ${user.email}")
    }
  })

  // front end facing API
  def insert(user: User): ZIO[UserDbEnv, Throwable, Unit] =
    ZIO.accessM(hasService => hasService.get.insert(user))
}
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

  // Horizontal Composition
  // ZLayer[In1, E1, Op1] ++ ZLayer[In2, E2, Op2] => ZLayer[In1 with In2, super(E1, E2), Op1 with Op2]
  val userBackendLayer: ZLayer[Any, Nothing, UserDbEnv with UserEmailerEnv] = UserDb.live ++ UserEmailer.live

  // Vertical Composition
  object UserSubscription {
    type UserSubscriptionEnv = Has[UserSubscription.Service]
    class Service(notifier: UserEmailer.Service, userDb: UserDb.Service) {
      def subscribe(user: User): Task[User] = for {
        _ <- userDb.insert(user)
        _ <- notifier.notify(user, s"Welcome ${user.name}")
      } yield user
    }

    val live: ZLayer[UserEmailerEnv with UserDbEnv, Nothing, UserSubscriptionEnv] = ZLayer.fromServices[UserEmailer.Service, UserDb.Service, UserSubscription.Service] {
      (userEmailer, userDb) => new Service(userEmailer, userDb)
    }

    def subscribe(user: User): ZIO[UserSubscriptionEnv, Throwable, User] = ZIO.accessM(_.get.subscribe(user))
  }


  val userSubscriptionLayer: ZLayer[Any, Nothing, UserSubscriptionEnv] = userBackendLayer >>> UserSubscription.live

  val ram = User("ram", "ram@ram.com")
  val message = "Welcome to Rock the JVM"

  def notifyRam =
    UserEmailer.notify(ram, message) // The kind of effect
      .provideLayer(userBackendLayer) // provide the input for that effect, a composite layer could also be provided like userBackEndlayer
      .exitCode

  //    greeting.exitCode
  // only when you put exitCode, the greeting code will be executed.
  // Otherwise they are just descriptions of effects
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    UserSubscription.subscribe(ram)
      .provideLayer(userSubscriptionLayer)
      .exitCode
}
