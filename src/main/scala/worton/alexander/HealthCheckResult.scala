package worton.alexander

sealed trait HealthCheckResult {
  val response: String
  val healthy: Boolean
}

object HealthCheckResult {

  final case class Healthy(response: String) extends HealthCheckResult {
    override val healthy = true
  }

  final case class Unhealthy(response: String) extends HealthCheckResult {
    override val healthy = false
  }

}