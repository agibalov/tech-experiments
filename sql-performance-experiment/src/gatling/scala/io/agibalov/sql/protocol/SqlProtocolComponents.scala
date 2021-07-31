package io.agibalov.sql.protocol

import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session

case class SqlProtocolComponents(sqlProtocol: SqlProtocol) extends ProtocolComponents {
  override def onStart: Session => Session = session => session
  override def onExit: Session => Unit = session => {}
}
