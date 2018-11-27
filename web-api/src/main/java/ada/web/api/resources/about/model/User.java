package ada.web.api.resources.about.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Set;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    property = "class")
public interface User {

    Set<String> getRoles();

}
