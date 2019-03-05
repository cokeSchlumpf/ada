package com.ibm.ada.api.repository;

import com.ibm.ada.api.model.RepositoryDetails;

public interface Repository {

    /**
     * Retrieves the admin functions for the repository.
     *
     * @return The repository admin functions.
     */
    RepositoryAdministration admin();

    /**
     * Retrieves functions to work with repository data.
     *
     * @return The repository data functions.
     */
    RepositoryData data();

    /**
     * Retrieves the details of the repository.
     *
     * @return The repository details.
     */
    RepositoryDetails details();

}
