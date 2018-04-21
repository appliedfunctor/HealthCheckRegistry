package worton.alexander

case class HealthCheck(name: String, run: () => HealthCheckResponse)
