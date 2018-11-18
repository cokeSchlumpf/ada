package ada.client.output

import java.io.PrintStream

final case class DefaultClientOutput(ps: PrintStream) extends ClientOutput {

  override def getPrintStream: PrintStream = ps

  override def println(message: String, args: Object*): Unit = {
    val s = String.format(message, args)
    println(s)
  }

  override def println(message: String): Unit = System.out.println(message)

}

object DefaultClientOutput {

  def apply(): DefaultClientOutput = DefaultClientOutput(System.out)

}
