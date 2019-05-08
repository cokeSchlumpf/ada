package ada.vcs.server.domain.repository.valueobjects;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GrantedAuthorization {

    User by;

    Date at;

    Authorization authorization;

    public static GrantedAuthorization apply(User by, Date at, Authorization authorization) {
        return new GrantedAuthorization(by, at, authorization);
    }

}
