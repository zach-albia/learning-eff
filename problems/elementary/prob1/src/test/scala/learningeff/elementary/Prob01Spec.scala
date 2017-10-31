package learningeff.elementary

import org.scalatest._

class Prob01Spec extends FlatSpec with Matchers {

  "our second pass at printHelloWorld" should
      "print hello world given a println function" in {
    Prob01Pass02.printHelloWorld(_ should be ("Hello World"))
  }

  "our third pass at printHelloWorld" should
      "be a flexible hello world program" in {
    val program = Prob01Pass03.printHelloWorld

    program.run(_ should be ("Hello World"))
  }

  object MockAppConfig extends AppConfig {
    def println(s: String): Unit = s should be ("Hello World")
    def startTimeMillis: Long = 1
    def endTimeMillis: Long = 2
  }

  "our fifth pass at printHelloWorld" should
      "be a flexible, timed hello world program" in {
    val program = Prob01Pass05.printHelloWorld

    val (timeElapsed, _) = program.run(MockAppConfig).run
    timeElapsed should be (1)
  }

  object ThrowingAppConfig extends AppConfig {
    val exception = new Exception("Couldn't connect to Mars!")
    def println(s: String): Unit = throw exception
    def startTimeMillis: Long = 1
    def endTimeMillis: Long = 2
  }

  "our sixth pass at printHelloWorld" should
      "be a flexible, timed, resilient hello world program" in {
    val program = Prob01Pass06.printHelloWorld

    val (timeElapsed, _) = program.run(MockAppConfig).run
    timeElapsed should be (1)

    val (_, result) = program.run(ThrowingAppConfig).run
    result should be (Left(ThrowingAppConfig.exception))
  }
}
