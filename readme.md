Going through Scala With Zio: Introduction to Fibers
https://www.youtube.com/watch?v=0OTYAVWUnU4

Functional Programming 101 with Scala and ZIO
https://www.youtube.com/watch?v=gZMwjYTkK9k

Functional Programming basic concepts
ZIO Basic concepts

What is Functional programming?
A paradigm, where programs are composition of pure functions

What is a pure function?
A pure function must be total, deterministic and depends on inputs only, no side effects

**Benefits of Functional Programming**
* Local Reasoning
* Referential Transparency --> Fearless Refactoring
* Conciseness --> Fewer bugs
* Easier to test
* Applications behave more predictably
* Allows us to write correct parallel programs

But Side effects are going to be always there
The trick Don't write functions that interact with the outside world. 
Write functions that describe interactions instead
* Descriptions of interactions with the outside world
* Immutable values that can serve as inputs and outputs of pure functions
* They are executed only at the end of the world

**ZIO the library**
* Asynchronous and Concurrent -> Fiber based model
* Resilient --> Leverages the power of scala's Type system!
* Efficient --> Apps that never leak resources
* Easy to understand and test --> Thanks to superior composability!

**ZIO the data type**
ZIO[-R, +E, +A]
* Core type of the ZIO library
* Functional effect

A good mental model is the following
R => Either[E, A]
This means that a ZIO effect:
* Needs an environment of type R to run
* It may fail with an error of type E
* Or, it may complete successfully, returning a value of type A

Common Aliases:
* Task[+A]          = ZIO[Any, Throwable, +A]
* UIO[+A]           = ZIO[Any, Nothing, +A]
* RIO[-R, +A]       = ZIO[-R, Throwable, +A]
* IO[+E, +A]        = ZIO[Any, E, A]
* URIO[-R, +A]      = ZIO[R, Nothing, A]

structuring code with Zio and Zlayers
https://www.youtube.com/watch?v=PaogLRrYo64

https://www.youtube.com/watch?v=6A1SA5Be9qw
ZIO is a library
ZIO is a zero dependency library for asynchronous and concurrent programming in Scala

# ZIO is Lazy
```val sayHello: ZIO[Any, Nothing, Unit] = 
    ZIO.effectTotal(println("Hello World!"))
Runtime.unsafe(sayHello)
```
- ZIO is aggressively lazy
- sayHello is just a description of a computation
- Nothing is printed until we call unsafeRun

# ZIO is composable
```
val zioTweets:ZIO[Any, Throwable, List[String]] = ???
zioTweets.retry(Schedule.exponential(1.second))
```
-- Easy to combine effects to solve problems
-- Effects are descriptions so we can run them again
-- Can't do with Future

# ZIO preserves information
```
sealed trait ConfigError
final case class IOError(message: String)  extends ConfigError
final case class ParseError(message: String) extends ConfigError

def readFile(path: String): ZIO[Any, IOError, String] = ???
def parseConfig(s: String): ZIO[Any, ParseError, String] = ???

def readConfig(path: String): ZIO[Any, ConfigError, Config] = 
    readFile.flatMap(parseConfig)
val config: ZIO[Any, Nothing, Config] = 
    readConfig("config.json").orElseSucceed(defaultConfig)
```
-- We can express exactly how our effects can fail
-- Composed error types automatically inferred
-- We know whether our effects can fail at all

# ZIO is concurrent
```scala
trait ZIO[-R, +E, +A] {
  def fork: ZIO[R, Nothing, Fiber[E, A]]
}
trait Fiber[+E, +A] {
  def join: ZIO[Any, E, A]
}
```
-- Fiber based concurrency model
-- Have hundreds of thousands of fibers at a time
-- Semantically block but never block underlying threads

```scala
def getUserById(id: Int): ZIO[Any, Throwable, User] = ???

val getUsers: ZIO[Any, Throwable, List[User]] = 
  ZIO.foreachPar(ids)(getUserById)
```
-- Perform effects in parallel and collect the results
-- Automatically interrupt others if one fails
-- Control parallelism with foreachParN

```
val getDataFromEastCoast: ZIO[Any, Throwable, List[Result]] = ???
val getDataFromWestCoast: ZIO[Any, Throwable, List[Result]] = ???

val result: ZIO[Any, Throwable, List[Result]] = 
    getDataFromEastCoast.race(getDataFromWestCoast)
```
-- Return first effect to succeed
-- Automatically interrupt the loser
-- Race many effects with raceAll

