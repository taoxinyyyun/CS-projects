package gitlet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.File;
import static gitlet.Utils.*;

/** A command that merges files from the given branch into the current branch.
 * @author taoxinyyyun
 */
public class MergeCommand extends CheckOutCommand {

    /** the constructor.
     * @param repository the repository
     * @param arg the arguments */
    public MergeCommand(Repository repository, String[] arg) {
        super(repository, arg);
    }

    /** The repository. */
    private Repository repo = getRepo();

    /** The arguments.*/
    private String[] args = getArgs();

    /** The current working directory. */
    private File workingDirectory = new File(System.getProperty("user.dir"));

    /** execute the command.
     * @throws GitletException
     */
    @Override
    public void run() throws GitletException {
        String branchName = args[1];

        File[] staged = repo.getStagedFiles();
        HashMap<String, File> removed = repo.getRemoveFiles();
        if (staged.length != 0 || !removed.isEmpty()) {
            throw new GitletException("You have uncommitted changes.");
        }
        if (!repo.getBranches().containsKey(branchName)) {
            throw new GitletException("A branch with that name "
                    + "does not exist.");
        }
        String activeBranch = repo.getActiveBranch();
        if (branchName.equals(activeBranch)) {
            throw new GitletException("Cannot merge a branch with itself.");
        }

        HashMap<String, String> branches = repo.getBranches();
        Commit currCommit = repo.getHeadCommit();
        Commit givenCommit = repo.getCommit(branches.get(branchName));
        Commit splitCommit = findSplit(currCommit, givenCommit);

        if (splitCommit.getSHA1().equals(givenCommit.getSHA1())) {
            System.out.println("Given branch is an ancestor of "
                    + "the current branch.");
        } else if (splitCommit.getSHA1().equals(currCommit.getSHA1())) {
            checkout3(branchName);
            System.out.println("Current branch fast-forwarded.");
        } else {
            if (!mergeCommit(splitCommit, currCommit, givenCommit)) {
                System.out.println("Encountered a merge conflict.");
            }
            commitMerge(activeBranch, branchName, currCommit, givenCommit);
        }
    }

    /** Find the split point of two branches.
     * @param first the head commit of the current branch
     * @param second the head commit of the given branch
     * @return the split point
     */
    public Commit findSplit(Commit first, Commit second) {
        HashMap<String, Commit> visitedChain = new HashMap<String, Commit>();
        buildVisitedChain(second, visitedChain);
        return findVisited(first, visitedChain);
    }

    /** build the commit chain visited by the given branch.
     * @param node start node
     * @param chain the chain
     */
    public void buildVisitedChain(Commit node, HashMap<String, Commit> chain) {
        if (node != null) {
            chain.put(node.getSHA1(), node);
            buildVisitedChain(node.getParent(), chain);
            if (node.getParent2SHA1() != null) {
                buildVisitedChain(node.getSecParent(), chain);
            }
        }
    }

    /** find the split point of two branches.
     * @param node start node
     * @param chain the commit chain
     * @return the split point
     */
    public Commit findVisited(Commit node, HashMap<String, Commit> chain) {
        if (node.getParent() == null) {
            return node;
        } else if (chain.containsKey(node.getSHA1())) {
            return node;
        } else if (node.getSecParent() != null) {
            return node.getSecParent();
        } else {
            return findVisited(node.getParent(), chain);
        }
    }

