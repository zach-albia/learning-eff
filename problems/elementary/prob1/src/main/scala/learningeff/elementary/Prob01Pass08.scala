package learningeff.elementary

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
