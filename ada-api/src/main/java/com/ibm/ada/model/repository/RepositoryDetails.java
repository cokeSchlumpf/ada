package com.ibm.ada.model.repository;

import com.ibm.ada.model.ResourceName;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public final class RepositoryDetails {

    private final ResourceName name;

}
