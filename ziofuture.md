https://www.youtube.com/watch?v=u3pgyEiu9eU

#What ZIO
##CORE
Build Scalable, concurrent, non-blocking, resilient applications that never leak resources

## Streams
Build concurrent, streaming applications with back-pressuring and resource safety

## STM
Build non-blocking concurrent structures with transactional guarantees

## Testkit
Write fast, deterministic tests that do not require interacting with external systems

`Zio gives you superpowers, making you more productive and happier than ever before building complex modern applications.`

* Scalable
* Async
* Parallel
* Concurrent
* Leak-free
* efficient
* streaming
* stable 
* resilient

OS Threads -> JVM Threads
A Thread has
* Explicit Shutdown
* Preallocated Stack
* GC Root
* Context Switching 
* Maximum threads 10000

Imagine a Java Thread can be split into say further down into small subthreads called fibers. 
They are known as ZIO fibers. (before Loom's arrival)
Advantages of a ZIO fiber construct or abstraction is that
* Garbage collected
* Dynamic Stack
* No GC Root
* Less Context Switching
* Maximum 1,000,000 Fibers

Another advantage you never have to use callback again
```scala
// Legacy callback
s3Get(key,
  error => log(error),
  value => s3Put(key, enrichProfile(value)),
  error => log(error),
  _ => ())

// Zio Style, Asynchronous but with semantic blocking
val enrich =
  for {
    value <- s3Get(key)
    _     <- s3Put(key, enrichProfile(value))
  } yield ()
  
// Trivially parallelize with precise control:
ZIO.forEachParN(20)(urls) {
  url => for {
    data <- load(url)
    json <- parseToJson(data)
    transformed <- transform(json)
  } yield transformed
}

// Leverage non-blocking, blazing fast concurrent
// structures like Queue, Ref & more:
def startConsumers(n: Int, queue: Queue[Work]) = {
  val worker = queue.take.flatMap(dowork(_)).forever
  val workers = List.fill(n)(worker)
  
  ZIO.forkAll(workers)
}
#Concurrent
```
```scala
// Commit conditional transactions without locks
// or condition variables, free of race conditions
// and deadlocks.
def acquireConnection =
  STM.atomically {
    for {
      connection <- available.get.collect {
                        case head::Nil
                      }
      _          <- available.update(_.drop(1))
      _          <- used.update(connection :: _)
    } yield connection
  }
```

# Leak-free
```scala
// Package up acquire & release into a Managed
// resource, with no possibility of leaks:
val managedFile = Managed.make(open(file))(close(_))
managedFile.use {
  resource => (for{
    data <- read(resource)
    _    <- aggregateData(data)
  } yield ()).forever
}
```
# Efficient
```scala
// Race two effects, cancelling the loser:
val geoLookup = geoIpService.lookup(ipAddress)
val dbLookup = userRepo.getProfile(userId).map(_.location.toLatLong)

val fastest = geoLookup.race(dbLookup)

// Timeout slow effects, interrupting their execution:
slowDbQuery.timeout(60.seconds)
```
# Streaming
```scala
import zio.stream.{ZSink, ZStream}

import java.nio.file.Files
// Trivially construct complex transformation pipelines
// with concurrency, resources and more, which operate 
// on infinite data in constant memory:
val path = "<some path>"
val wordCount = ZStream.fromInputStream(Files.newInputStream(path))
  .transduce(ZSink.utf8Decode)
  .transduce(ZSink.splitWords)
  .run(ZSink.count)
```
# Testable
```scala
import zio.console.{getStrLn, putStrLn}
import zio.duration.durationInt
// Write fast, deterministic unit tests on any program,
// even interactive, non-deterministic ones:
val program =
    for {
      _    <- putStrLn("What is your name?")
      name <- getStrLn
      _    <- putStrLn("I will wait " + name.length + " seconds, " + name)
      _    <- clock.sleep(name.length.seconds)
    } yield ()
    
val deterministicResults = program.provideLayer(testServices)
```

# Resilient
```scala
import zio.Schedule
import zio.duration.durationInt
// Guided by types, build resilient apps:
val retryPolicy =
  (Schedule.exponential(10.millis)
    .whileOutput(_ <- 1.second) andThen
      Schedule.spaced(60.seconds)) && 
  Schedule.recurs(100)

val result = callFlakyApi(request).retry(retryPolicy)

// Let the compiler tell you what can fail and why:
val infallible = result.catchAll(_ => fallback)
```
# Compositional
All the features compose, you can mix and match like lego blocks.
Good functional design lets you solve problems in a compositional way.

```scala
val managedData = Managed.make(open(url))(close(_))
managedData.use { data => 
  searchBreadth(data)
}
// Do it parallel
ZIO.foreach(urls) { url =>
  val managedData = Managed.make(open(url))(close(_))
  managedData.use { data =>
    searchBreadth(data)
  }
}

// Do it with only 20 parallel 
ZIO.foreachParN(20)(urls) { url =>
  val managedData = Managed.make(open(url))(close(_))
  managedData.use { data =>
    searchBreadth(data)
  }
}

// Some of the URLs are flaky so why don't you add retry?
val policy = Schedule.recurs(100)
ZIO.foreachParN(20)(urls) { url =>
  val managedData = Managed.make(open(url))(close(_))
  val robustData = managedData.retry(policy)
  managedData.use { data =>
    searchBreadth(data)
  }
}

// Add exponential backoff to retrying
val policy = Schedule.recurs(100) && Schedule.exponential(10.millis)
ZIO.foreachParN(20)(urls) { url =>
  val managedData = Managed.make(open(url))(close(_))
  val robustData = managedData.retry(policy)
  robustData.use { data =>
    searchBreadth(data)
  }
}

// Add time out to retry
val policy = Schedule.recurs(100) && Schedule.exponential(10.millis)
ZIO.foreachParN(20)(urls) { url =>
  val managedData = Managed.make(open(url))(close(_))
  val robustData = managedData.retry(policy).timeoutFail(30.seconds)
  robustData.use { data =>
    searchBreadth(data)
  }
}

// Use the faster of 2 search methods
val policy = Schedule.recurs(100) && Schedule.exponential(10.millis)
ZIO.foreachParN(20)(urls) { url =>
  val managedData = Managed.make(open(url))(close(_))
  val robustData = managedData.retry(policy).timeoutFail(30.seconds)
  robustData.use { data =>
    searchBreadth(data).race(breadthSearch(data))
  }
}

// Time out the whole process
val policy = Schedule.recurs(100) && Schedule.exponential(10.millis)
ZIO.foreachParN(20)(urls) { url =>
  val managedData = Managed.make(open(url))(close(_))
  val robustData = managedData.retry(policy).timeoutFail(30.seconds)
  robustData.use { data =>
    searchBreadth(data).race(breadthSearch(data))
  }
}.timeout(10.minutes)

```

# Layers
```scala
final case class UserRepository(database: Database) {
  def getUserById(id: Id): Task[User] = ...
}
object userRepository {
  val live = UserRepository(_).toLayer
}
...
val myEffect: ZIO[Has[UserRepository]] Throwable User] = 
  ZIO.serviceWith(_.getUserById(id))

val complexLayer =
  ( for {
      ref      <- Ref.make(state)
      database <- ZIO.service[Database]
      logging  <- ZIO.service[Logging]
      config   <- ZIO.service[Config]
  } yield MyService(ref, database, logging, config)).toLayer

val managedLayer =
  ( for {
    ref      <- Ref.make(state)
    database <- ZIO.service[Database]
    logging  <- ZIO.service[Logging]
    config   <- ZIO.service[Config]
    service  <- MyService(ref, database, logging, config).toManaged_
    _        <- service.initialize.toManaged_
    _        <- ZManaged.finalizer(service.destroy)
  }) yield Service).toLayer
```


