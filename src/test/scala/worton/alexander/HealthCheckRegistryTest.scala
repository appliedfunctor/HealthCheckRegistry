package worton.alexander

import cats.effect.IO
import cats.implicits._
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.{Matchers, WordSpec}
import worton.alexander.HealthCheckResult.Healthy

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._

class HealthCheckRegistryTest extends WordSpec with Matchers with ScalaFutures with PatienceConfiguration {


  override implicit def patienceConfig: PatienceConfig = PatienceConfig(5.seconds, 500.milliseconds)

  trait SyncSetup {
    val healthCheck0 = HealthCheck( "", () => {
      Thread.sleep(500)
      HealthCheckResponse("death star", Healthy("Fire when ready!"))
    })
    val healthCheck1 = HealthCheck( "", () => {
      Thread.sleep(500)
      HealthCheckResponse("luke's x-wing", Healthy("Use the force"))
    })

    val registry: HealthCheckRegistry[IO] = new HealthCheckRegistry[IO]()
      .add(healthCheck0)
      .add(healthCheck1)
      .async(false)
  }

  trait ASyncSetup {
    val healthCheck0 = HealthCheck( "", () => {
      Thread.sleep(800)
      HealthCheckResponse("death star", Healthy("Fire when ready!"))
    })

    val healthCheck1 = HealthCheck( "", () => {
      Thread.sleep(800)
      HealthCheckResponse("luke's x-wing", Healthy("Use the force"))
    })

    val healthCheck2 = HealthCheck( "", () => {
      Thread.sleep(800)
      HealthCheckResponse("vader's tie fighter", Healthy("I've got him in my sights"))
    })

    val registry: HealthCheckRegistry[IO] = new HealthCheckRegistry[IO]()
      .add(healthCheck0)
      .add(healthCheck1)
      .add(healthCheck2)
      .async(true)
  }

  "Execution of synchronous IOs" should{
    "take their total sequential duration to run" in new SyncSetup {
      val start: Long = System.currentTimeMillis()
      registry.runHealthChecks().unsafeToFuture().futureValue
      val end: Long = System.currentTimeMillis()
      val durationMillis: Long = end - start

      println(s"sync runtime: $durationMillis")

      durationMillis should be < 2000L
      durationMillis should be >= 1000L
    }
  }

  "Execution of asynchronous IOs" should{
    "take their longest separate duration to run" in new ASyncSetup {
      val start: Long = System.currentTimeMillis()
      registry.runHealthChecks().unsafeToFuture().futureValue
      val end: Long = System.currentTimeMillis()
      val durationMillis: Long = end - start

      println(s"async runtime: $durationMillis")

      durationMillis should be < 1000L
      durationMillis should be >= 500L
    }
  }

}
