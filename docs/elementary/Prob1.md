# Introduction

Learning how to use [Eff in Scala](https://github.com/atnos-org/eff), let alone learning how it works on the inside, is quite the challenge for me and I believe for many people too. I feel that there's quite the chasm from running a few easy examples to putting together a full app using Eff.

However, Eff has a great amount documentation for the initiated FP user in Scala. In fact, there's even a set of exercises in [*"Getting Work Done With Extensible Effects"*](https://github.com/benhutchison/GettingWorkDoneWithExtensibleEffects/) that guide you in a very methodical way towards understanding how you might use Eff in a real, non-trivial app. There's even [a great presentation](https://vimeo.com/channels/flatmap2016/165927840) about a practical way to use Eff in bigger projects, though I've yet to see the usage of the overall framework described in it in a sample application. I hope there's one out there and I hope I just missed it.

That said, I think there's still a part of that chasm that needs to be covered. It's somewhere in between someone starting out with FP in Scala and the aforementioned exercises by Ben Hutchinson. I think there's some value in going back to the simplest problems and seeing what Eff looks like there.

I'm planning to have this series span the first set of adriannn's [Simple Programming Problems](https://adriann.github.io/programming_problems.html), namely the *Elementary Problems*. We kick things off with our first problem:

> Write a program that prints ‘Hello World’ to the screen.

# Many ways to say Hello

## The simplest way

Our first jab at the problem begins with the simplest possible example that we're taught when we first learn to program in any language. Our first problem happens to be the one for Scala:

```scala
object Prob01Pass01 {
  def main(args: Array[String]): Unit = {
    println("Hello World")
  }
}
```

How do we know it works? We run it, and it prints `Hello World` to the console. But we did have to run it, look at the characters it prints out, and make sure *every single character* lines up to make the string `Hello World`. We aren't exactly very good at reading either, and our eyes can trip and think we're reading the right string when we're not!

If it can happen to us in a simple "Hello World", imagine what would happen if we had to do it for any bigger program. We say we **cannot test** `main`. It's opaque to us unless we go to great lengths just to read the output on the console itself.

## The testable way

At the heart of developing software is getting things done *automatically*. That includes testing. We have to restructure our tiny Hello World app so we can **run a test**:

1. to see if our program compiles,
2. to see if it works as expected without having to look at our program's console output, and
3. to be able to run the same test again and again to do 1 & 2.

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

Now we can write code to test `printHelloWorld`.

```scala
import Prob01Pass01.printHelloWorld
import org.scalatest._

class Prob01Spec extends FlatSpec with Matchers {

  val mockPrintln: String => Unit = _ should be ("Hello World")

  "our second pass at printHelloWorld" should
      "print hello world given a println function" in {
    Prob01Pass02.printHelloWorld(mockPrintln)
  }
}
```

## The *Reader Monad* way

`printHelloWorld` is a function that takes a `println` function and runs it on `"Hello World"`, giving back `Unit`. In reader monad terms:

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

Our code is very similar to our first pass. Note that we only had to change a little bit of code. When we use the reader monad here, we mean to say it *delays reading or asking for*  a `println`  function that helps *return* `Unit`, i.e. to make some side effect(s). I think "reader monad" is a weird name for it. It'd probably make more sense as the "context monad" or "dependency monad". But what do I know? I don't know how it got the name.

We also have a corresponding test case:

```scala
"our third pass at printHelloWorld" should
    "print hello world when run with a println function" in {
  val program = Prob01Pass03.printHelloWorld

  // at the end of the world, again
  program.run(mockPrintln)
}
```

By using the reader monad, in `main` we end up with a pure *description* of our "Hello World" program in `val program`. A program that we can *run*.

It's pure in the sense that we can call `printHelloWorld` any number of times and it won't have any side effects, and will *always* return the same program. I've read a lot of material which states *making programs pure is the essence of functional programming*.

We can interpret our program in another way with the use of a *"fake"* or a _"mock"_ `println`. One that just checks if it got passed `"Hello World"`.

What does this buy us? There is some extra cognitive overload here for sure. We've had to learn Reader when we could have just stuck to our first pass by just passing functions and calling it a day. Our second test case shows that we've gained *the ability and the concept* of interpreting a program in many ways, one of which is useful for testing. We could even make another version of "Hello World" that interprets our program description to print the line to a file, given the right `println` function.

## The Reader-Writer Monad way

