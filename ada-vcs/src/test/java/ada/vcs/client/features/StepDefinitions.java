package ada.vcs.client.features;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StepDefinitions {

    private final ApplicationContext context;

    @Given("today is Sunday")
    public void today_is_Sunday() {
        context.run("init");
    }

    @When("I ask whether it's Friday yet")
    public void i_ask_whether_it_s_Friday_yet() {
        System.out.println(context.getOutput().toString());
    }

    @Then("^I should be told \"([^\"]*)\"$")
    public void i_should_be_told(String string) {

    }


}
