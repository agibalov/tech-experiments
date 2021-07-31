package io.agibalov.sql.action

import com.typesafe.scalalogging.StrictLogging
import io.agibalov.sql.protocol.SqlProtocol
import io.gatling.commons.stats.OK
import io.gatling.commons.util.Clock
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import org.springframework.jdbc.core.JdbcTemplate

class ExecuteSqlAction(protocol: SqlProtocol,
                       val name: String,
                       val sql: String,
                       val statsEngine: StatsEngine,
                       coreComponents: CoreComponents,
                       val next: Action) extends ExitableAction with StrictLogging {

  override def clock: Clock = coreComponents.clock

  override def execute(session: Session): Unit = {
    val jdbcTemplate = protocol.springContext.getBean(classOf[JdbcTemplate])

    val start = System.currentTimeMillis
    val rows = jdbcTemplate.queryForMap(sql)
    val end = System.currentTimeMillis

    logger.info(s"${rows.size()} rows: ${rows}")

    statsEngine.logResponse(session.scenario, session.groups, name, start, end, OK, None, None)

    next ! session
  }
}
