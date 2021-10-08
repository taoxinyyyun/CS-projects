package gitlet;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Utils.*;

/** Command for finding a specific commit.
 * @author taoxinyyyun
 */
public class FindCommand extends Command {

    /** The gitlet repository. */
    private File gitletDir = new File(".gitlet/");

    /** Filter out all commits. */
    private FilenameFilter cFilter = (f, name) -> name.matches("[0-9a-f]{40}");

    /** the constructor.
     * @param repository the repository
     * @param arg the arguments */
    public FindCommand(Repository repository, String[] arg) {
        super(repository, arg);
    }

    /** The repository. */
    private Repository repo = getRepo();

    /** The arguments.*/
    private String[] args = getArgs();

    /** Prints out the ids of all commits that have the given commit message,
     *  one per line. If there are multiple such commits, it prints the ids
     *  out on separate lines. The commit message is a single operand; to
     *  indicate a multiword message, put the operand in quotation marks,
     *  as for the commit command below. */
    public void run() throws GitletException {
        String message = args[1];
        File[] commits = gitletDir.listFiles(cFilter);
        List<String> target = new ArrayList<String>();
        for (File commit : commits) {
            Commit c = readObject(commit, Commit.class);
            if (c.getMessage().equals(message)) {
                target.add(c.getSHA1());
            }
        }
        if (target.isEmpty()) {
            throw new GitletException("Found no commit with that message.");
        } else {
            for (String id : target) {
                System.out.println(id);
            }
        }
    }
}
