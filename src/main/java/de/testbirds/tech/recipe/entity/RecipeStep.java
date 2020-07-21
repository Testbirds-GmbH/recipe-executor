package de.testbirds.tech.recipe.entity;

import java.util.ArrayList;
import java.util.List;

public class RecipeStep {
    private final RecipeMethod method;
    private final String parameter;

    public RecipeStep(RecipeMethod method, String parameter) {
        this.method = method;
        this.parameter = parameter;
    }

    public RecipeMethod getMethod() {
        return method;
    }

    public String getParameter() {
        return parameter;
    }

    public static class Builder {
        private final List<RecipeStep> steps = new ArrayList<>();

        public Builder add(RecipeMethod method, String parameter) {
            steps.add(new RecipeStep(method, parameter));
            return this;
        }

        public Builder pop() {
            return add(RecipeMethod.POP, null);
        }

        public List<RecipeStep> build() {
            int open = steps.stream().reduce(0, (sum, step) -> sum + (step.method == RecipeMethod.POP ? -1 : 1), Integer::sum);

            for (int i = 0; i < open; i++) {
                steps.add(new RecipeStep(RecipeMethod.POP, null));
            }
            return steps;
        }

        public List<RecipeStep> getSteps() {
            return steps;
        }

        public Builder cmd(String s) {
            return add(RecipeMethod.COMMAND, s);
        }

        public Builder move(String target) {
            return add(RecipeMethod.MOVE, target);
        }
    }
}
