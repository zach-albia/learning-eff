package learningeff.elementary

trait AppConfig {
  def println(s: String): Unit
  def startTimeMillis: Long
  def endTimeMillis: Long
}

object AppConfig {
  val default: AppConfig = new AppConfig {
    def println(s: String): Unit = Predef.println(s)
    def startTimeMillis: Long = System.currentTimeMillis()
    def endTimeMillis: Long = System.currentTimeMillis()
  }
}
