package ada.client

import ada.client.commands.about.{AboutCommand, AboutCommandFactory}
import picocli.CommandLine.IFactory

case class ClientCommandLineFactory(context: ClientContext) extends IFactory {

  override def create[K](cls: Class[K]): K = {
    if (classOf[AboutCommand].isAssignableFrom(cls)) {
      AboutCommandFactory
        .create(context.getAboutResource, context.getOutput, context.getMaterializer)
        .asInstanceOf[K]
    } else {
      cls.newInstance()
    }
  }

}
