package ada.dvcs.impl

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.specs2.matcher.Matchers

import ada.dvcs.api._

class VersionControlSystemActorTest  extends TestKit(ActorSystem("spec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "An Echo actor" must {

    "send back messages unchanged" in {
      val echo = system.actorOf(VersionControlSystemActor.props())
      echo ! commands.CreateRepository(new model.Repository {
        override def name: String = "Hello"
      })
    }

  }

}
