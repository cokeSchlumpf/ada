package com.ibm.ada.api.repository;

import com.ibm.ada.api.exceptions.RepositoryNotFoundException;
import com.ibm.ada.model.ResourceName;
import com.ibm.ada.model.auth.User;

import java.util.stream.Stream;

public interface Repositories {

    Repository createRepository(User executor, ResourceName name);

    Repository getRepository(User executor, ResourceName name) throws RepositoryNotFoundException;

    Stream<Repository> getRepositories(User executor);

}
