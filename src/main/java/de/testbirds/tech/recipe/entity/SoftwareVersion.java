package de.testbirds.tech.recipe.entity;

import java.util.List;

public class SoftwareVersion {
    private final List<RecipeStep> steps;

    private final String parameter;

    private final String version;

    private final Arch arch;

    public SoftwareVersion(String version, Arch arch, List<RecipeStep> steps, String parameter) {
        this.version = version;
        this.arch = arch;
        this.steps = steps;
        this.parameter = parameter;
    }

    public List<RecipeStep> getSteps() {
        return steps;
    }

    public String getParameter() {
        return parameter;
    }

    public String getVersion() {
        return version;
    }

    public Arch getArch() {
        return arch;
    }
}
