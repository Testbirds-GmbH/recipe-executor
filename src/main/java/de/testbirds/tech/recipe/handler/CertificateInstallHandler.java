package de.testbirds.tech.recipe.handler;

import de.testbirds.tech.recipe.base.AbstractRecipeMethodHandler;
import de.testbirds.tech.recipe.base.Installer;
import de.testbirds.tech.recipe.base.StackElement;
import de.testbirds.tech.recipe.entity.OSFamily;
import de.testbirds.tech.recipe.entity.RecipeStep;
import de.testbirds.tech.recipe.report.SoftwareInstallException;

/**
 * install certificate.
 */
public class CertificateInstallHandler extends AbstractRecipeMethodHandler {

    @Override
    public final StackElement handle(final String parameter, final Installer exe) throws SoftwareInstallException {
        final OSFamily fam = exe.getStartup().determineOSFamily();
        switch (fam) {
            case WIN:
                exe.insert(new RecipeStep.Builder().cmd("certutil -addstore -f \"ROOT\" " + parameter).build());
                break;
            case MAC:
                exe.insert(new RecipeStep.Builder().cmd(
                        "sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain " + parameter)
                        .build());
                break;
            default:
                throw new SoftwareInstallException("installing certificates not yet supported on " + fam);
        }

        return new StackElement("N/A");
    }

}
