package com.ibm.ada.api.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = User.Anonymous.class, name = "anonymous"),
    @JsonSubTypes.Type(value = User.Authenticated.class, name = "authenticated")
})
public abstract class User {

    private User() {

    }

    public abstract String getDisplayName();

    public abstract Stream<Role> getRoles();

    @Value
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor(staticName = "apply")
    public static class Anonymous extends User {

        private final ImmutableSet<Role> roles;

        public static Anonymous apply(Stream<Role> roles) {
            return apply(ImmutableSet.copyOf(roles.collect(Collectors.toList())));
        }

        @SuppressWarnings("unused")
        private Anonymous() {
            this(ImmutableSet.of());
        }

        @Override
        @JsonIgnore
        public String getDisplayName() {
            return "anonymous";
        }

        @Override
        public Stream<Role> getRoles() {
            return roles.stream();
        }

    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor(staticName = "apply")
    public static class Authenticated extends User {

        private final String name;

        private final ImmutableSet<Role> roles;

        @SuppressWarnings("unused")
        private Authenticated() {
            this("n/a", ImmutableSet.of());
        }

        public static Authenticated apply(String name, Stream<Role> roles) {
            return apply(name, ImmutableSet.copyOf(roles.collect(Collectors.toList())));
        }

        @Override
        @JsonIgnore
        public String getDisplayName() {
            return name;
        }

        @Override
        public Stream<Role> getRoles() {
            return roles.stream();
        }

    }

}
