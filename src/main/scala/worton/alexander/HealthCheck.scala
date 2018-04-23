package worton.alexander

final case class HealthCheck(name: String, run: () => HealthCheckResponse)
