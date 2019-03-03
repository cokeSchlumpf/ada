package ada.server.api.health;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/health")
@Api("Health information of Ada node")
public class HealthController {


    @GetMapping(path = "live")
    public String isAlive() {
        return "alive";
    }

    @GetMapping(path = "ready")
    public String isReady() {
        return "ok";
    }

}