    /** Merge the two given commits. Return true if the merge is clean.
     * @param split the split point
     * @param curr the current head commit
     * @param given the given head commit
     * @return true/false
     */
    public boolean mergeCommit(Commit split, Commit curr,
                               Commit given) throws GitletException {
        HashMap<String, Blob> currDiff =
                findModifications(split.getTrackedBlobs(),
                curr.getTrackedBlobs());
        HashMap<String, Blob> givenDiff =
                findModifications(split.getTrackedBlobs(),
                given.getTrackedBlobs());

        Set<String> conflictingFiles = currDiff.keySet();
        conflictingFiles.retainAll(givenDiff.keySet());

        for (String f : givenDiff.keySet()) {
            File check = join(workingDirectory, f);
            if (!curr.getTrackedBlobs().containsKey(f) && check.exists()) {
                throw new GitletException("There is an untracked file in "
                        + "the way; delete it, or add and commit it first.");
            }
        }

        for (Map.Entry<String, Blob> toCheck : givenDiff.entrySet()) {
            if (!conflictingFiles.contains(toCheck.getKey())) {
                String name = toCheck.getKey();
                if (toCheck.getValue() != null) {
                    restoreFile(toCheck.getValue(), name);
                    File stageFile = join(repo.getStaging(), name);
                    byte[] modification = toCheck.getValue().getContents();
                    writeContents(stageFile, (Object) modification);
                } else {
                    File rm = join(workingDirectory, name);
                    repo.getRemoveFiles().put(name, rm);
                    restoreFile(toCheck.getValue(), name);
                }
            }
        }

        for (String conflict : conflictingFiles) {
            Blob currBlob = currDiff.get(conflict);
            Blob givenBlob = givenDiff.get(conflict);
            if (currBlob == null && givenBlob == null) {
                conflictingFiles.remove(conflict);
            } else if (currBlob != null && givenBlob != null
                    && currBlob.getSha1hash().equals(givenBlob.getSha1hash())) {
                conflictingFiles.remove(conflict);
            } else {
                solveConflict(conflict, currBlob, givenBlob);
            }
        }
        return conflictingFiles.isEmpty();
    }

    /** Return files that are modified in the current commit.
     * @param prev blobs of the previous commit
     * @param curr blobs of the current commit
     * @return the modified blobs
     */
    public HashMap<String, Blob> findModifications(HashMap<String, Blob> prev,
                                                   HashMap<String, Blob> curr) {
        HashMap<String, Blob> modifiedFiles = new HashMap<>(curr);
        for (Map.Entry<String, Blob> blobEntry : prev.entrySet()) {
            if (!curr.containsKey(blobEntry.getKey())) {
                modifiedFiles.put(blobEntry.getKey(), null);
            } else {
                String f = blobEntry.getKey();
                Blob b = curr.get(f);
                String code = b.getSha1hash();
                if (code.equals(blobEntry.getValue().getSha1hash())) {
                    modifiedFiles.remove(f);
                }
            }
        }
        return modifiedFiles;
    }

    /** Any files modified in different ways in the current and given
     * branches are in conflict. In this case, replace the contents
     * of the conflicted file and stage the result.
     * @param fileName name of file
     * @param curr current blob
     * @param given given blob
     */
    public void solveConflict(String fileName, Blob curr, Blob given) {
        String currentData = curr != null ? new String(curr.getContents()) : "";
        String givenData = given != null ? new String(given.getContents()) : "";
        String newData = "<<<<<<< HEAD\n" + currentData
                + "=======\n" + givenData + ">>>>>>>\n";
        File f = join(workingDirectory, fileName);
        writeContents(f, newData);
        File s = join(repo.getStaging(), fileName);
        writeContents(s, newData);
    }

    /** Perform the commit for the merge.
     * @param currBranch current branch
     * @param newBranch given branch
     * @param c1 current head commit
     * @param c2 given head commit
     */
    public void commitMerge(String currBranch, String newBranch,
                            Commit c1, Commit c2) {
        String message = "Merged " + newBranch
                + " into " + currBranch + ".";
        String p1 = c1.getSHA1();
        String p2 = c2.getSHA1();
        HashMap<String, File> removed = repo.getRemoveFiles();
        Commit newCommit = new Commit(message, p1, p2);
        HashMap<String, Blob> trackedFiles = newCommit.getTrackedBlobs();

        File[] stagingFiles = repo.getStaging().listFiles();
        if (stagingFiles != null) {
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
        }

        for (Map.Entry<String, File> removeFile : removed.entrySet()) {
            String name = removeFile.getKey();
            trackedFiles.remove(name);
            removed.remove(name);
        }

        String newSHA1 = newCommit.getSHA1();
        repo.getBranches().put(currBranch, newSHA1);
        repo.getCommits().add(newSHA1);
        File commit = new File(".gitlet/" + newSHA1);
        writeObject(commit, newCommit);
    }
}
