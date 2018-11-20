package ada.datarepo.api

package object commands {

  final case class CreateRepository(name: String)

  final case class DeleteRepository(name: String)

}
