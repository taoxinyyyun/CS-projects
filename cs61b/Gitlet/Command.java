package gitlet;

/** The abstract class for command.
 * @author taoxinyyyun
 */
public abstract class Command {

    /** The repository. */
    private Repository repo;

    /** The arguments. */
    private String[] args;

    /** the constructor.
     * @param repository the repository
     * @param argument the arguments
     */
    public Command(Repository repository, String[] argument) {
        this.repo = repository;
        this.args = argument;
    }

    /** the repo.
     * @return the repo
     */
    public Repository getRepo() {
        return repo;
    }

    /** the args.
     * @return the args
     */
    public String[] getArgs() {
        return args;
    }

    /** Execute the command.
     */
    abstract void run();
}
