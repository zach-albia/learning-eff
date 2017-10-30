package learningeff.elementary

import org.scalatest._

class Prob01Spec extends FlatSpec with Matchers {

  val mockPrintln: String => Unit = _ should be ("Hello World")

  "our second pass at printHelloWorld" should
      "print hello world given a println function" in {
    Prob01Pass02.printHelloWorld(mockPrintln)
  }

  "our third pass at printHelloWorld" should
      "print hello world when run with a println function" in {
    val program = Prob01Pass03.printHelloWorld

    program.run(mockPrintln)
  }
}
