package io.datacater.core.project;

import com.fasterxml.jackson.databind.JsonNode;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.deployment.DeploymentSpec;
import io.datacater.core.exceptions.CreateDeploymentException;
import io.datacater.core.kubernetes.PythonRunnerPool;
import io.datacater.core.stream.StreamMessage;
import io.datacater.core.stream.StreamUtilities;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ProjectUtilities {

}
