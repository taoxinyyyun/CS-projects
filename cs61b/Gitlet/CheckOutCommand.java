package gitlet;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.*;

/** A general command that can do different checkouts.
 * @author taoxinyyyun
 */
public class CheckOutCommand extends Command {

    /** the constructor.
     * @param repository the repository
     * @param arg the arguments */
    public CheckOutCommand(Repository repository, String[] arg) {
        super(repository, arg);
    }

    /** The current working directory. */
    private File workingDirectory = new File(System.getProperty("user.dir"));

    /** The repository. */
    private Repository repo = getRepo();

    /** The arguments.*/
    private String[] args = getArgs();

    /** executes the command. */
    public void run() {
        checkArgs(args);
        if (args.length == 4) {
            String id = args[1];
            String name = args[3];
            checkout2(id, name);
        } else if (args.length == 3) {
            String name = args[2];
            checkout1(name);
        } else {
            String name = args[1];
            checkout3(name);
        }
    }

    /** check the format of arguments.
     * @param arg the arguments */
    public void checkArgs(String[] arg) throws GitletException {
        if (arg.length < 2) {
            throw new GitletException("Incorrect operands.");
        }
        if (arg.length == 3 && !"--".equals(arg[1])) {
            throw new GitletException("Incorrect operands.");
        }
        if (arg.length == 4 && !"--".equals(arg[2])) {
            throw new GitletException("Incorrect operands.");
        }
        if (arg.length > 4) {
            throw new GitletException("Incorrect operands.");
        }
    }

    /** Takes the version of the file as it exists in the head commit,
     * and puts it in the working directory, overwriting the version of
     * the file that's already there if there is one.
     * @param fileName the file needs to be checked out
     */
    public void checkout1(String fileName) throws GitletException {
        Commit headCommit = repo.getHeadCommit();
        HashMap<String, Blob> headFiles = headCommit.getTrackedBlobs();
        if (!headFiles.containsKey(fileName)) {
            throw new GitletException("File does not exist in that commit.");
        }
        Blob fileBlob = headFiles.get(fileName);
        restoreFile(fileBlob, fileName);
    }

    /** Takes the version of the file as it exists in the commit with
     *  the given id, and puts it in the working directory, overwriting
     *  the version of the file that's already there if there is one.
     * @param id the commit id
     * @param fileName name of the file
     */
    public void checkout2(String id, String fileName) throws GitletException {
        Commit commitNode = repo.getCommit(id);
        HashMap<String, Blob> commitFiles = commitNode.getTrackedBlobs();
        if (!commitFiles.containsKey(fileName)) {
            throw new GitletException("File does not exist in that commit.");
        }
        Blob fileBlob = commitFiles.get(fileName);
        restoreFile(fileBlob, fileName);
    }


    /** Takes all files in the commit at the head of the given branch,
     *  and puts them in the working directory, overwriting the versions
     *  of the files that are already there if they exist. Also, at the
     *  end of this command, the given branch will now be considered the
     *  current branch (HEAD). Any files that are tracked in the current
     *  branch but are not present in the checked-out branch are deleted.
     * @param branchName name of the branch
     */
    public void checkout3(String branchName) throws GitletException {
        String currBranch = repo.getActiveBranch();
        if (currBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
        }

        String headCode = repo.getBranch(branchName);
        checkoutCommit(headCode);

        repo.setActiveBranch(branchName);
    }

    /** Check out all the files in a commit with commit ID CODE.
     *  Removes tracked files that are not present in that commit.
     *  Clear the stage area afterwards.
     * @param code the commit's SHA1 code
     */
    public void checkoutCommit(String code) throws GitletException {
        Commit headCommit = repo.getHeadCommit();
        HashMap<String, Blob> currFiles = headCommit.getTrackedBlobs();
        Commit headNode = repo.getCommit(code);
        HashMap<String, Blob> headFiles = headNode.getTrackedBlobs();
        if (findUntracked(currFiles, headFiles)) {
            throw new GitletException("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }

        for (Map.Entry<String, Blob> fileSets : headFiles.entrySet()) {
            String fileName = fileSets.getKey();
            Blob fileBlob = fileSets.getValue();
            restoreFile(fileBlob, fileName);
        }

        for (Map.Entry<String, Blob> files : currFiles.entrySet()) {
            String name = files.getKey();
            if (!headFiles.containsKey(name)) {
                repo.delFile(name);
            }
        }
        repo.clearStage();
    }

    /** Restore the file in the current working directory to a given
     *  version. Delete the file if it didn't exist before.
     * @param b the blob representing a previous version
     * @param fileName name of the file
     */
    public void restoreFile(Blob b, String fileName) {
        File workFile = join(workingDirectory, fileName);
        if (b == null) {
            workFile.delete();
        } else {
            byte[] fileContent = b.getContents();
            writeContents(workFile, (Object) fileContent);
        }
    }

    /** Find if any file that would be checked out is currently untracked.
     * @param currSet the current files tracked
     * @param checkoutSet the files that would be checked out
     * @return true/false
     */
    private boolean findUntracked(HashMap<String, Blob> currSet,
                                  HashMap<String, Blob> checkoutSet) {
        for (Map.Entry<String, Blob> files : checkoutSet.entrySet()) {
            String fileName = files.getKey();
            File workFile = join(workingDirectory, fileName);
            if (!currSet.containsKey(fileName) && workFile.exists()) {
                return true;
            }
            if (currSet.containsKey(fileName)) {
                Blob curBlob = currSet.get(fileName);
                Blob checkout = files.getValue();
                if (workFile.exists()) {
                    byte[] workData = readContents(workFile);
                    if (!Arrays.equals(curBlob.getContents(), workData)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
