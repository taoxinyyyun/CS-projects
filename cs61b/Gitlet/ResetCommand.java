package gitlet;

import java.io.File;

/** A command that extends the checkout command.
 * @author taoxinyyyun
 */
public class ResetCommand extends CheckOutCommand {

    /** the constructor.
     * @param repository the repository
     * @param arg the arguments */
    public ResetCommand(Repository repository, String[] arg) {
        super(repository, arg);
    }

    /** The repository. */
    private Repository repo = getRepo();

    /** The arguments.*/
    private String[] args = getArgs();

    /** Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch's head to that commit node. */
    @Override
    public void run() throws GitletException {
        String code = args[1];
        File f = new File(".gitlet/" + code);
        if (!f.exists()) {
            throw new GitletException("No commit with that id exists.");
        }
        checkoutCommit(code);
        String activeBranch = repo.getActiveBranch();
        repo.getBranches().put(activeBranch, code);
    }
}
