package worton.alexander

import cats._
import cats.effect.Effect
import cats.implicits._
import worton.alexander.HealthCheckResult.Unhealthy

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class HealthCheckRegistry[F[_]: Monad] {

  val healthChecks: List[HealthCheck] = List.empty
  val isAsync: Boolean = true

  def add(healthCheck: HealthCheck): HealthCheckRegistry[F] = {
    val allHealthChecks = healthCheck :: healthChecks
    val sync = isAsync
    new HealthCheckRegistry[F] {
      override val healthChecks: List[HealthCheck] = allHealthChecks
      override val isAsync: Boolean = sync
    }
  }

  def async(enable: Boolean): HealthCheckRegistry[F] = {
    val allHealthChecks = healthChecks
    new HealthCheckRegistry[F] {
      override val healthChecks: List[HealthCheck] = allHealthChecks
      override val isAsync: Boolean = enable
    }
  }


  private def runAsync(hc: HealthCheck)(implicit ec: ExecutionContext, ef: Effect[F]): F[HealthCheckResponse] = {
    val f = Future(hc.run())
    Effect[F].async[HealthCheckResponse] { cb =>
      f.onComplete {
        case Success(a) => cb(Right(a))
        case Failure(e) => cb(Right(HealthCheckResponse(hc.name, Unhealthy("No response received"))))
      }
    }
  }

  private def runSync(hc: HealthCheck)(implicit ef: Effect[F]): F[HealthCheckResponse] = {
    Effect[F].pure(hc.run())
  }

  //TODO these should be fired off asynchronously
  def runHealthChecks()(implicit ef: Effect[F], ec: ExecutionContext): F[List[HealthCheckResponse]] =
    if(!isAsync) healthChecks.map(runSync).sequence[F, HealthCheckResponse]
    else healthChecks.map(runAsync).sequence[F, HealthCheckResponse]

}