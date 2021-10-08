package gitlet;

import java.util.HashMap;
import java.io.File;
import java.util.Arrays;

import static gitlet.Utils.*;

/** Execute the add and remove command. Allows user to add a file to be staged
 *  or remove a file from the staging area.
 * @author taoxinyyyun
 */
public class AddRemoveCommand extends Command {

    /** The file to add or remove. */
    private File toDo;

    /** the constructor.
     * @param repository the repository
     * @param f the file
     * @param arg the arguments */
    public AddRemoveCommand(Repository repository, String[] arg, File f) {
        super(repository, arg);
        toDo = f;
    }

    /** The repository. */
    private Repository repo = getRepo();

    /** The arguments.*/
    private String[] args = getArgs();

    /** Adds a copy of the file as it currently exists to the staging area. */
    public void run() throws GitletException {
        if (!toDo.exists()) {
            throw new GitletException("File does not exist.");
        }
        String fileName = args[1];
        File f = join(repo.getStaging(), fileName);
        if (isCommitted(fileName)) {
            if (f.exists()) {
                restrictedDelete(f);
            }
        } else {
            byte[] addContent = readContents(toDo);
            if (!f.exists() || !Arrays.equals(readContents(f), addContent)) {
                writeContents(f, (Object) addContent);
            }
        }
        repo.getRemoveFiles().remove(fileName);
    }

    /** Unstage the file if it is currently staged for addition. If the file
     *  is tracked in the current commit, stage it for removal and remove the
     *  file from the working directory if the user has not already done so.
     */
    public void rm() throws GitletException {
        String fileName = args[1];

        boolean isStaged = false;
        File[] stagedFiles = repo.getStagedFiles();
        if (stagedFiles != null) {
            for (File f : stagedFiles) {
                if (f.getName().equals(fileName)) {
                    byte[] contents = readContents(f);
                    byte[] contents2 = readContents(toDo);
                    if (Arrays.equals(contents, contents2)) {
                        f.delete();
                        isStaged = true;
                    }
                }
            }
        }

        if (!isCommitted(fileName) && !isStaged) {
            throw new GitletException("No reason to remove the file.");
        }

        if (isCommitted(fileName)) {
            repo.getRemoveFiles().put(fileName, toDo);
            if (toDo.exists()) {
                restrictedDelete(toDo);
            }
        }
    }

    /** Check if the file has already been tracked in the current commit.
     * @param filename name of file
     * @return whether or not it's tracked
     */
    public boolean isCommitted(String filename) {
        String currBranch = repo.getActiveBranch();
        HashMap<String, String> branches = repo.getBranches();
        String currCommit = branches.get(currBranch);
        Commit headCommit = repo.getCommit(currCommit);
        HashMap<String, Blob> contents = headCommit.getTrackedBlobs();
        if (!toDo.exists() && contents.containsKey(filename)) {
            return true;
        } else if (contents.isEmpty() || !contents.containsKey(filename)) {
            return false;
        } else {
            Blob blob = contents.get(filename);
            byte[] fileContent = readContents(toDo);
            return Arrays.equals(fileContent, blob.getContents());
        }
    }
}
