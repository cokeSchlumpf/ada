package ada.client.commands.about

import ada.client.output.ClientOutput
import ada.web.controllers.AboutResource
import akka.stream.Materializer
import akka.stream.scaladsl.Source

case class AboutCommandImpl (
                             resource: AboutResource,
                             out: ClientOutput,
                             materializer: Materializer) extends AboutCommand {

  override def run(): Unit = {
    Source
      .fromPublisher(resource.getAboutStream)
      .runForeach(System.out.println)(materializer)
  }

}

object AboutCommandFactory {

  def create(resource: AboutResource,
            out: ClientOutput,
            materializer: Materializer): AboutCommand = {

    val cmd = AboutCommandImpl(resource, out, materializer)
    AboutCommandPicoDecorator.apply(cmd)
  }

}
