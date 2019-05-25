package ada.domain.dvc.values;

import ada.commons.databind.MessageSerializer;
import ada.commons.util.FQResourceName;
import ada.commons.util.ResourcePath;
import akka.actor.ExtendedActorSystem;
import com.google.common.collect.Maps;

import java.util.Map;

public final class ValuesSerializer extends MessageSerializer {

    public ValuesSerializer(ExtendedActorSystem actorSystem) {
        super(actorSystem, 2403 + 4);
    }

    @Override
    protected Map<String, Class<?>> getManifestToClass() {
        Map<String, Class<?>> m = Maps.newHashMap();

        m.put("values/user/anonymous/v1", AnonymousUser.class);
        m.put("values/user/authenticated/v1", AuthenticatedUser.class);
        m.put("values/user/id/v1", UserId.class);

        m.put("values/authorizations/granted/v1", GrantedAuthorization.class);
        m.put("values/authorizations/role/v1", RoleAuthorization.class);
        m.put("values/authorizations/user/v1", UserAuthorization.class);
        m.put("values/authorizations/wildcard/v1", WildcardAuthorization.class);

        m.put("values/repository/authorizations/v1", RepositoryAuthorizations.class);
        m.put("values/repository/summary/v1", RepositorySummary.class);
        m.put("values/repository/versions/state/v1", VersionState.class);
        m.put("values/repository/versions/status/v1", VersionStatus.class);

        m.put("values/resource/fq/v1", FQResourceName.class);
        m.put("values/resource/path/v1", ResourcePath.class);

        return m;
    }

}
