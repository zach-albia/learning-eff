package learningeff.elementary

import learningeff.elementary.Prob01Pass05.AppConfig
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

  object MockAppConfig extends AppConfig {
    def println(s: String): Unit = s should be ("Hello World")
    def startTime: Long = 1
    def endTime: Long = 2
  }

  "our fifth pass at printHelloWorld" should
      "print hello world and log the elapsed time" in {
    val program = Prob01Pass05.printHelloWorld

    val (elapsed, _) = program.run(MockAppConfig).run
    elapsed should be (1L)
  }
}