# ZIO is Resource Safe
```scala
trait Fiber[+E, +A] {
  def interrupt: ZIO[Any, Nothing, Exit[E, A]]
}
```
-- Necessary for safe resource usage
-- Stop doing work if we don't need result anymore
-- Can't interrupt a Future without ZIO
```scala
trait ZIO[-R, +E, +A] {
  def ensuring[R1 <: R](finalizer: ZIO[R1, Nothing, A]): ZIO[R1, E, A]
}
```
-- The other necessary condition for safe resource usage
-- Finalizer will run no matter how effect terminates
-- Other combinators such as bracket built on top of this

```scala
val connection: ZManaged[Any, Throwable, Connection]
def postgres(connection: Connection): ZManaged[Any, Throwable, UserRepo] = ???

val userRepo: ZManaged[Any, Throwable, UserRepo] = connection.flatMap(postgres)
```
-- Resources guaranteed to be acquired in order
-- Resources guaranteed to be released in order
-- Combinators for parallelism and many other scenarios

# ZIO is Testable
```scala
trait ZIO[-R, +E, +A] {
  def provider(r: R): IO[E, A]
}

object ZIO {
  def environment[R]: ZIO[R, Nothing, R]
}
```
-- Express the services our application depends on
-- Add a dependency with ZIO#Environment
-- Eliminate a dependency with ZIO#provide

```scala
val alarm: ZIO[Clock with Console, Throwable, Unit] = for {
  time <- console.readLine.mapEffect(_.toInt)
  _    <- clock.sleep(duration.seconds)
  _    <- console.putStrLn("Wake Up!")
} yield ()
```
-- Environment type automatically inferred
-- See all the services our application depends con
-- Propagate dependencies throughout our application

```scala
for {
  _ <- TestConsole.feedLine("5")
  _ <- alarm
  _ <- TestClock.adjust(5.seconds)
  output <- TestConsole.output 
} yield assert(output)(equalTo(Vector("Wake up!\n")))
```
-- Provide alternative implementations of services
-- Using test implementation from ZIO test here
-- No real time or console interaction needed

# ZIO is Focused on Solving Your Problems
ZIO includes a variety of concurrency primitives:
-- Ref - functional equivalent of atomic reference
-- Promise - single value communication
-- Queue - multiple value communication
-- Semaphore - control level concurrency
-- Schedule - manage repeats and retries

Software Transactional memory for tackling your toughest concurrent problems
```scala
final class TPriorityQueue[K, V] private (private val tref: TRef[SortedMap[K ::[V]]]) extends AnyVal {
  def offer(key: K, value: V): STM[Nothing, Unit] =
    tref.update { map => 
      map.get(key) match {
        case None             => map + (key -> :: (value, Nil))
        case Some(values)     => map + (key -> :: (value, values))
      }
    }
    
  def take: STM[Nothing, V] =
    tref.get.flatMap {
      map.headOption match {
        case None                                      => ZSTM.retry
        case Some((key, value :: (values @ ::(_, _)))) => tref.update(_ + (key -> values)).as(value)
        case Some((key, value :: _))                   => tref.update(_ - key).as(value)
      }
    }
}
```
ZIO Stream is pull based streaming solution with deep integration with ZIO
```scala
ZStream.effectAsync { cb => 
  feed.register { tweets => 
    ZIO.succeed(Chunk.fromIterable(tweets))
  }
}
  .filter(_user = "jdegoes")
  .map(_.text)
  .tap(console.putStrLn)
  .run(writeToFile)
```
Ecosystem
-- Caliban
-- ZIO Kafka
-- ZIO Config
-- ZIO Logging
-- And Many More

# Migrating to ZIO
# How do we get there?
This all sounds great, but we have huge legacy code bases.
-- Start small
-- Run your effects
-- Use type aliases
-- Take advantage of interop packages
-- Ask for help

# Start Small
You don't have to migrate your whole application at one.
-- Pick one part of your codebase
-- That will be your initial "island" of ZIO
-- Lift inputs into ZIO
-- Run outputs to your legacy type
-- Slowly expand the edges of your "island"

# Run your Effects
```scala
val runtime = Runtime.default
runtime.unsafeRun(zio)
```
Don't be afraid to use unsafeRun:
-- 100% of your application is probably "unsafe" today
-- Use at boundary between ZIO and legacy code
-- Create once at the top of your application

# Use Type Aliases
type Task[+A] = ZIO[Any, Throwable, A]

Taking advantage of all of ZIO's features is a process:
-- Existing code doesn't have an environment type
-- Probably has error type fixed to Throwable
-- So Task is closest to your existing code 
-- Over time use IO and UIO for typed errors
-- Then use ZIO for dependency injection

# Take advantage of Interop Packages
Interop packages available for:
-- Scala Future
-- Java Future and friends
-- Twitter Future
-- Monix
-- Cats Effect

# Ask for Help
We're in this together
-- Ask on Discord
-- Ask at events like this
-- Ask for what you want to see












