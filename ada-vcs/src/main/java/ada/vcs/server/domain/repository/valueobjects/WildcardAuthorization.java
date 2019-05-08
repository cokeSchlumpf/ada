package ada.vcs.server.domain.repository.valueobjects;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class WildcardAuthorization implements Authorization {

    @Override
    public boolean hasAuthorization(User user) {
        return true;
    }

}
