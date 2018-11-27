package ada.cli.users;

import ada.web.api.resources.about.model.User;
import com.google.common.collect.Sets;

import java.util.Set;

public class MutualTLSAuthenticatedUser implements User {

    private static final MutualTLSAuthenticatedUser INSTANCE = new MutualTLSAuthenticatedUser();

    @Override
    public Set<String> getRoles() {
        return Sets.newHashSet();
    }

    private MutualTLSAuthenticatedUser() {

    }

    public static MutualTLSAuthenticatedUser apply() {
        return INSTANCE;
    }

}
