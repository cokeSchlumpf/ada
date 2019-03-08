package com.ibm.ada.api.repository;

import com.ibm.ada.api.exceptions.LockedException;
import com.ibm.ada.api.exceptions.NotAuthorizedException;
import com.ibm.ada.api.exceptions.UncommittedChangesException;
import com.ibm.ada.model.Record;
import com.ibm.ada.model.Schema;
import com.ibm.ada.model.TransferResult;
import com.ibm.ada.model.auth.User;
import com.ibm.ada.model.versions.PatchVersion;
import com.ibm.ada.model.versions.Version;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * This interface bundles operations which change the data content of a {@link Repository}.
 */
public interface RepositoryData {

    /**
     * Appends a new set of data to the current repository version.
     *
     * @param executor The user which executes the operation.
     * @param schema   The schema of the data which should be appended.
     * @param data     The stream of data records to be appended to the data set.
     * @return A summary of the data transfer.
     * @throws LockedException             If another user has locked the repository.
     * @throws NotAuthorizedException      If the executor is not authorized to execute the operation.
     * @throws UncommittedChangesException If the repository contains uncommitted changes with another schema.
     */
    CompletionStage<TransferResult> append(User executor, Schema schema, Publisher<Record> data)
        throws LockedException, NotAuthorizedException, UncommittedChangesException;

    /**
     * Commits the current content of the repository. This will generate a new committed version.
     *
     * @param executor The user which executes the operation.
     * @param message  The message of the commit.
     * @return The version which has been committed.
     */
    CompletionStage<PatchVersion> commit(User executor, String message);

    /**
     * Reads the data of a provided version of the repository.
     *
     * @param executor The user which executes the operation.
     * @param version  The version of the repository to read from.
     * @return A reactive stream of records which are contained in the repository.
     */
    Publisher<Record> read(User executor, Version version);

    /**
     * Replaces the existing data set with a new data set.
     *
     * @param executor The user which executes the operation.
     * @param schema   The schema of the data which is inserted.
     * @param data     The stream of data records to be inserted.
     * @return A summary of the data transfer.
     * @throws LockedException             If another user has locked the repository.
     * @throws NotAuthorizedException      If the executor is not authorized to execute the operation.
     * @throws UncommittedChangesException If the repository contains uncommitted changes with another schema.
     */
    CompletionStage<TransferResult> replace(User executor, Schema schema, Publisher<Record> data)
        throws LockedException, NotAuthorizedException, UncommittedChangesException;

    /**
     * Reverts all changes in the working directory to the latest version or to no version if it
     * doesn't contain a committed version yet.
     *
     * @param executor The user which executes the operation.
     */
    CompletionStage<Optional<PatchVersion>> revert(User executor) throws LockedException, NotAuthorizedException;

}
