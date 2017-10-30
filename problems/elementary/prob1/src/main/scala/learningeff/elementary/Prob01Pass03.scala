package learningeff.elementary

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
