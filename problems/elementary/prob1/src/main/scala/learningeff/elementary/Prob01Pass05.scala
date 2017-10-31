package learningeff.elementary

import cats._, cats.data._, cats.implicits._
import Writer._

object Prob01Pass05 {

  trait AppConfig {
    def println(s: String): Unit
    def startTime: Long
    def endTime: Long
  }

  object DefaultAppConfig extends AppConfig {
    def println(s: String): Unit = Predef.println(s)
    def startTime: Long = System.currentTimeMillis()
    def endTime: Long = System.currentTimeMillis()
  }

  def main(args: Array[String]): Unit = {
    val program = printHelloWorld

    // at the end of the world
    val (timeElapsed, _) = program.run(DefaultAppConfig).run
    println(s"Ran hello world in $timeElapsed ms.")
  }

  val printHelloWorld: Reader[AppConfig, Writer[Long, Unit]] =
    Reader { appCfg =>
      for {
        _ <- tell(-appCfg.startTime)
        _ <- value[Long, Unit](appCfg.println("Hello World"))
        _ <- tell(appCfg.endTime)
      } yield ()
    }
}