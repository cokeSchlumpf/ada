package ada.client

import ada.client.commands.about.{AboutCommand, AboutCommandFactory}
import picocli.CommandLine.IFactory

case class ClientCommandLineFactory(clientContext: ClientContext) extends IFactory {

  override def create[K](cls: Class[K]): K = {
    if (classOf[AboutCommand].isAssignableFrom(cls)) {
      AboutCommandFactory
        .create(
          clientContext.getAboutResource,
          clientContext)(
          clientContext.getMaterializer,
          clientContext.getMaterializer.executionContext)
        .asInstanceOf[K]
    } else {
      cls.newInstance()
    }
  }

}
