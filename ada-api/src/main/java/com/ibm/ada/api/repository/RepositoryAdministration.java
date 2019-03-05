package com.ibm.ada.api.repository;

import com.ibm.ada.api.exceptions.NotAuthorizedException;
import com.ibm.ada.api.model.auth.AuthorizationRequest;
import com.ibm.ada.api.model.RepositoryDetails;
import com.ibm.ada.api.model.auth.User;

import java.util.concurrent.CompletionStage;

/**
 * This interface bundles all operations which manage a repository.
 */
public interface RepositoryAdministration {

    /**
     * Changes the owner of the repository.
     *
     * @param executor The user which executes the operation.
     * @param auth     The authorization which specifies the new owner.
     * @return Updated details of the repository.
     * @throws NotAuthorizedException If the executor is not authorized to change the owner.
     */
    CompletionStage<RepositoryDetails> changeOwner(User executor, AuthorizationRequest auth) throws
        NotAuthorizedException;

    /**
     * Deletes the repository.
     *
     * @param executor The user which executes the operation.
     * @return Completable future which completes when successfully deleted.
     * @throws NotAuthorizedException If the executor is not authorized to change the owner.
     */
    CompletionStage<Void> delete(User executor) throws
        NotAuthorizedException;

    /**
     * Adds an authorization to the repository readers and writers.
     *
     * @param executor The user which executes the operation.
     * @param auth     The authorization which should be allowed to get access to the repository.
     * @return Updated details of the repository.
     * @throws NotAuthorizedException If the executor is not authorized to grant access to the repository.
     */
    CompletionStage<RepositoryDetails> grant(User executor, AuthorizationRequest auth) throws
        NotAuthorizedException;

    /**
     * Removes an authorization from the repository readers and writers.
     *
     * @param executor The user which executes the operation.
     * @param auth     The authorization which should be removed from allowed authorizations to access to the repository.
     * @return Updated details of the repository.
     * @throws NotAuthorizedException If the executor is not authorized to grant access to the repository.
     */
    CompletionStage<RepositoryDetails> revoke(User executor, AuthorizationRequest auth) throws
        NotAuthorizedException;

}
