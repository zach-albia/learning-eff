package learningeff.elementary

import cats.data._
import cats.implicits._
import ReaderT._
import WriterT._

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
      appConfig <- ask[WriterLongEither, AppConfig]
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
    _ <- ReaderT.lift[WriterLongEither, AppConfig, Unit](tell(time))
  } yield ()
}