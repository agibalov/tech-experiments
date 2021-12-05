package io.agibalov

import org.springframework.jdbc.core.namedparam.{MapSqlParameterSource, NamedParameterJdbcTemplate}
import org.springframework.jdbc.datasource.DriverManagerDataSource

import scala.jdk.CollectionConverters._

object PostgresDbFacade {
  private val jdbcTemplate = {
    val environment = System.getenv().asScala
    val dataSource = new DriverManagerDataSource(
      environment("SPRING_DATASOURCE_URL"),
      environment("SPRING_DATASOURCE_USERNAME"),
      environment("SPRING_DATASOURCE_PASSWORD"))
    new NamedParameterJdbcTemplate(dataSource)
  }

  def getAnyAccountId(): String = {
    val row = jdbcTemplate.queryForMap(
      """select id
        |from Accounts
        |limit 1""".stripMargin,
      new MapSqlParameterSource)

    row.get("id").asInstanceOf[String]
  }
}
