package ada.vcs.server.domain.dvc.values;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class WildcardAuthorization implements Authorization {

    @Override
    public boolean hasAuthorization(User user) {
        return true;
    }

}
