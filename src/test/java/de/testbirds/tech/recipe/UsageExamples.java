package de.testbirds.tech.recipe;

import de.testbirds.tech.recipe.entity.Arch;
import de.testbirds.tech.recipe.entity.RecipeMethod;
import de.testbirds.tech.recipe.entity.RecipeStep;
import de.testbirds.tech.recipe.entity.SoftwareVersion;
import de.testbirds.tech.recipe.report.SoftwareInstallException;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.mock;

public class UsageExamples {

    private final Startup startup = mock(Startup.class);

    @Test
    public void simple() throws SoftwareInstallException {
        SoftwareVersion software = new SoftwareVersion("1.0", Arch.X86, new RecipeStep.Builder().add(RecipeMethod.SET, "123").build(), "param");
        RecipeInstaller installer = new RecipeInstaller(startup, software);
        installer.execute();
    }

    @Test
    public void downloadMoveExecute() {
        List<RecipeStep> steps = new RecipeStep.Builder().add(RecipeMethod.DOWNLOAD, "{{0}}")
                .add(RecipeMethod.MOVE, "/tmp/downloadedFile")
                .add(RecipeMethod.COMMAND, "/tmp/downloadedFile").build();
    }
}
