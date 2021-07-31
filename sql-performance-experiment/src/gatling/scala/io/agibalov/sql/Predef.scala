package io.agibalov.sql

import io.agibalov.sql.action.ExecuteSqlActionBuilder
import io.agibalov.sql.protocol.{SqlProtocol, SqlProtocolBuilder}
import io.gatling.core.Predef.Simulation

object Predef {
  def sql(simulation: Simulation): SqlProtocolBuilder = SqlProtocolBuilder.simulation(simulation)
  def sql(queryName: String) = new Sql(queryName)
}

class Sql(queryName: String) {
  def executeSql(sql: String) = new ExecuteSqlActionBuilder(queryName, sql)
}
