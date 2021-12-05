package io.agibalov

import io.agibalov.sql.Predef._
import io.gatling.core.Predef._
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource

class MysqlCountAllUsersSimulation extends Simulation {
  val scn = scenario("Count all users")
    .repeat(5) {
      exec(sql("Count all users").executeSql(
        """
          |select count(*) from Users
          |""".stripMargin,
        new MapSqlParameterSource))
    }

  setUp(scn.inject(atOnceUsers(1)))
    .protocols(sql(this))
    .assertions(
      forAll.successfulRequests.percent.is(100)
    )
}
