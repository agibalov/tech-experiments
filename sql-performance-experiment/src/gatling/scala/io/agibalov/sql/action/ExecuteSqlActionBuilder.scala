package io.agibalov.sql.action

import io.agibalov.sql.protocol.SqlProtocol
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.protocol.ProtocolComponentsRegistry
import io.gatling.core.structure.ScenarioContext
import org.springframework.jdbc.core.namedparam.SqlParameterSource

class ExecuteSqlActionBuilder(queryName: String, sql: String,
                              sqlParameterSource: SqlParameterSource) extends ActionBuilder {

  private def components(protocolComponentsRegistry: ProtocolComponentsRegistry) =
    protocolComponentsRegistry.components(SqlProtocol.SqlProtocolKey)

  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._
    val statsEngine = coreComponents.statsEngine
    val sqlProtocolComponents = components(protocolComponentsRegistry)
    new ExecuteSqlAction(sqlProtocolComponents.sqlProtocol, queryName, sql,
      sqlParameterSource, statsEngine, coreComponents, next)
  }
}
