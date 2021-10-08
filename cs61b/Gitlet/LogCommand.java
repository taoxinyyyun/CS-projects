package gitlet;

import java.io.File;
import java.util.HashSet;

import static gitlet.Utils.*;

/** Displays the current commit's history.
 * @author taoxinyyyun
 */
public class LogCommand extends Command {

    /** The gitlet repository. */
    private File gitletDir = new File(".gitlet/");

    /** the constructor.
     * @param repository the repository
     * @param arg the arguments */
    public LogCommand(Repository repository, String[] arg) {
        super(repository, arg);
    }

    /** The repository. */
    private Repository repo = getRepo();

    /** The arguments.*/
    private String[] args = getArgs();

    /** Starting at the current head commit, display information about each
     * commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second parents
     * found in merge commits. For every node in this history, the information
     * it should display is the commit id, the time the commit was made,
     * and the commit message.
     */
    public void run() {
        String currBranch = repo.getActiveBranch();
        String currCode = repo.getBranches().get(currBranch);
        Commit currCommit = repo.getCommit(currCode);

        while (currCommit != null) {
            System.out.println("===");
            System.out.println("commit " + currCommit.getSHA1());
            if (currCommit.getParent2SHA1() != null) {
                String first = currCommit.getParentSHA1().substring(0, 7);
                String second = currCommit.getParent2SHA1().substring(0, 7);
                System.out.println("Merge: " + first + " " + second);
            }
            System.out.println("Date: " + currCommit.getDate());
            System.out.println(currCommit.getMessage() + "\n");

            currCommit = currCommit.getParent();
        }

    }

    /** Displays the global log. */
    public void runGlobal() {
        HashSet<String> commits = repo.getCommits();
        if (commits == null) {
            return;
        }
        for (String com : commits) {
            File commit = join(gitletDir, com);
            Commit c = readObject(commit, Commit.class);
            System.out.println("===");
            System.out.println("commit " + c.getSHA1());
            if (c.getParent2SHA1() != null) {
                String first = c.getParentSHA1().substring(0, 7);
                String second = c.getParent2SHA1().substring(0, 7);
                System.out.println("Merge: " + first + " " + second);
            }
            System.out.println("Date: " + c.getDate());
            System.out.println(c.getMessage() + "\n");
        }
    }


}
