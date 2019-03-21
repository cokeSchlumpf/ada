package com.ibm.ada.model.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.ada.model.sources.DataSourceDefinition;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class Schema {

    /**
     * A speaking title for the data-set.
     */
    private final String title;

    /**
     * Some schema description.
     */
    private final String description;

    /**
     * Classification of the data contained in the repository (version).
     */
    private final DataCategory category;

    /**
     * Schema description in Avro schema format.
     */
    private final JsonNode schema;

}
