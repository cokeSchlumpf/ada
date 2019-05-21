package ada.experiment;

import ada.commons.util.ResourcePath;
import akka.actor.ActorSystem;
import akka.actor.typed.javadsl.Adapter;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.ORSet;
import akka.cluster.ddata.ORSetKey;
import akka.cluster.typed.Cluster;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;

public class ClusterApplication {

    public static void run() {
        ActorSystem system = ActorSystem.create();

        SimpleServer server = SimpleServer.apply(system);

        Cluster node = Cluster.get(Adapter.toTyped(system));



        try {
            server.startServer("0.0.0.0", nextFreePort(8080), system);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int nextFreePort(int from) {
        int port = from;
        while (true) {
            if (isLocalPortFree(port)) {
                return port;
            } else {
                port = nextFreePort(from + 1);
            }
        }
    }

    private static boolean isLocalPortFree(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
