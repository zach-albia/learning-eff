package learningeff.elementary

object Prob01Pass02 {
  val defaultPrintln: String => Unit = Predef.println

  def main(args: Array[String]): Unit =
    printHelloWorld(defaultPrintln)

  def printHelloWorld(println: String => Unit): Unit =
    println("Hello World")
}
