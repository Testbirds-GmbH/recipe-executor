/*
 * Copyright 2019 Testbirds GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.testbirds.tech.recipe.entity;

/**
 * All possible recipe methods.
 */
public enum RecipeMethod {

    /**
     * Go one level up/left in the hierarchy/tree. You will lose the current context, all changes done in this level are
     * lost, and the state is reseted to the one it was before the last RIGHT
     * <p>
     * input: N/A
     * </p>
     * <p>
     * output: N/A
     * </p>
     * <p>
     * limitations: there have to be exactly as much POP as PUSH operations
     * </p>
     */
    POP,

    /**
     * Download a file to your virtual machine. This method takes the value on top of your stack, interprets it as an
     * URL and starts downloading to a temporary location. Afterwards the temporary location of the download file is
     * pushed on top of the stack.
     * <p>
     * input: url
     * </p>
     * <p>
     * output: local path to download file in tmp folder
     * </p>
     */
    DOWNLOAD,

    /**
     * Upload a file from the VM to the VM host, where it is publicly downloadable (but only once!).
     * <p>
     * input: local file path
     * </p>
     * <p>
     * output: FILE-UUID of file. You can then download the file from
     * vmhost-XX.testchameleon.com/file/download/FILE-UUID
     * </p>
     */
    UPLOAD,

    /**
     * Add a string to the top of the stack.
     * <p>
     * input: some string
     * </p>
     * <p>
     * output: parameter
     * </p>
     */
    SET,

    /**
     * Execute a command on the remote VM.
     * <p>
     * input: command
     * </p>
     * <p>
     * output: exit code
     * </p>
     * <p>
     * STD_OUT: std out of this command
     * </p>
     * <p>
     * STD_ERR: std err of this command
     * </p>
     * <p>
     * limitations: the command is put into a bash/... file and then this file is executed
     * </p>
     */
    COMMAND,

    /**
     * Execute a command async.
     * <p>
     * input: command
     * </p>
     * <p>
     * output: N/A
     * </p>
     *
     * @deprecated we will replace it with AFTER_INSTALL or REBOOT.
     */
    @Deprecated
    ASYNC,

    /**
     * Mount a .dmg file.
     * <p>
     * input: .dmg file path
     * </p>
     * <p>
     * output: path to folder with the mounted dmg
     * </p>
     * <p>
     * limitation: only makes sense on Mac. If your .dmg file has some weird EULA mechanism, insert a
     * {@link RecipeMethod#DMG_EULA} before
     * </p>
     */
    DMG,

    /**
     * Install a .pkg file.
     * <p>
     * input: .pkg file path
     * </p>
     * <p>
     * output: exit code
     * </p>
     */
    PKG,

    /**
     * Unzip a *.zip file.
     * <p>
     * input: .zip file path
     * </p>
     * <p>
     * output: path of extracted folder
     * </p>
     */
    UNZIP,

    /**
     * An additional step necessary for some .dmg files.
     * <p>
     * input: path to a .dmg file
     * </p>
     * <p>
     * output: path to new file, that can be mounted with {@link #DMG}
     * </p>
     * <p>
     * limitations: only makes sense on Mac. Also it is not necessary for all .dmg files, use it only if you know what
     * you're doing
     * </p>
     */
    DMG_EULA,

    /**
     * Uninstall an APK based on its name on Android.
     * <p>
     * input: APK name
     * </p>
     * <p>
     * output: N/A
     * </p>
     */
    APK_UNINSTALL,

    /**
     * Install a .apk file on Android.
     * <p>
     * input: path to a .apk file
     * </p>
     * <p>
     * output: N/A
     * </p>
     */
    APK_INSTALL,

    /**
     * Install a .apk file as system app on Android.
     * <p>
     * input: path to a .apk file
     * </p>
     * <p>
     * output: N/A
     * </p>
     */
    APK_INSTALL_SYSTEM,

