package ada.server.api.about;

import ada.server.configuration.ApplicationConfiguration;
import com.ibm.ada.api.model.About;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api("Repositories")
@RestController
@RequestMapping("api/v1/about")
public class AboutApiResource {

    @Autowired
    public ApplicationConfiguration config;

    @RequestMapping(
        method = RequestMethod.GET,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Returns a list of available repositories")
    public About about() {
        // TODO get information from build/ environment
        return About.apply(config.getName(), config.getEnvironment(), config.getBuild());
    }

}
