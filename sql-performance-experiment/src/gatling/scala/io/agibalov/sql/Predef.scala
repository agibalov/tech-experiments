package io.agibalov.sql

import io.agibalov.sql.action.ExecuteSqlActionBuilder
import io.agibalov.sql.protocol.{SqlProtocol, SqlProtocolBuilder}
import io.gatling.core.Predef.Simulation
import org.springframework.jdbc.core.namedparam.SqlParameterSource

object Predef {
  def sql(simulation: Simulation): SqlProtocolBuilder = SqlProtocolBuilder.simulation(simulation)
  def sql(queryName: String) = new Sql(queryName)
}

class Sql(queryName: String) {
  def executeSql(sql: String, sqlParameterSource: SqlParameterSource) =
    new ExecuteSqlActionBuilder(queryName, sql, sqlParameterSource)
}
