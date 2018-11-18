package ada.client.commands.about

import ada.client.CommandContext
import ada.web.controllers.AboutResource
import akka.stream.Materializer
import akka.stream.scaladsl.Source

import scala.concurrent.ExecutionContext

case class AboutCommandImpl(
                             resource: AboutResource,
                             context: CommandContext)(
                             implicit materializer: Materializer,
                             ec: ExecutionContext) extends AboutCommand {

  override def run(): Unit = {
    val done = Source
      .fromPublisher(resource.getAboutStream)
      .runForeach(context.getOutput.println)

    done.onComplete({
      case _ => context.terminate()
    })
  }

}

object AboutCommandFactory {

  def create(resource: AboutResource,
             out: CommandContext)(
              implicit materializer: Materializer,
              ec: ExecutionContext): AboutCommand = {

    val cmd = AboutCommandImpl(resource, out)
    AboutCommandPicoDecorator.apply(cmd)
  }

}
