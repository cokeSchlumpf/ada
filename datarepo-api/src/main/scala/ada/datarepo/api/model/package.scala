package ada.datarepo.api

package object model {

  trait Repository {

    def name: String

    def versions: List[MajorVersion]

  }

  trait Schema {

  }

  trait MajorVersion {

    def minorVersions: List[MinorVersion]

    def schema: Schema

    def version: Int

  }

  trait MinorVersion {

    def version: Int

    def patch: Int

  }

}
