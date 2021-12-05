package io.agibalov

import io.agibalov.sql.Predef._
import io.gatling.core.Predef._
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource

class PostgresCountUsersInOneAccountSimulation extends Simulation {
  val scn = {
    val accountId = PostgresDbFacade.getAnyAccountId()

    scenario("Count users in one account")
      .repeat(10) {
        exec(sql("Count users in one account").executeSql(
          """
            |select count(*) from Users where accountId = :accountId
            |""".stripMargin,
          new MapSqlParameterSource()
            .addValue("accountId", accountId)))
      }
  }

  setUp(scn.inject(atOnceUsers(1)))
    .protocols(sql(this))
    .assertions(
      forAll.successfulRequests.percent.is(100)
    )
}
