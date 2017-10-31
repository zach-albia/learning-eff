# Introduction

Learning how to use [Eff in Scala](https://github.com/atnos-org/eff), let alone learning how it works on the inside, is quite the challenge for me and I believe for many people too. I feel that there's quite the chasm from running a few easy examples to putting together a full app using Eff.

However, Eff has a great amount documentation for the initiated FP user in Scala. In fact, there's even a set of exercises in [*"Getting Work Done With Extensible Effects"*](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/) that guide you in a very methodical way towards understanding how you might use Eff in a real, non-trivial app. There's even [a great presentation](https://vimeo.com/channels/flatmap2016/165927840) about a practical way to use Eff in bigger projects, though I've yet to see the usage of the overall framework described in it in a sample application. I hope there's one out there and I hope I just missed it. Or maybe it's not needed at all.

That said, I think there's still a part of that chasm that needs to be covered. It's somewhere in between someone starting out with FP in Scala and the aforementioned exercises by Ben Hutchinson. I think there's some value in going back to the simplest problems and seeing what Eff looks like there.

I'm planning to have this series span the first set of adriannn's [Simple Programming Problems](https://adriann.github.io/programming_problems.html), namely the *Elementary Problems*. We kick things off with our first problem:

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

What does Reader buy us? There is some extra cognitive overload here for sure. We've had to learn Reader when we could have just stuck to our second pass by just passing functions. We've gained *the flexibility and the concept* of a pure program, one that could be interpreted in many ways, one of which is useful for testing. What else can we gain?

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

Let's make an `AppConfig` trait and let's give it the default console and system time version that we can use in `main`:

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

Note that we've declared that our `println` can fail. This is due to our program being flexible enough for this to happen. Now that we have `AppConfig` nailed down, let's see we what we can do with `Reader` and `Writer`:

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
        _ <- tell(-appCfg.startTime)
        _ <- value[Long, Unit](appCfg.println("Hello World"))
        _ <- tell(appCfg.endTime)
      } yield ()
    }
}
```

Looking at `main`, `printHelloWorld` is run twice. That's because our program now has *two* effects to apply, `Reader` and `Writer`. Our third pass only had `Reader` so we've only had to call `run` once. In FP, pure programs like `printHelloWorld` will be run or *interpreted* as many times as it has effects. Once we *interpret* all those effects, we end up with the output of our pure program, in this case, a `(Long, Unit)` tuple, the first part of which contains our `timeElapsed` in milliseconds.

With `AppConfig`, `Reader`, and `Writer`, we've decoupled our code from the console and the system clock. Note that we've had to manually `println` our `timeElapsed` report in `main`, since the second `run` produces a pure log of `timeElapsed`.

With this, we can write a test for our fifth pass that checks both printing Hello World and logging the time.

```scala
object MockAppConfig extends AppConfig {
  def println(s: String): Unit = s should be ("Hello World")
  def startTime: Long = 1
  def endTime: Long = 2
}

"our fifth pass at printHelloWorld" should
    "be a flexible, timed hello world program" in {
  val program = Prob01Pass05.printHelloWorld

  val (timeElapsed, _) = program.run(MockAppConfig).run
  timeElapsed should be (1)
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

Our `println` can throw an exception, as declared in `AppConfig`. After all, we could have our pure program tell a rover on Mars to print "Hello World" there, again given the right `println`function. We could very easily lose our connection to the rover! As programmers, we have to be prepared for this scenario. Let's add error-handling to our stack.

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
object ThrowingAppConfig extends AppConfig {
  val exception = new Exception("Couldn't connect to Mars!")
  def println(s: String): Unit = throw exception // this matters
  def startTimeMillis: Long = 1 // these values
  def endTimeMillis: Long = 2   // don't matter
}

"our sixth pass at printHelloWorld" should
    "be a flexible, timed, resilient hello world program" in {
  val program = Prob01Pass06.printHelloWorld

  val (timeElapsed, _) = program.run(MockAppConfig).run
  timeElapsed should be (1)

  val (_, result) = program.run(ThrowingAppConfig).run
  result should be (Left(ThrowingAppConfig.exception))
}
```

Our tests say it all. We now have a Hello World program that's flexible (allowing any `println`), timed (in ms), and resilient (by catching errors). By now we should be able to say it's all worth it. If we know how to imbue our humble Hello World program with these qualities, we now stand a chance of ensuring all our functional programs have the same qualities. But our `printHelloWorld` already looks rather *tedious*. Scale it to any bigger program and it becomes a maintenance and readability nightmare. There has to be a better way to write functional programs. This calls for a more suitable abstraction.

## The *Monad Transformer* Way

One way is to use monad transformers to flatten our for comprehension:

```scala
import cats.data._
import cats.implicits._

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
      appConfig <- ReaderT.ask[WriterLongEither, AppConfig]
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
    _ <- ReaderT.lift[WriterLongEither, AppConfig, Unit](WriterT.tell(time))
  } yield ()
}
```

This was a **bitch** to get running. The `cats` library's implicits here mean that anytime we have to use `ReaderT`'s functions `ask` and `lift`, we've had to specify exactly what it is we're stacking a `Reader` on top of. Otherwise, we get nasty implicit resolution compilation errors.

It is somewhat of an improvement though, in that we're able to specify our application logic in *only one layer* of a for-comprehension. It's the lifting part that stings.