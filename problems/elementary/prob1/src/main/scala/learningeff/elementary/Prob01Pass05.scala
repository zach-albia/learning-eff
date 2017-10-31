package learningeff.elementary

import cats._, cats.data._, cats.implicits._
import Writer._

object Prob01Pass05 {

  def main(args: Array[String]): Unit = {
    val program = printHelloWorld

    // at the end of the world
    val (timeElapsed, _) = program.run(AppConfig.default).run
    println(s"Ran hello world in $timeElapsed ms.")
  }

  val printHelloWorld: Reader[AppConfig, Writer[Long, Unit]] =
    Reader { appCfg =>
      for {
        _ <- tell(-appCfg.startTimeMillis)
        _ <- value[Long, Unit](appCfg.println("Hello World"))
        _ <- tell(appCfg.endTimeMillis)
      } yield ()
    }
}