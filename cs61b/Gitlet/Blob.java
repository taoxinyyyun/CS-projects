package gitlet;
import java.io.Serializable;
import java.io.File;

import static gitlet.Utils.sha1;
import static gitlet.Utils.readContents;

/** Essentially the content of files.
 * @author taoxinyyyun
 */
public class Blob implements Serializable {

    /** The SHA1 of this blob. */
    private String sha1hash;

    /** The content of this blob, represented as a byte array. */
    private byte[] contents;

    /** The name of this blob. */
    private String name;

    /** Create a new blob from a file.
     * @param f the file
     * @param filename the file's name */
    public Blob(File f, String filename) {
        this.contents = readContents(f);
        this.sha1hash = sha1((Object) this.contents);
        this.name = filename;
    }

    /** Get the SHA1 of this blob.
     * @return the SHA1 */
    public String getSha1hash() {
        return this.sha1hash;
    }

    /** Get the content of this blob.
     * @return the blob's content */
    public byte[] getContents() {
        return this.contents;
    }

    /** Get the blob's name.
     * @return its name */
    public String getName() {
        return this.name;
    }

}
