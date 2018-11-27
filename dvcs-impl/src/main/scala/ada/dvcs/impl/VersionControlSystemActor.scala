package ada.dvcs.impl

import ada.dvcs.api._
import ada.dvcs.api.model.Repository
import akka.actor.{Actor, ActorLogging, Props}

class VersionControlSystemActor extends Actor with ActorLogging{

  override def receive: Receive = active(List())

  private def active(repositories: List[Repository]): Receive = {
    case commands.CreateRepository(repo) =>
      context.child(repo.name) match {
        case Some(_) =>
          // The repository already exists. No need to create it.
          sender ! events.RepositoryCreated(repo)

        case None =>
          context.actorOf(RepositoryActor.props(repo.name))
      }

    case requests.GetRepositories =>
      sender ! replies.Repositories(repositories)

    case requests.GetStatus =>
      sender ! replies.Status(repositories.size)

    case any =>
      log.warning(s"Received unexpected message ${any}")
  }

}

object VersionControlSystemActor {

  def props(): Props = Props[VersionControlSystemActor]

}
