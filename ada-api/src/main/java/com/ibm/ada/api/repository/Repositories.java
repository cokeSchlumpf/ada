package com.ibm.ada.api.repository;

import com.ibm.ada.exceptions.RepositoryNotFoundException;
import com.ibm.ada.model.RepositoryName;
import com.ibm.ada.model.auth.User;

import java.util.stream.Stream;

public interface Repositories {

    Repository createRepository(User executor, RepositoryName name);

    Repository getRepository(User executor, RepositoryName name) throws RepositoryNotFoundException;

    Stream<Repository> getRepositories(User executor);

}
