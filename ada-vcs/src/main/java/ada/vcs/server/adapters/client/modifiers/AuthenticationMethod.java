package ada.vcs.server.adapters.client.modifiers;

public interface AuthenticationMethod extends RequestModifier {

    String info();

    AuthenticationMethodMemento memento();

}
