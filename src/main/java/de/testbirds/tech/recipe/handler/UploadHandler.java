package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import de.testbirds.tech.recipe.util.FileUtils;
import de.testbirds.tech.recipe.util.URLBuilder;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.PathContentProvider;
import org.eclipse.jetty.http.HttpMethod;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * The upload handler.
 *
 * @author testbirds
 */
public final class UploadHandler extends AbstractRecipeMethodHandler {

    @Override
    public StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        try {
            MultiPartContentProvider multiPart = new MultiPartContentProvider();
            multiPart.addFilePart("icon", "img.png", new PathContentProvider(Paths.get(parameter)), null);
            multiPart.close();

            final Request request = FileUtils.getJettyClient().newRequest(
                    URLBuilder.getInst().buildLocalVMHost("/file/upload/" + exe.getStartup().getUUID()).toString())
                    .method(HttpMethod.POST).content(multiPart);
            final ContentResponse response = request.send();

            return new StackElement(response.getContentAsString());
        } catch (final IOException | InterruptedException | TimeoutException | ExecutionException e) {
            throw new SoftwareInstallException("Cannot upload file", e);
        }
    }
}
