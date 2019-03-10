package com.ibm.ada.model.repository;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;
import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Commit {

    private final String id;

    private final String message;

    private final Date created;

    private final String createdBy;

    private final Commit previous;

    @JsonCreator
    public static Commit apply(
        @JsonProperty("id") String id, @JsonProperty("message") String message, @JsonProperty("date") Date created,
        @JsonProperty("createdBy") String createdBy, @JsonProperty("previous") Commit previous) {

        return new Commit(id, message, created, createdBy, previous);
    }

    public static Commit apply(String id, String message, Date created, String createdBy) {
        return apply(id, message, created, createdBy, null);
    }

    @JsonIgnore
    public Optional<Commit> getPrevious() {
        return Optional.ofNullable(previous);
    }

    @JsonIgnore
    public Optional<Integer> getDistance(Commit other) {
        Optional<Integer> result = getAhead(other);

        if (result.isPresent()) {
            return result;
        } else {
            return other.getAhead(this);
        }
    }

    private Optional<Integer> getAhead(Commit other) {
        if (this.id.equals(other.id)) {
            return Optional.of(0);
        } else if (this.previous != null) {
            return this.previous.getAhead(other).map(i -> i + 1);
        } else {
            return Optional.empty();
        }
    }

}
