package ada.dvcs

package object api {

  object commands {

    import model._

    trait RepositoryCommand { def repo: Repository }

    final case class CreateRepository(repo: Repository) extends RepositoryCommand

    final case class DeleteRepository(repo: Repository) extends RepositoryCommand

  }

  object events {

    import model._

    trait RepositoryEvent { def repo: Repository }

    case class RepositoryCreated(repo: Repository)

    case class RepositoryDeleted(repo: Repository)

  }

  object replies {

    import model._

    final case class Repositories(repositories: List[Repository])

    final case class Status(count: Int)

  }

  object requests {

    case object GetRepositories

    case object GetStatus

  }

  object model {

    trait Repository {

      def name: String

    }

    trait RepositoryFactory {

      def apply(name: String): Repository

    }


    trait Schema {

    }

    trait MajorVersion {

      def version: Int

    }

    trait MajorVersionFactory {

      def apply(version: Int): MajorVersion

    }

    trait MinorVersion {

      def version: Int

      def patch: Int

    }

    trait MinorVersionFactory {

      def apply(version: Int, patch: Int): MinorVersion

    }

    case class RepositoryFakeImpl(name: String) extends Repository

    case object SchemaFakeImpl extends Schema

    case class MajorVersionFakeImpl(version: Int) extends MajorVersion

    case class MinorVersionFakeImpl(version: Int, patch: Int) extends MinorVersion

  }

}
