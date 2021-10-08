package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Utils.*;

/** A Gitlet system is considered "initialized" in a particular location if it
 * has a .gitlet directory there.Most Gitlet commands only need to work
 * when used from a directory where a Gitlet system has been initialized.
 * @author taoxinyyyun
 */
public class Repository implements Serializable {
    private static final long serialVersionUID = -3324283370490125503L;
    /** Length of commit. */
    private static final int COMMITLENGTH = 40;

    /** Maps a branch name to its head commit's SHA1 code. */
    private HashMap<String, String> branches;

    /** Points to the current active branch. */
    private String activeBranch;

    /** The dir that stores all staging files. */
    private File staging;

    /** The current working directory. */
    private File workingDirectory = new File(System.getProperty("user.dir"));

    /** The gitlet directory. */
    private File repo = new File(".gitlet/");

    /** A mapping that maps files staged for removal to their names. */
    private HashMap<String, File> removeFiles;

    /** A set that stores all the commits made. */
    private HashSet<String> commits;

    /** Create a new gitlet repository. Set the staging area.
     * @param initial the initial commit */
    public Repository(Commit initial) {
        repo.mkdir();

        branches = new HashMap<>();
        String initialHash = initial.getSHA1();
        branches.put("master", initialHash);
        activeBranch = "master";

        staging = new File(".gitlet/staging");
        staging.mkdir();

        commits = new HashSet<String>();
        commits.add(initialHash);

        removeFiles = new HashMap<String, File>();

        File f = new File(".gitlet/" + initialHash);
        writeObject(f, initial);
    }

    /** A mapping of branch names to the SHA1 of the head commit.
     * @return the mapping */
    public HashMap<String, String> getBranches() {
        return branches;
    }

    /** Get the current branch.
     * @return the active branch */
    public String getActiveBranch() {
        return activeBranch;
    }

    /** Set the current branch.
     * @param branchname name of branch */
    public void setActiveBranch(String branchname) {
        activeBranch = branchname;
    }

    /** Get a specific branch.
     * @param name of branch
     * @return the SHA1 code of the head commit */
    public String getBranch(String name) throws GitletException {
        if (!branches.containsKey(name)) {
            throw new GitletException("No such branch exists.");
        }
        return branches.get(name);
    }

    /** Get a specific commit.
     * @param commitHash the SHA1 code
     * @return the commit */
    public Commit getCommit(String commitHash) throws GitletException {
        if (commitHash.length() < COMMITLENGTH) {
            for (String commit : commits) {
                if (commitHash.equals(commit.substring(0, 8))) {
                    File c = new File(".gitlet/" + commit);
                    return readObject(c, Commit.class);
                }
            }
        }
        File f = new File(".gitlet/" + commitHash);
        if (!f.exists()) {
            throw new GitletException("No commit with that id exists.");
        }
        return readObject(f, Commit.class);
    }

    /** Get the current head commit.
     * @return the head commit */
    public Commit getHeadCommit() {
        String headCode = branches.get(activeBranch);
        File f = new File(".gitlet/" + headCode);
        return readObject(f, Commit.class);
    }

    /** Get all commits currently stored in the repo.
     * @return all commits */
    public HashSet<String> getCommits() {
        return commits;
    }

    /** Get the staging area.
     * @return the staging directory */
    public File getStaging() {
        return staging;
    }

    /** Get all staged files.
     * @return a file list */
    public File[] getStagedFiles() {
        return staging.listFiles();
    }

    /** get all files staged for removal.
     * @return all files staged for removal */
    public HashMap<String, File> getRemoveFiles() {
        return removeFiles;
    }

    /** Delete a file from the cwd.
     * @param fileName name of file */
    public void delFile(String fileName) {
        File f = join(workingDirectory, fileName);
        if (f.exists()) {
            f.delete();
        }
    }

    /** Clear the staging area. */
    public void clearStage() {
        File[] stagingFiles = staging.listFiles();
        if (stagingFiles != null) {
            for (File staged : stagingFiles) {
                staged.delete();
            }
        }
    }

}
