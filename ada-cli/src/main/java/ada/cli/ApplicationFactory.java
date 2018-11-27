package ada.cli;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class ApplicationFactory implements CommandLine.IFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public <K> K create(Class<K> cls) {
        return applicationContext.getBean(cls);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
