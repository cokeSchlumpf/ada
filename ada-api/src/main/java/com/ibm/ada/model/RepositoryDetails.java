package com.ibm.ada.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RepositoryDetails {

    private final RepositoryName name;

}
