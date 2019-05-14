package ada.vcs.domain.dvc.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WildcardAuthorization implements Authorization {

    @JsonCreator
    public static WildcardAuthorization apply() {
        return new WildcardAuthorization();
    }

    @Override
    @JsonIgnore
    public boolean hasAuthorization(User user) {
        return true;
    }

}
