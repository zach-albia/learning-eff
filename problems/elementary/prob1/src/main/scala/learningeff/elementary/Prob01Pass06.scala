package learningeff.elementary

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