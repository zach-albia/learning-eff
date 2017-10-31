package learningeff.elementary

trait AppConfig {
  @throws(classOf[Exception])
  def println(s: String): Unit
  def startTimeMillis: Long
  def endTimeMillis: Long
}

object AppConfig {
  val default: AppConfig = new AppConfig {
    @throws(classOf[Exception])
    def println(s: String): Unit = Predef.println(s)
    def startTimeMillis: Long = System.currentTimeMillis()
    def endTimeMillis: Long = System.currentTimeMillis()
  }
}
