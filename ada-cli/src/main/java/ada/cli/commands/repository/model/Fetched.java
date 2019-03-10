package ada.cli.commands.repository.model;

import com.ibm.ada.model.repository.Commit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Date;

@Value
@AllArgsConstructor(staticName = "apply")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Fetched {

    private final Date date;

    private final Commit commit;

}
