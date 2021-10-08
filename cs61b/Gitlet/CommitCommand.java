package gitlet;

import java.util.HashMap;
import java.io.File;
import java.util.Map;

import static gitlet.Utils.*;

/** Saves a snapshot of certain files in the current commit and staging area
 *  so they can be restored at a later time, creating a new commit.
 * @author taoxinyyyun
 */
public class CommitCommand extends Command {

    /** the constructor.
     * @param repository the repository
     * @param arg the arguments */
    public CommitCommand(Repository repository, String[] arg) {
        super(repository, arg);
    }

    /** The repository. */
    private Repository repo = getRepo();

    /** The arguments.*/
    private String[] args = getArgs();

    /**By default, each commit's snapshot of files will be exactly the same as
     * its parent commit's snapshot of files; it will keep versions of files
     * exactly as they are, and not update them. A commit will only update the
     * contents of files it is tracking that have been staged for addition at
     * the time of commit, in which case the commit will now include the
     * version of the file that was staged instead of the version
     * it got from its parent.
     */
    public void run() throws GitletException {
        if (args.length < 2 || args[1].equals("")) {
            throw new GitletException("Please enter a commit message.");
        }
        File[] stagingFiles = repo.getStagedFiles();
        HashMap<String, File> removed = repo.getRemoveFiles();
        if (stagingFiles.length == 0 && removed.isEmpty()) {
            throw new GitletException("No changes added to the commit.");
        }
        String message = args[1];
        Commit headCommit = repo.getHeadCommit();
        String headCode = headCommit.getSHA1();
        Commit newCommit = new Commit(message, headCode, null);
        HashMap<String, Blob> trackedFiles = newCommit.getTrackedBlobs();

        for (File f : stagingFiles) {
            String name = f.getName();
            Blob newFile = new Blob(f, name);
            if (trackedFiles.containsKey(name)) {
                trackedFiles.replace(name, newFile);
            } else {
                trackedFiles.put(name, newFile);
            }
            f.delete();
        }

        for (Map.Entry<String, File> removeFile : removed.entrySet()) {
            String name = removeFile.getKey();
            trackedFiles.remove(name);
            removed.remove(name);
        }

        String newSHA1 = newCommit.getSHA1();
        String activeBranch = repo.getActiveBranch();
        repo.getBranches().put(activeBranch, newSHA1);
        repo.getCommits().add(newSHA1);
        File commit = new File(".gitlet/" + newSHA1);
        writeObject(commit, newCommit);
    }
}
