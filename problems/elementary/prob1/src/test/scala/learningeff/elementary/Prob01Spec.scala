package learningeff.elementary

import org.scalatest._

import org.atnos.eff.syntax.all._

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
    def endTimeMillis: Long = 3
  }

  "our fifth pass at printHelloWorld" should
      "be a flexible, timed hello world program" in {
    val program = Prob01Pass05.printHelloWorld

    val (timeElapsed, _) = program.run(MockAppConfig).run
    timeElapsed should be (2)
  }

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

  "our seventh pass monad transformers printHelloWorld" should
    "be a flexible, timed, resilient hello world program" in {
    val program = Prob01Pass07.printHelloWorld

    program.run(MockAppConfig).run should be (Right((2, ())))

    program.run(MockUnreachableRover).run should be (Left(MockUnreachableRover.exception))
  }

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
}
