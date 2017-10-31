# Introduction

Learning how to use [Eff in Scala](https://github.com/atnos-org/eff), let alone learning how it works on the inside, is quite the challenge for me and I believe for many people too. I feel that there's quite the chasm from running a few easy examples to putting together a full app using Eff.

However, Eff has a great amount of documentation for the initiated FP user in Scala. In fact, there's even a set of exercises in [*"Getting Work Done With Extensible Effects"*](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/) that guide you in a methodical way towards understanding how you might use Eff in a real, non-trivial app. There's even [a great presentation](https://vimeo.com/channels/flatmap2016/165927840) about a practical way to use Eff in bigger projects, though I've yet to see the usage of the overall framework described in it in a sample application. I hope there's one out there and I hope I just missed it. Or maybe it's not needed at all.

That said, I think there's still a part of that chasm that needs to be covered. It's somewhere in between someone starting out with FP in Scala and the aforementioned exercises by Ben Hutchinson. I think there's some value in going back to the simplest problems and seeing what Eff looks like there.

I'm planning to have this series span the first set of adriannn's [Simple Programming Problems](https://adriann.github.io/programming_problems.html), namely the *Elementary Problems*. [This series' code](https://github.com/zach-albia/learning-eff) is available on Github. We kick things off with our first problem:

> Write a program that prints ‘Hello World’ to the screen.

# Many ways to say Hello

## The simplest way

Our first jab at the problem begins with the simplest possible example that we're taught when we first learn to program in any language. Our first problem happens to be Scala's Hello World:

```scala
object Prob01Pass01 {
  def main(args: Array[String]): Unit = {
    println("Hello World")
  }
}
```

It's simple, but inflexible. You can only ever print to the console here. Period. What if we wanted to print `"Hello World"` to a file? Also, how do we know it works? We run it, and it prints `Hello World` to the console. But we did have to look at the characters it prints out. And we did have to make sure *every single character* lines up to make the string `"Hello World"`. We aren't exactly perfect at reading either, and our eyes can trip and think we're reading the right string when we're not!

If it can happen to us in a simple "Hello World", imagine what would happen if we had to do it for any bigger program. We say we **cannot test** `main`. It's opaque to any testing effort unless we go to great lengths just to read the output on the console itself. Let's see if we can test one of the simplest programs ever.

## The *testable* way

At the heart of software development is getting things done *automatically*. That includes testing. We have to restructure our tiny Hello World app so we can run a test:

1. to see if our program compiles,
2. to see if it works as expected without having to look at our program's console output, and
3. to be able to automatically do both 1 & 2 repeatedly.

Here is one way to refactor our code:

```scala
object Prob01Pass02 {
  val defaultPrintln: String => Unit = Predef.println

  def main(args: Array[String]): Unit =
    printHelloWorld(defaultPrintln)

  def printHelloWorld(println: String => Unit): Unit =
    println("Hello World")
}
```

Note that `println` is now totally *divorced* from the concept of the console. We might as well write `"Hello World"` in crop circles, given the right `println`. More practically, now we can write code to test `printHelloWorld`.

```scala
import Prob01Pass01.printHelloWorld
import org.scalatest._

class Prob01Spec extends FlatSpec with Matchers {

  val mockPrintln: String => Unit = _ should be ("Hello World")

  "our second pass at printHelloWorld" should
      "print hello world to a println function" in {
    Prob01Pass02.printHelloWorld(_ should be ("Hello World"))
  }
}
```

Our test code *intercepts* what's passed to our function. This allows us to automatically confirm that, for whatever way we can print `"Hello World"`, `println` will receive *exactly* the string `"Hello World"` and *nothing else*.

## The *Reader* way

`printHelloWorld` is a function that takes a `println` function and runs it on `"Hello World"`, giving back `Unit`. Reader is just an effect in the form of a function taking one parameter which yields our value, the return value. Let's look at our testable code in terms of Reader:

```scala
import cats.data.Reader

object Prob01Pass03 {
  val defaultPrintln: String => Unit = Predef.println

  def main(args: Array[String]): Unit = {
    val program = printHelloWorld

    // at the end of the world
    program.run(defaultPrintln)
  }

  val printHelloWorld: Reader[String => Unit, Unit] =
    Reader { println => println("Hello World") }
}
```

Our code is very similar to our second pass. Note that we only had to change a little bit of code Since `Reader` and a function with one parameter, namely `Function1[A, B]` or `A => B` in Scala, are practically one and the same. In our usage of `Reader` here, we mean it to *delay reading or asking for*  a `println`  function in order to *return* `Unit`, i.e. to make some side effect(s). I think "Reader" is a weird name for it. It'd probably make more sense as the "Context", "Config" or "Dependency". But what do I know?

`Reader` also allows us to write tests:

```scala
"our third pass at printHelloWorld" should
    "be a flexible hello world program" in {
  val program = Prob01Pass03.printHelloWorld

  program.run(_ should be ("Hello World"))
}
```

By using `Reader` , in `main` we end up with a pure *description* of our "Hello World" program in `program`. A pure program that we can interpret, or `run` any way we want.

It's pure in the sense that we can call `printHelloWorld` any number of times and it won't have any side effects, and will *always* return the same program. I've read a lot of material with the idea that *functional programs are descriptions of programs that you run*. This looks like it fits the bill.

We can interpret our program in another way with the use of a *"fake"* or a _"mock"_ `println`--one that just checks if it got passed `"Hello World"`. We could even interpret `printHelloWorld` to print the line to a file, or write a smoke message in the sky with an autonomous drone given the right `println` function.

What does Reader buy us? There is some extra cognitive overhead here for sure. We've had to learn Reader when we could have just stuck to our second pass by just passing functions. We've gained *the flexibility and the concept* of a pure program, one that could be interpreted in many ways, one of which is useful for testing. What else can we gain?

## The *Reader-Writer* way

Say, for our fourth pass, we wanted to know how fast "Hello World" runs on our computer. The simplest, non-testable way looks something like this:

```scala
object Prob01Pass04 {
  def main(args: Array[String]): Unit = {
    val start = System.currentTimeMillis()
    println("Hello World")
    val time = System.currentTimeMillis() - start
    println(s"Ran hello world in $time ms.")
  }
}
```

Our problem with this code is that it doesn't easily allow us to change *how* we print "Hello World", as well as *how* we read the time. `System.getCurrentTimeMillis` and `println` here are impure functions. `System.getCurrentTimeMillis` observes the side effect of passing time while `println` has the side effect of writing to a console. Consequently, besides testing Hello World, we can't test if it's logging the right amount of time.

Let's make an `AppConfig` trait and give it the default console and system time version that we can use in `main`:

```scala
trait AppConfig {
  @throws(classOf[Exception])
  def println(s: String): Unit
  def startTimeMillis: Long
  def endTimeMillis: Long
}

object AppConfig {
  val default: AppConfig = new AppConfig {
    def println(s: String): Unit = Predef.println(s)
    def startTimeMillis: Long = System.currentTimeMillis()
    def endTimeMillis: Long = System.currentTimeMillis()
  }
}
```

Note that we've declared that our `println` can fail. This is due to `println` being flexible enough for this to happen. Now that we have `AppConfig` nailed down, let's see what we can do with `Reader` and `Writer`:

```scala
import cats._, cats.data._, cats.implicits._
import Writer._

object Prob01Pass05 {

  def main(args: Array[String]): Unit = {
    val program = printHelloWorld

    // at the end of the world
    val (timeElapsed, _) = program.run(AppConfig.default).run
    println(s"Ran hello world in $timeElapsed ms.")
  }

  val printHelloWorld: Reader[AppConfig, Writer[Long, Unit]] =
    Reader { appCfg =>
      for {
        _ <- tell(-appCfg.startTimeMillis)
        _ <- value[Long, Unit](appCfg.println("Hello World"))
        _ <- tell(appCfg.endTimeMillis)
      } yield ()
    }
}
```

Looking at `main`, `printHelloWorld` is run twice. That's because our program now has *two* effects, `Reader` and `Writer`. Our third pass only had `Reader` so we've only had to call `run` once. In FP, pure programs like `printHelloWorld` will be run or *interpreted* as many times as it has effects. Once we *interpret* all those effects, we end up with the output of our pure program, in this case, a `(Long, Unit)` tuple, the first part of which contains our `timeElapsed` in milliseconds.

With `AppConfig`, `Reader`, and `Writer`, we've decoupled our code from the console and the system clock. Note that we've had to manually `println` our `timeElapsed` report in `main`, since the second `run` produces a pure log of `timeElapsed`.

With this, we can write a test for our fifth pass that checks both printing Hello World and logging the time.

```scala
object MockAppConfig extends AppConfig {
  def println(s: String): Unit = s should be ("Hello World")
  def startTime: Long = 1
  def endTime: Long = 3
}

"our fifth pass at printHelloWorld" should
    "be a flexible, timed hello world program" in {
  val program = Prob01Pass05.printHelloWorld

  val (timeElapsed, _) = program.run(MockAppConfig).run
  timeElapsed should be (2)
}
```

Let's take a closer look at our `printHelloWorld` program itself.

```scala
val printHelloWorld: Reader[AppConfig, Writer[Long, Unit]] =
  Reader { appCfg =>
    for {
      _ <- tell(-appCfg.startTime)
      _ <- value[Long, Unit](appCfg.println("Hello World"))
      _ <- tell(appCfg.endTime)
    } yield ()
  }
```

We have a stack of two effects, Reader & Writer. Our `Writer` needs access to `appCfg` to log the time, which means `Reader` has to be on the top of our effect stack. Note how we've had to manually stack our `Writer` program inside our `Reader`. Though we did also gain the ability for our computation (printing Hello World, mind you), to have multiple effects.

If we had to stack more effects, our pure program code would get more and more unwieldy and shift further and further to the right. To illustrate this, let's have a go at adding error handling to our stack.

## The _Reader-Writer-Either_ Way

Our `println` can throw an exception, as declared in `AppConfig`. After all, we could have our pure program tell a rover on Mars to print "Hello World" on Martian sand, again given the right `println`function. We could very easily lose our connection to the rover! As programmers, we have to be prepared for this scenario. Let's add error-handling to our stack.

```scala
import cats.data._, cats.implicits._
import Writer._

object Prob01Pass06 {

  def main(args: Array[String]): Unit = {
    val program = printHelloWorld

    // at the end of the world
    val (timeElapsed, result) = program.run(AppConfig.default).run
    result match {
      case Left(e) => println(s"Hello world failed: $e")
      case _       => println(s"Ran hello world in $timeElapsed ms.")
    }
  }

  type ReaderWriterEither[A] =
    Reader[AppConfig, Writer[Long, Either[Throwable, A]]]

  val printHelloWorld: ReaderWriterEither[Unit] =
    Reader { appCfg =>
      for {
        _ <- tell(-appCfg.startTimeMillis)
        e <- value[Long, Either[Throwable, Unit]] {
          try {
            Right(appCfg.println("Hello World"))
          } catch {
            case e: Exception => Left(e)
          }
        }
        _ <- tell(appCfg.endTimeMillis)
      } yield e
    }
}
```

Note that in `main`, after we *run* or *remove a layer* from the pure program twice, we have one last layer in error handling. We have to handle any errors that might have occurred during the course of its execution.

Our hand-rolled `ReaderWriterEither[A]` stack now contains `Reader[AppConfig, ?]`, `Writer[Long, ?]` and `Either[Throwable, ?]`. Note how we've had to have *yet another level of indentation* just to support error handling. 

A test for this code looks like:

```scala
object MockUnreachableRover extends AppConfig {
  val exception = new Exception("Couldn't connect to Mars!")
  def println(s: String): Unit = throw exception // this matters
  def startTimeMillis: Long = 1 // these values
  def endTimeMillis:   Long = 2 // don't matter
}

"our sixth pass at printHelloWorld" should
    "be a flexible, timed, resilient hello world program" in {
  val program = Prob01Pass06.printHelloWorld

  val (timeElapsed, _) = program.run(MockAppConfig).run
  timeElapsed should be (2)

  val (_, result) = program.run(MockUnreachableRover).run
  result should be (Left(MockUnreachableRover.exception))
}
```

Our tests say it all. We now have a Hello World program that's flexible (allowing any `println`), timed (in ms), and resilient (by catching errors). By now we should be able to say it's all worth it. If we know how to imbue our humble Hello World program with these qualities, we now stand a chance at ensuring all our functional programs have the same qualities. But our `printHelloWorld` already looks rather *tedious*. Scale it to any bigger program and it becomes a maintenance and readability nightmare. There has to be a better way to write functional programs. This calls for a more suitable abstraction.

## The *Monad Transformer* Way

One way is to use monad transformers to flatten our for-comprehension:

```scala
import cats.data._
import cats.implicits._
import ReaderT._
import WriterT._

object Prob01Pass07 {

  def main(args: Array[String]): Unit = {
    val program = printHelloWorld

    // at the end of the world
    program.run(AppConfig.default).run match {
      case Left(e)                   => println(s"Hello world failed: $e")
      case Right((timeElapsed, _))   =>
        println(s"Ran hello world in $timeElapsed ms.")
    }
  }

  type EitherThrowable[A] = Either[Throwable, A]
  type WriterLongEither[A] = WriterT[EitherThrowable, Long, A]
  type ReaderWriterEither[A] = ReaderT[WriterLongEither, AppConfig, A]

  def printHelloWorld: ReaderWriterEither[Unit] =
    for {
      appConfig <- ask[WriterLongEither, AppConfig]
      _         <- logTime(-appConfig.startTimeMillis)
      either    <- ReaderT.lift[WriterLongEither, AppConfig, Unit](
                     WriterT.lift[EitherThrowable, Long, Unit](
                       try {
                         Right(appConfig.println("Hello World"))
                       } catch { case e: Throwable =>
                         Left(e)
                       }
                     )
                   )
      _         <- logTime(appConfig.endTimeMillis)
    } yield either

  def logTime(time: Long): ReaderWriterEither[Unit] = for {
    _ <- ReaderT.lift[WriterLongEither, AppConfig, Unit](tell(time))
  } yield ()
}
```

This was a **bitch** to write, and it's as clean as I could get it with my limited knowledge of `cats`. I'm pretty sure there's a way more compact way to write this that involves `cats.EitherT`. The `cats` library's implicits here mean that anytime we have to use `ReaderT`'s functions `ask` and `lift`, we've had to specify exactly what it is we're stacking a `Reader` on top of. Otherwise, we get all sorts of nasty compilation errors.

It is somewhat of an improvement though, in that we're able to specify our application logic in *only one layer* of a for-comprehension. It's the lifting part that stings. Stacking together three effects doesn't seem to get us the concise code that we want.

Our test code is fairly trivial and similar to the last one:

```scala
"our seventh pass monad transformers printHelloWorld" should
  "be a flexible, timed, resilient hello world program" in {
  val program = Prob01Pass07.printHelloWorld

  program.run(MockAppConfig).run should be (Right((2, ())))

  program.run(MockUnreachableRover).run should be (Left(MockUnreachableRover.exception))
}
```

## The _Eff Monad_ Way

Let's let the code do the talking this time:

```scala
import cats.data._
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._

object Prob01Pass08 {

  type Stack = Fx.fx3[
    Reader[AppConfig, ?],
    Writer[String, ?],
    Either[Throwable, ?]
  ]

  def main(args: Array[String]): Unit = {
    val program = printHelloWorld[Stack]

    // at the end of the world
    val result: Either[Throwable, Unit] =
      program
        .runReader(AppConfig.default)
        .runWriterUnsafe[String](println)
        .runEither
        .run

    result match { // Our program's result is a side effect that can fail.
      case Left(e) =>
        println(s"Hello world failed: $e")
      case _ => () 
    }
  }

  type _appCfg[R] = Reader[AppConfig, ?] |= R
  type _timeReport[R] = Writer[String, ?] |= R

  def printHelloWorld[R: _appCfg : _timeReport : _throwableEither]
      : Eff[R, Unit] = for {
    appConfig <- ask
    start = appConfig.startTimeMillis
    either    <- catchNonFatalThrowable(appConfig.println("Hello World"))
    durationInMs = appConfig.endTimeMillis - start
    _         <- tell(s"Ran hello world in $durationInMs ms.")
  } yield either
}
```

It's embarrassing how stupidly simple `printHelloWorld` looks compared to passes 6 and 7. Let's break each part down to see how it all works.

We define our program's effect stack using a type declaration as shown below.

```scala
type Stack = Fx.fx3[
  Reader[AppConfig, ?],
  Writer[String, ?],
  Either[Throwable, ?]
]
```

`Fx.fx3` means we have three effects as shown. Eff supports up to 12 effects stacked with `Fx.fx12`. Why the seemingly arbitrary selection of 12? I don't know. Probably something to do with the Scala compiler? At any rate, we'll need this later for `main` to know how to interpret our pure program. We also need `printHelloWorld` to declare what sort of effects it needs. Before we do that, we make a couple of type declarations:

```scala
type _appCfg[R] = Reader[AppConfig, ?] |= R
type _timeReport[R] = Writer[String, ?] |= R
```

We need these since `Reader` and `Writer` both accept two type parameters. Now we use them in our `printHelloWorld` signature:

```scala
def printHelloWorld[R: _appCfg : _timeReport : _throwableEither]
    : Eff[R, Unit] = for {
  appConfig <- ask
  start = appConfig.startTimeMillis
  either    <- catchNonFatalThrowable(appConfig.println("Hello World"))
  durationInMs = appConfig.endTimeMillis - start
  _         <- tell(s"Ran hello world in $durationInMs ms.")
} yield either
```

It's almost as if someone put together `Reader`, `Writer` and `Either` and combined them together into one monad. We simply `ask` the `Reader` for the config, catch `println` errors with `catchNonFatalThrowable`, and `tell` `Writer` to write our running time to some log.

Finally, let's go take a look at `main`.

```scala
def main(args: Array[String]): Unit = {
  val program = printHelloWorld[Stack]

  // at the end of the world
  val result: Either[Throwable, Unit] =
    program
      .runReader(AppConfig.default)
      .runWriterUnsafe[String](println)
      .runEither
      .run

  result match { // Our program's result is a side effect that can fail.
    case Left(e) =>
      println(s"Hello world failed: $e")
    case _ => () 
  }
}
```

We first let `printHelloWorld` know it runs under a stack of effects as specified earlier in our `Stack` type. At the end of the world, we run our effects exactly in the order shown above. Remember, we need to `runReader` before `runWriterUnsafe` because our usage of the `Writer` effect in `printHelloWorld` depends on there being an `AppConfig`, i.e. it needs to get the current time to write `println`'s running time.

Note that `runWriterUnsafe` means we can just write our logs in a function with side effects, e.g. `println` to console. You can choose to log this anywhere. Very handy when you have to log a lot of stuff and you don't want your logs to eat up all your RAM like Google Chrome does. When we `runEither`, we're left with only one effect, `Eff`. You can think of `Eff` as an effect that combines other effects. We finally get `result` with the last `run`.

Our final act in `main` is handling any errors that might have occurred while trying to print `"Hello World"`. `e` in `Left(e)` contains our possible error while `Right` only contains `Unit`, which we can just ignore.

Looking at `main`, we get the notion that the entire goal of our program is to produce a side effect, which can fail non-fatally.

How do we test our Eff program? We just interpret it a different way.

```scala
"our eighth pass printHelloWorld Eff monad program" should
  "be flexible, timed, and resilient" in {
  val program = Prob01Pass08.printHelloWorld[Prob01Pass08.Stack]

  val runningMock: AppConfig = MockAppConfig
  val failingMock: AppConfig = MockUnreachableRover

  program
    .runReader(runningMock)
    .runWriter
    .runEither
    .run should be (Right((), List("Ran hello world in 2 ms.")))

  program
    .runReader(failingMock)
    .runWriter
    .runEither
    .run should be (Left(MockUnreachableRover.exception))
}
```

# Conclusion

FP can be difficult without the proper abstractions. Even Hello World can get unwieldy. That isn't to say our FP versions are invariably harder than procedural code or "mainstream" OOP equivalents. Indeed these versions can yield even more difficult, opaque code.

Our pure programs, however, are highly composable, easier to reason about, and can very easily be made asynchronous, concurrent, and parallel just by adding some `Task` or `Future` effect on the stack and in our pure program's code. It's just not as easy to do the same in procedural/OOP code. To see this in action, if this article has helped you understand the whole point of the Eff monad, I strongly suggest you move on to working on the excellent exercises in Ben Hutchinson's [*"Getting Work Done With Extensible Effects"*](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/).

There are two other ways to express our stack that I didn't include here for lack of time. One way is with the free monad and the other is by rolling out your own `ReaderWriterEither[R, T, L, A]` type which effectively lets you have the same level of abstraction as with Eff. I leave these two as exercises for the reader.