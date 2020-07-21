package de.testbirds.tech.recipe.base;

import de.testbirds.tech.recipe.entity.RecipeMethod;
import de.testbirds.tech.recipe.handler.*;
import de.testbirds.tech.recipe.report.SoftwareInstallException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * collects all the recipe method handlers.
 */
public class RecipeMethodDispatcher {

    private static RecipeMethodDispatcher INSTANCE;

    /**
     * The map of event types to their handlers.
     */
    private final ConcurrentMap<RecipeMethod, AbstractRecipeMethodHandler> handlers;

    /**
     * takes all the recipemethodhandlers.
     */
    private RecipeMethodDispatcher() {
        handlers = new ConcurrentHashMap<>();
    }

    public static RecipeMethodDispatcher getInst() {
        if (INSTANCE == null) {
            INSTANCE = new RecipeMethodDispatcher();
            addHandlers(INSTANCE);
        }
        return INSTANCE;
    }

    /**
     * add all the RecipeMethod handlers here.
     */
    private static void addHandlers(RecipeMethodDispatcher recipeMethodDispatcher) {
        // add all the recipeMethodHandlers
        recipeMethodDispatcher.setHandler(RecipeMethod.ASYNC, new AsyncHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.COPY, new CopyHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.DOWNLOAD, new DownloadHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.DMG_EULA, new DmgEulaHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.DMG, new DmgHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.MOVE, new MoveHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.PKG, new PkgHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.FROM_FILE, new ReadFileHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.COMMAND, new RunHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.SET, new SetHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.UNZIP, new UnzipHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.TO_FILE, new WriteFileHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.REBOOT_NOW, new AbstractRecipeMethodHandler() {
            @Override
            public StackElement handle(final String parameter, final Installer callback)
                    throws SoftwareInstallException {
                callback.getStartup().reboot();
                // we can return null because the machine is already rebooting
                // at the moment
                return null;
            }
        });
        recipeMethodDispatcher.setHandler(RecipeMethod.INSTALL_CERT, new CertificateInstallHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.MOVE_FF, new FirefoxProfileHandler());
        recipeMethodDispatcher.setHandler(RecipeMethod.UPLOAD, new UploadHandler());
    }

    /**
     * set the handler for some specific recipeMethod.
     *
     * @param recipeMethod recipeMethod
     * @param handler      new handler.
     */
    private void setHandler(final RecipeMethod recipeMethod, final AbstractRecipeMethodHandler handler) {
        handlers.put(recipeMethod, handler);
    }

    /**
     * get the handler for some recipeMethod.
     *
     * @param recipeMethod searching for a handler for this recipeMethod
     * @return the handler
     * @throws SoftwareInstallException if there is no handler for this recipeMethod
     */
    public final AbstractRecipeMethodHandler getHandler(final RecipeMethod recipeMethod)
            throws SoftwareInstallException {
        final AbstractRecipeMethodHandler rmh = handlers.get(recipeMethod);
        if (rmh == null) {
            throw new SoftwareInstallException("can't handle " + recipeMethod + " because there is no handler for it");
        }
        return rmh;
    }

}
