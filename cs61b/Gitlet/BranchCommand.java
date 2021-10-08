package gitlet;

import java.util.HashMap;

/** A command that creates new branches.
 * @author taoxinyyyun
 */
public class BranchCommand extends Command {

    /** the constructor.
     * @param repository the repository
     * @param arg the arguments */
    public BranchCommand(Repository repository, String[] arg) {
        super(repository, arg);
    }

    /** The repository. */
    private Repository repo = getRepo();

    /** The arguments.*/
    private String[] args = getArgs();

    /** Creates a new branch with the given name, and points it
     * at the current headnode. A branch is nothing more than a
     * name for a reference (a SHA-1 identifier)to a commit node. */
    public void run() throws GitletException {
        String branchName = args[1];

        HashMap<String, String> branches = repo.getBranches();
        if (branches.containsKey(branchName)) {
            throw new GitletException("A branch with that "
                    + "name already exists.");
        }
        String activeBranch = repo.getActiveBranch();
        String headCode = branches.get(activeBranch);
        branches.put(branchName, headCode);
    }

    /** Deletes the branch with the given name. This only means to delete
     * the pointer associated with the branch. */
    public void remove() throws GitletException {
        String branchName = args[1];

        HashMap<String, String> branches = repo.getBranches();
        if (!branches.containsKey(branchName)) {
            throw new GitletException("A branch with that "
                    + "name does not exist.");
        }
        String activeBranch = repo.getActiveBranch();
        if (activeBranch.equals(branchName)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        branches.remove(branchName);
    }
}
