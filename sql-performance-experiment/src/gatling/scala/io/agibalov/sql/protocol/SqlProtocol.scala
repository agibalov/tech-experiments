package io.agibalov.sql.protocol

import io.gatling.core.CoreComponents
import io.gatling.core.Predef.Simulation
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}
import org.springframework.boot.Banner.Mode
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext

class SqlProtocol(val springContext: ConfigurableApplicationContext) extends Protocol {
  type Components = SqlProtocolComponents
}

object SqlProtocol {
  def apply(simulation: Simulation): SqlProtocol = {
    val context = new SpringApplicationBuilder(classOf[App])
      .bannerMode(Mode.OFF)
      .run()
    simulation.after {
      context.close()
    }
    new SqlProtocol(context)
  }

  val SqlProtocolKey = new ProtocolKey[SqlProtocol, SqlProtocolComponents] {
    override def protocolClass: Class[io.gatling.core.protocol.Protocol] =
      classOf[SqlProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): SqlProtocol =
      throw new IllegalStateException("Can't provide a default value for SqlProtocol")

    override def newComponents(coreComponents: CoreComponents): SqlProtocol => SqlProtocolComponents = {
      dummyProtocol => SqlProtocolComponents(dummyProtocol)
    }
  }
}

@SpringBootApplication
class App {
}
