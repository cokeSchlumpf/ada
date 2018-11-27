package ada.dvcs.impl

import ada.dvcs.api.model
import akka.actor.{Actor, Props}

class RepositoryActor extends Actor {

  override def receive: Receive = {
    case _ @ msg =>
      println(s"Received ${msg} ...")
  }

  override def idle(repo: model.Repository): Receive = {

  }

}

object RepositoryActor {

  def props(repo: model.Repository): Props = {
    Props[RepositoryActor]
  }

}
