package io.agibalov.sql.protocol

import io.gatling.core.Predef.Simulation

case class SqlProtocolBuilder(simulation: Simulation) {
  def build() = SqlProtocol(simulation)
}

object SqlProtocolBuilder {
  def simulation(simulation: Simulation) = SqlProtocolBuilder(simulation)
  implicit def toSqlProtocol(builder: SqlProtocolBuilder): SqlProtocol = builder.build()
}
