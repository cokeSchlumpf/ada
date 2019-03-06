package com.ibm.ada.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor(staticName = "apply")
public class About {

    private final String name;

    private final String environment;

    private final String build;

}
