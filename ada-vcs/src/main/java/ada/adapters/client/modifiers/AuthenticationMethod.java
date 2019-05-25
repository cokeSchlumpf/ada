package ada.adapters.client.modifiers;

import ada.commons.util.ResourceName;

import java.util.Optional;

public interface AuthenticationMethod extends RequestModifier {

    String info();

    Optional<ResourceName> getDefaultNamespace();

    AuthenticationMethodMemento memento();

}
