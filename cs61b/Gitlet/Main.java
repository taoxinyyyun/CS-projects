package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author taoxinyyyun
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        try {
            if (args.length < 1) {
                throw new GitletException("Please enter a command.");
            }
            boolean commandExists = false;
            String command = args[0];
            Repository repository;
            File workingDirectory = new File(System.getProperty("user.dir"));
            File gitletDirectory = new File(".gitlet/");
            if (!command.equals("init") && !isInitialized()) {
                throw new GitletException("Not in an "
                        + "initialized Gitlet directory.");
            }
            if (command.equals("init")) {
                if (gitletDirectory.exists() && gitletDirectory.isDirectory()) {
                    throw new GitletException("A Gitlet version-control system "
                            + "already exists in the current directory.");
                }
                Commit initial = new Commit("initial commit", null, null);
                repository = new Repository(initial);
                File repo = join(gitletDirectory, "repository");
                Utils.writeObject(repo, repository);
            } else {
                File r = join(gitletDirectory, "repository");
                repository = readObject(r, Repository.class);
                if (command.equals("add")) {
                    checkArguments1(args);
                    commandExists = true;
                    File toAdd = join(workingDirectory, args[1]);
                    AddRemoveCommand add = new AddRemoveCommand(repository,
                            args, toAdd);
                    add.run();
                } else if (command.equals("commit")) {
                    checkArguments1(args);
                    commandExists = true;
                    CommitCommand commit = new CommitCommand(repository, args);
                    commit.run();
                } else if (command.equals("checkout")) {
                    commandExists = true;
                    CheckOutCommand checkout = new CheckOutCommand(repository,
                            args);
                    checkout.run();
                }
                if (mainHelper(command, repository, args)) {
                    commandExists = true;
                }
                if (!commandExists) {
                    throw new GitletException("No command with"
                            + " that name exists.");
                }
                File repo = join(gitletDirectory, "repository");
                writeObject(repo, repository);
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /** Check if a gitlet repository is initialized.
     * @return true/false
     */
    public static boolean isInitialized() {
        File gitletDirectory = new File(".gitlet/");
        if (gitletDirectory.exists()) {
            return true;
        }
        return false;
    }

    /** Check the format of arguments.
     * @param args args */
    public static void checkArguments0(String[] args) throws GitletException {
        if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        }
    }

    /** Check the format of arguments.
     * @param args args */
    public static void checkArguments1(String[] args) throws GitletException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
    }

    /** The helper method.
     * @param command the command
     * @param repository the repo
     * @param args the argument
     * @return t/f
     * @throws GitletException
     */
    public static boolean mainHelper(String command, Repository repository,
                                     String[] args) throws GitletException {
        File workingDirectory = new File(System.getProperty("user.dir"));
        boolean commandExists = false;
        if (command.equals("log")) {
            checkArguments0(args);
            commandExists = true;
            LogCommand log = new LogCommand(repository, args);
            log.run();
        } else if (command.equals("rm")) {
            checkArguments1(args);
            commandExists = true;
            File toDel = join(workingDirectory, args[1]);
            AddRemoveCommand rm = new AddRemoveCommand(repository,
                    args, toDel);
            rm.rm();
        } else if (command.equals("global-log")) {
            checkArguments0(args);
            commandExists = true;
            LogCommand log = new LogCommand(repository, args);
            log.runGlobal();
        } else if (command.equals("status")) {
            checkArguments0(args);
            commandExists = true;
            StatusCommand status = new StatusCommand(repository, args);
            status.run();
        } else if (command.equals("find")) {
            checkArguments1(args);
            commandExists = true;
            FindCommand find = new FindCommand(repository, args);
            find.run();
        } else if (command.equals("branch")) {
            checkArguments1(args);
            commandExists = true;
            BranchCommand branch = new BranchCommand(repository, args);
            branch.run();
        } else if (command.equals("rm-branch")) {
            checkArguments1(args);
            commandExists = true;
            BranchCommand branch = new BranchCommand(repository, args);
            branch.remove();
        } else if (command.equals("reset")) {
            checkArguments1(args);
            commandExists = true;
            ResetCommand reset = new ResetCommand(repository, args);
            reset.run();
        } else if (command.equals("merge")) {
            checkArguments1(args);
            commandExists = true;
            MergeCommand merge = new MergeCommand(repository, args);
            merge.run();
        }
        return commandExists;
    }


}
