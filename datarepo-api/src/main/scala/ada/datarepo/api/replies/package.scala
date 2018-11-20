package ada.datarepo.api

import ada.datarepo.api.model.Repository

package object replies {

  final case class Repositories(repositories: List[Repository])

  final case class Status(count: Int)

}
