package io.agibalov

import io.agibalov.sql.Predef._
import io.gatling.core.Predef._

class DummyPostgresSimulation extends Simulation {
  val scn = scenario("10 users one plus one scenario")
    .repeat(100) {
      exec(sql("One plus one").executeSql(
        """
          |select 1+1
          |""".stripMargin))
    }

  setUp(scn.inject(atOnceUsers(2)))
    .protocols(sql(this))
    .assertions(
      forAll.successfulRequests.percent.is(100),
      forAll.responseTime.percentile4.lt(100)
    )
}
