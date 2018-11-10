package ada.web.resources.about
import ada.web.resources.about.model.{AboutApplication, AboutUser}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import org.reactivestreams.Publisher

/**
  * @author Michael Wellner (michael.wellner@de.ibm.com)
  */
case class AboutControllerImpl(
  config: AboutControllerConfiguration,
  user: AboutUser)(implicit materializer: Materializer) extends AboutController {

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