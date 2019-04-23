package ada.vcs.client.core.repository.api;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class UserUTest {

    @Test
    public void test() {
        User u1 = User.apply("Michael Wellner", "info@michaelwellner.de");
        assertThat(u1.toString()).isEqualTo("Michael Wellner <info@michaelwellner.de>");
        assertThat(User.fromString(u1.toString())).isEqualTo(u1);
    }

}
