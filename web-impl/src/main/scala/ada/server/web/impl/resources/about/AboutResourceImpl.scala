package ada.server.web.impl.resources.about

import ada.web.controllers.AboutResource
import ada.web.controllers.model.{AboutApplication, AboutUser}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import org.reactivestreams.Publisher

case class AboutResourceImpl(
  config: AboutControllerConfiguration,
  user: AboutUser)(implicit materializer: Materializer) extends AboutResource {

  override def getAbout: AboutApplication = AboutApplication.apply(config.getName, config.getBuild)

  override def getAboutStream: Publisher[String] = {
    val output =
      s"""|
          |              _
          |     /\\      | |
          |    /  \\   __| | __ _
          |   / /\\ \\ / _` |/ _` |
          |  / ____ \\ (_| | (_| |
          | /_/    \\_\\__,_|\\__,_| v${config.getBuild}
          |
          |
          | instance name: ${config.getName}
          |
          |""".stripMargin

    Source(output.split("\n").toList)
      .map(s => s"$s\n")
      .runWith(Sink.asPublisher(true))
  }

  override def getUser: AboutUser = user

}
