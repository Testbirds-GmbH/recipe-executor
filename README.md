# Recipe Executor

An important part of the device cloud was the installation of a wide variety of software. We were in need of a format to describe the installation steps of different software artifacts.

The result are so called recipes, but instead of describing steps to cook food our recipes describe the steps to install a software (version).
All steps are stored in the database and there was a UI to create or edit recipes. This repo gives an overview how the recipes work and contains the Java code to execute them.

## Concept

The main concept behind the recipes is a stack machine. There is always a stack of parameters on which the recipe steps operate.
In the beginning there is one element on the stack, which comes from the `SoftwareVersion`. Most of the time it contains the url path to that specific version.

Every step then adds another element to the stack, except the POP operation, which removes the top element.

We implemented many different steps, such as a download step, one moving files, an upload step or more fine-grained things like a dmg installer.
The full list with explanations can be found [in RecipeMethod.java](src/main/java/de/testbirds/tech/recipe/entity/RecipeMethod.java).

Steps can reference elements on the stack using the syntax `{{N}}`, where N is the position on the stack, with 0 being the element at the top. We introduced further special variables which are referenced similar, such as `{{SW_MIRROR}}`, `{{DESKTOP}}`, ...

## How to use

Simple example:
```java
SoftwareVersion software = new SoftwareVersion("1.0", Arch.X86, new RecipeStep.Builder().add(RecipeMethod.SET, "123").build(), "param");
RecipeInstaller installer = new RecipeInstaller(startup, software);
installer.execute();
```

A more complex recipe:
```java
List<RecipeStep> steps = new RecipeStep.Builder()
    .add(RecipeMethod.DOWNLOAD, "{{0}}")
    .add(RecipeMethod.MOVE, "/tmp/downloadedFile")
    .add(RecipeMethod.COMMAND, "/tmp/downloadedFile").build();
```