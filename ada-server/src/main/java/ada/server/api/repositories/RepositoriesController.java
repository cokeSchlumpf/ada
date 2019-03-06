package ada.server.api.repositories;

import ada.server.api.AuthenticatedApiResource;
import com.ibm.ada.api.repository.Repositories;
import com.ibm.ada.api.repository.Repository;
import com.ibm.ada.api.exceptions.RepositoryNotFoundException;
import com.ibm.ada.model.*;
import com.ibm.ada.model.auth.AuthorizationRequest;
import com.ibm.ada.model.auth.User;
import com.ibm.ada.model.versions.PatchVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Api("Repositories")
@RestController
@RequestMapping("api/v1/repositories")
public class RepositoriesController implements AuthenticatedApiResource {

    private final Repositories repositories;

    public RepositoriesController(Repositories repositories) {
        this.repositories = repositories;
    }

    @RequestMapping(
        path = "{name}/data",
        method = RequestMethod.POST,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Appends data to the working directory of the repository")
    public CompletableFuture<TransferResult> append(
        @PathVariable("name") String name,
        @RequestBody Flux<Record> data,
        ServerWebExchange exchange) throws RepositoryNotFoundException {

        User user = getUser(exchange);
        Repository repository = repositories.getRepository(user, RepositoryName.apply(name));
        // TODO

        return null;
    }

    @RequestMapping(
        path = "{name}/owner",
        method = RequestMethod.POST,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Changes the owner of a repository")
    public CompletableFuture<RepositoryDetails> changeRepositoryOwner(
        @PathVariable("name") String name,
        @RequestBody AuthorizationRequest newOwner,
        ServerWebExchange exchange) {

        // TODO
        return null;
    }

    @RequestMapping(
        path = "{name}/data",
        method = RequestMethod.PATCH,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Commits the current working directory and creates a new data version")
    public CompletableFuture<PatchVersion> commit(@PathVariable("name") String name) {
        // TODO
        return null;
    }

    @RequestMapping(
        path = "{name}/access",
        method = RequestMethod.PUT,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Changes the owner of a repository")
    public CompletableFuture<RepositoryDetails> grantRepositoryAccess(
        @PathVariable("name") String name,
        @RequestBody AuthorizationRequest newAccess) {

        // TODO
        return null;
    }

    @RequestMapping(
        path = "{name}",
        method = RequestMethod.PUT,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Creates a new repository")
    public CompletableFuture<RepositoryDetails> createRepository(@PathVariable("name") String name) {
        // TODO
        return null;
    }

    @RequestMapping(
        path = "{name}",
        method = RequestMethod.DELETE,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Deletes a repository")
    public CompletableFuture<Void> deleteRepository(@PathVariable("name") String name) {
        // TODO
        return null;
    }

    @RequestMapping(
        method = RequestMethod.GET,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Returns a list of available repositories")
    public Stream<RepositoryDetails> getRepositories() {
        // TODO
        return Stream.empty();
    }

    @RequestMapping(
        path = "{name}",
        method = RequestMethod.GET,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Returns a list of available repositories")
    public RepositoryDetails getRepository(@PathVariable("name") String name) {
        // TODO
        return null;
    }

    @RequestMapping(
        path = "{name}/data",
        method = RequestMethod.GET,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Returns the data set for the latest version (might be an uncommitted version)")
    public Flux<Record> read(@PathVariable("name") String name) {
        // TODO
        return Flux.empty();
    }

    @RequestMapping(
        path = "{name}/data/{version}",
        method = RequestMethod.GET,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Returns the data set for the provided version")
    public Flux<Record> read(@PathVariable("name") String name, @PathVariable("version") String version) {
        // TODO
        return Flux.empty();
    }

    @RequestMapping(
        path = "{name}/data",
        method = RequestMethod.PUT,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Appends data to the working directory of the repository")
    public CompletableFuture<TransferResult> replace(
        @PathVariable("name") String name,
        @RequestBody Flux<Record> data) {

        // TODO
        return null;
    }

    @RequestMapping(
        path = "{name}/data",
        method = RequestMethod.DELETE,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Reverts the changes in the current working directory")
    public CompletableFuture<PatchVersion> revert(@PathVariable("name") String name) {
        // TODO
        return null;
    }

    @RequestMapping(
        path = "{name}/access",
        method = RequestMethod.DELETE,
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Returns a list of available repositories")
    public CompletableFuture<RepositoryDetails> revokeRepositoryAccess(
        @PathVariable("name") String name,
        @RequestBody AuthorizationRequest revokeAccess) {
        // TODO
        return null;
    }

}
