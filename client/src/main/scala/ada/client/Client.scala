package ada.client

import ada.client.commands.AdaCommand
import ada.client.output.ClientOutput
import picocli.CommandLine

case class Client private(out: ClientOutput, cli: CommandLine) {

  def run(arguments: Array[String]): Unit = run(arguments: _*)

  def run(arguments: String*): Unit = {
    try {
      cli.parseWithHandler(new CommandLine.RunLast, arguments.toArray)
    } catch {
      case ex: CommandLine.ExecutionException =>
        CommandLine.usage(ex.getCommandLine, this.out.getPrintStream)
    }
  }

}

object Client {

  def apply(context: ClientContext): Client = {
    val factory = ClientCommandLineFactory(context)
    val command = factory.create(classOf[AdaCommand])
    val cli = new CommandLine(command, factory)

    Client(context.getOutput, cli)
  }

}
