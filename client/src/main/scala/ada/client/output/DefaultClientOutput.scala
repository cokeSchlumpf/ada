package ada.client.output

import java.io.PrintStream

final case class DefaultClientOutput(ps: PrintStream) extends ClientOutput {

  override def getPrintStream: PrintStream = ps

  override def println(message: String, args: Object*): Unit = {
    val s = String.format(message, args)
    println(s)
  }

  override def println(message: String): Unit = System.out.println(message)

  override def print(message: String, args: Object*): Unit = print(String.format(message, args))

  override def print(message: String): Unit = System.out.print(message)

}

object DefaultClientOutput {

  def apply(): DefaultClientOutput = DefaultClientOutput(System.out)

}
