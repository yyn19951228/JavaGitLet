package gitlet;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by yangyining on 04/05/2017.
 */
public class CNode implements Serializable {
    private String parentsID;
    private String ID;
    private CNode parent;
    private HashSet<String> trackedFiles;
    private String messages;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String commitDate;
    private String currentHead;

    public CNode() {
        trackedFiles = new HashSet<>();
        parent = null;
    }


    public void commit(String parentsID, CNode parent, String m, HashSet<String> fn,Date d,String currentHead) {
        this.parent = parent;
        this.parentsID = parentsID;
        this.messages = m;
        this.trackedFiles.addAll(fn);
        commitDate = dateFormat.format(d);
        this.currentHead = currentHead;
    }

    public String getParentsID() {
        return parentsID;
    }

    public HashSet<String> getTrackedFiles() {
        return trackedFiles;
    }

    public String getMessages() {
        return messages;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public CNode getParent() {
        return parent;
    }


    public void setID(String ID) {
        this.ID = ID;
    }

    public String getID() {
        return this.ID;
    }
    public String getCurrentBranch() {
        return this.currentHead;
    }
}
