package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;

import static gitlet.Utils.sha1;
import static gitlet.Utils.readObject;

/** Combinations of log messages, other metadata (commit date, author, etc.),
 *  a reference to a tree, and references to parent commits. The repository
 *  also maintains a mapping from branch heads to references to commits,
 *  so that certain important commits have symbolic names.
 * @author taoxinyyyun
 */
public class Commit implements Serializable {
    private static final long serialVersionUID = -6479770706268472431L;

    /** The log message. */
    private String message;

    /** The time when this commit is created. */
    private String date;

    /** The SHA1 of this commit's parent commit. */
    private String parentSHA1;

    /** The SHA1 of its second parent, if it exists. */
    private String parent2SHA1;

    /** The SHA1 of this commit. */
    private String _SHA1;

    /** A mapping of all the blobs tracked by this commit. */
    private HashMap<String, Blob> trackedBlobs;

    /** Create a new commit.
     * @param m the log message
     * @param parent the first parent
     * @param parent2 the second parent. */
    public Commit(String m, String parent, String parent2) {
        Date d;
        if (parent == null) {
            trackedBlobs = new HashMap<String, Blob>();
            d = new Date(0);
        } else {
            File p = new File(".gitlet/" + parent);
            Commit parentNode = readObject(p, Commit.class);
            HashMap<String, Blob> parentBlobs = parentNode.getTrackedBlobs();
            trackedBlobs = new HashMap<String, Blob>();
            trackedBlobs.putAll(parentBlobs);
            if (parent2 != null) {
                File p2 = new File(".gitlet/" + parent2);
                Commit parent2Node = readObject(p2, Commit.class);
                HashMap<String, Blob> blob2 = parent2Node.getTrackedBlobs();
                trackedBlobs.putAll(blob2);
            }
            d = new Date();
        }
        this.message = m;
        this.parentSHA1 = parent;
        this.parent2SHA1 = parent2;
        this.date = new SimpleDateFormat("E MMM d HH:mm:ss yyyy Z").format(d);
        this._SHA1 = sha1(parentSHA1 + parent2SHA1
                + date + message + trackedBlobs);
    }

    /** Get the blobs tracked by this commit.
     * @return the blobs */
    public HashMap<String, Blob> getTrackedBlobs() {
        return trackedBlobs;
    }

    /** Get the commit's SHA1 code.
     * @return the sha1 */
    public String getSHA1() {
        return _SHA1;
    }

    /** Get the parent's SHA1 code.
     * @return the parent code */
    public String getParentSHA1() {
        return parentSHA1;
    }

    /** Get the second parent's SHA1 code.
     * @return the parent2 code */
    public String getParent2SHA1() {
        return parent2SHA1;
    }

    /** Get the commit's parent.
     * @return the parent */
    public Commit getParent() {
        if (parentSHA1 == null) {
            return null;
        }
        File parent = new File(".gitlet/" + parentSHA1);
        return readObject(parent, Commit.class);
    }

    /** Get the commit's second parent.
     * @return the second parent */
    public Commit getSecParent() {
        if (parent2SHA1 == null) {
            return null;
        }
        File parent = new File(".gitlet/"  + parent2SHA1);
        return readObject(parent, Commit.class);
    }

    /** Get the commit's message.
     * @return the commit message.
     */
    public String getMessage() {
        return this.message;
    }

    /** Get the date.
     * @return the date
     */
    public String getDate() {
        return date;
    }
}