    /**
     * Copy a file or folder from one place to another.
     * <p>
     * stack[0]: src file/folder
     * </p>
     * <p>
     * input: target file/folder
     * </p>
     * <p>
     * output: target file/folder
     * </p>
     */
    COPY,

    /**
     * Move a file or folder from one place to another.
     * <p>
     * stack[0]: src file/folder
     * </p>
     * <p>
     * input: target file/folder
     * </p>
     * <p>
     * output: target file/folder
     * </p>
     */
    MOVE,

    /**
     * Execute an adb command.
     * <p>
     * input: the command. examples: "push file1 file2", "remount", ...
     * </p>
     * <p>
     * output: exit code (useless, is always 0...)
     * </p>
     */
    ADB,

    /**
     * Write a string to a tmp file.
     * <p>
     * input: string that will be written
     * </p>
     * <p>
     * output: the file the content was written to
     * </p>
     */
    TO_FILE,

    /**
     * The opposite of TO_FILE: this method reads the content of a file and pushes it to the stack.
     * <p>
     * input: file that is read
     * </p>
     * <p>
     * output: content of the file
     * </p>
     */
    FROM_FILE,

    /**
     * Do a reboot at this step. all steps that follow a REBOOT and are deeper than it, are executed after the reboot.
     * The reboot may not be executed immediately, but maybe after all softwares are installed.
     * <p>
     * input: N/A
     * </p>
     * <p>
     * output: N/A
     * </p>
     */
    REBOOT,

    /**
     * Do a reboot right now. this may mess up your software installation and exists for internal purpose only. Please
     * use {@link RecipeMethod#REBOOT} for consistent behavior. This method will NOT save any steps that come after it,
     * it is not ment to appear inside a software installation.
     * <p>
     * input: N/A
     * </p>
     * <p>
     * output: N/A
     * </p>
     */
    REBOOT_NOW,

    /**
     * Wait until a specific software is installed. All elements deeper than the WAIT are executed after the depending
     * sw is finished. If the depending software wasn't ordered at all and thus won't ever be installed, all steps after
     * the wait will be ignored.
     * <p>
     * input: slug of software we are waiting for
     * </p>
     * <p>
     * output: 1 if the software is installed (was ordered), 0 otherwise
     * </p>
     */
    WAIT,

    /**
     * Specify some steps that are executed after the machine is in SOFTWARE_INSTALLED. All steps deeper than this one
     * will be executed then.
     * <p>
     * input: N/A
     * </p>
     * <p>
     * output: N/A
     * </p>
     */
    AFTER_INSTALL,

    /**
     * Checks whether the element {{0}} is equal to the input. Throws an exception otherwise, and the machine will be in
     * SOFTWARE_FAILED.
     * <p>
     * input: the string that is compared to the element on the stack
     * </p>
     * <p>
     * output: 1 if the comparison was successful, an exception otherwise
     * </p>
     */
    EQUALS,

    /**
     * Install a .app folder (compiled source for the simulator) on the virtual machine.
     * <p>
     * input: a path to the .app folder.
     * </p>
     * <p>
     * output: 1 if the installation succeeded, exception otherwise
     * </p>
     */
    IOS_INSTALL,

    /**
     * Install a certificate in the virtual machine.
     * <p>
     * input: path to a .cer file.
     * </p>
     * <p>
     * output: N/A
     * </p>
     */
    INSTALL_CERT,

    /**
     * Move files into the Firefox profile. This step will create a new default Firefox profile the first time it is
     * called.
     * <p>
     * stack[0]: src file/folder
     * </p>
     * <p>
     * input: target file/folder name
     * </p>
     * <p>
     * output: target file/folder (full path)
     * </p>
     * <p>
     * limitations: Make sure it is only executed after the firefox installation finished. You can ensure that by
     * putting it behind a wait step: WAIT(firefox), DOWNLOAD(url/to/cert8.db), MOVE_FF(cert8.db), POP, POP, POP
     * </p>
     */
    MOVE_FF
}
