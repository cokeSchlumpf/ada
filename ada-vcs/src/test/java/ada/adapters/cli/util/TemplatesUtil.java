package ada.adapters.cli.util;

import com.google.common.collect.Maps;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.resource.exceptions.ResourceNotFoundException;

import java.util.Map;

public final class TemplatesUtil {

    private TemplatesUtil() {

    }

    public static String render(JtwigTemplate template, Map<String, Object> model) {
        return template.render(createModel(model));
    }

    public static String renderTemplateFromResources(String resourcePath) {
        return renderTemplateFromResources(resourcePath, createModel(Maps.newHashMap()));
    }

    public static String renderTemplateFromResources(String resourcePath, Map<String, Object> values) {
        return renderTemplateFromResources(resourcePath, createModel(values));
    }

    public static String renderTemplateFromResources(String resourcePath, JtwigModel model) {
        try {
            final JtwigTemplate template = JtwigTemplate.classpathTemplate(resourcePath);
            return template.render(model);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            // Fallback which might be used when running from an IDE.
            try {
                final JtwigTemplate template = JtwigTemplate.fileTemplate(resourcePath);
                return template.render(model);
            } catch (Exception ex) {
                throw resourceNotFoundException;
            }
        }
    }

    public static JtwigModel createModel(Map<String, Object> values) {
        JtwigModel model = JtwigModel.newModel();

        for (String key : values.keySet()) {
            model = model.with(key, values.get(key));
        }

        return model;
    }

}
