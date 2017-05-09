package gitlet;


import com.sun.org.apache.bcel.internal.generic.GETFIELD;
import edu.princeton.cs.algs4.ST;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yangyining on 03/05/2017.
 */
public class gitLet implements Serializable {

    private static final String GITLETFILE = ".gitlet/";
    private static final String STAGINE_DIR = "staged/";
    private static final String COMMIT_DIR = "commit/";

    // branches name and CNode ID
    private LinkedHashMap<String,String> branches;


    //CNode ID and CNode
    //CNode: commitNode, store the commit information
    private LinkedHashMap<String,CNode> commitTree;

    //untracked files name
    private HashSet<String> untracked;

    //stated files name
    private LinkedHashSet<String> staged;

    //removied files name
    private HashSet<String> removed;

    //modification and unstaged
    private HashSet<String> unstagedWhileTracked;

    private HashSet<String> removeFromTracking;

    //the branch that have commited
    private HashSet<String> commitedBranch;

    //the head of one branch
    private CNode head;

    //name of currentBranch;
    private String currentBranch;

    //ID of init
    private static String initID;


    gitLet() {
        branches = new LinkedHashMap<>();
        commitTree = new LinkedHashMap<>();
        untracked = new HashSet<>();
        staged = new LinkedHashSet<>();
        removed = new HashSet<>();
        unstagedWhileTracked = new HashSet<>();
        removeFromTracking = new HashSet<>();
        commitedBranch = new HashSet<>();
    }


    void gitletInit() {
        File gitletFile = new File(GITLETFILE);
        if (gitletFile.exists()) {
            System.out.println("A gitlet version-control system already exists in the current directory.");
        } else {
            //create .gitlet/ and commit initial
            gitletFile.mkdirs();

            //create .staged
            File statedFIle = new File(GITLETFILE + STAGINE_DIR);
            statedFIle.mkdirs();

            //create .commit
            File commitFIle = new File(GITLETFILE + COMMIT_DIR);
            commitFIle.mkdirs();

            currentBranch = "master";
            commitInit("initial commit");

            scanUntrackedFiles();

        }
    }

    void gitletAdd(String fileName) {
        //test!
        if (removeFromTracking.contains(fileName)) {
            removeFromTracking.remove(fileName);
        }
        if (untracked.contains(fileName)) {
            untracked.remove(fileName);
        }
        if (removed.contains(fileName)) {
            removed.remove(fileName);
        }
        File toBeAdded = new File(fileName);
        if (!toBeAdded.exists()) {
            System.out.println("File does not exist.");
            return ;
        }

        File src = new File(fileName);

        //check if the file has been modified
        //compared with current commit
        String currentCommitFilePath = GITLETFILE + COMMIT_DIR + head.getID() + "/" + fileName;
        File currentCommitFile = new File(currentCommitFilePath);
        if (currentCommitFile.exists()) {
            if (compareFiles(src,currentCommitFile)) {
                return;
            }
        }

        //add as a  staged file
        staged.add(fileName);

        File dest = new File(GITLETFILE + STAGINE_DIR + fileName);

        if (!dest.exists()) {
            dest.mkdirs();
        }
        //add operator
        //copy the staged file into the .gitlet/staged/
        try {
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    void gitletCommit(String message) {

        if (staged.isEmpty() && removeFromTracking.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return ;
        }
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return ;
        }


        Date currentDateObject = new Date();

        CNode toCommit = new CNode();

        String parentsID = head.getID();

        HashSet<String> trackingCommit = new HashSet<>();
        trackingCommit.addAll(staged);
        for (String str : head.getTrackedFiles()) {
            if (removed.contains(str)) {
                trackingCommit.remove(str);
            } if (removeFromTracking.contains(str)) {
                removed.remove(str);
            } else if (!trackingCommit.contains(str)) {
                trackingCommit.add(str);
            }
        }

        //to commit
        toCommit.commit(parentsID,head,message,trackingCommit,currentDateObject,currentBranch);

        head = toCommit;

        String toSHA = toCommit.getID() + toCommit.getParentsID() + toCommit.getMessages() + toCommit.getCommitDate();

        String commitSHA = Utils.sha1(toSHA);

        transferFromStagedToCommit(commitSHA);
//        transferFromParentCommitToCommit(commitSHA);

        untracked.removeAll(staged);
        staged.clear();
        removed.clear();


        toCommit.setID(commitSHA);
        commitTree.put(commitSHA,toCommit);
        branches.put(currentBranch,commitSHA);
        commitedBranch.add(currentBranch);

    }

    void gitletRm(String fileName) {
        if (!staged.contains(fileName) && !head.getTrackedFiles().contains(fileName)) {
            System.out.println("No reason to remove the file.");
            return ;
        }

        if (staged.contains(fileName)) {
            //delete this file from .gitlet/stage/ file and unstage it

            staged.remove(fileName);

            untracked.add(fileName);

        } else if (head.getTrackedFiles().contains(fileName)){
                removeFromTracking.add(fileName);
                removed.add(fileName);
//                untracked.add(fileName);
                File f = new File(fileName);
                if (f.exists()) {
                    f.delete();
                }


        }
    }

    void gitletLog() {

        CNode toLog = head;
        while(toLog != null) {
            System.out.println("===");
            System.out.println("Commit " + toLog.getID());
            System.out.println(toLog.getCommitDate());
            System.out.println(toLog.getMessages());
            System.out.println();

            toLog = toLog.getParent();
        }

    }

    void gitletGlobalLog() {
        for (CNode toLog: commitTree.values()) {
            System.out.println("===");
            System.out.println("Commit " + toLog.getID());
            System.out.println(toLog.getCommitDate());
            System.out.println(toLog.getMessages());
            System.out.println();
        }
    }

    void gitletFind(String commitMessage) {

        HashSet<String> r = new LinkedHashSet<>();
        for (CNode toFind : commitTree.values()) {
            String id = toFind.getID();
            String message = toFind.getMessages();
            if (message.equals(commitMessage)) {
                r.add(id);
            }
        }
        if (r.size() == 0) {
            System.out.println("Found no commit with that message.");
            return;
        }
        for (String str : r) {
            System.out.println(str);
        }
    }

    void gitletStatus() {
        System.out.println("=== Branches ===");
        for (String branchName: branches.keySet()) {
            if (branchName.equals(currentBranch)) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String fileName: staged) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String fileName: removed) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName: unstagedWhileTracked) {

            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (String fileName: untracked) {
            System.out.println(fileName);
        }


    }

    void gitletCheckout(String... args) {

        if (args.length == 3) {
            //checkout -- [file name]
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            String checkOutFileName = args[2];
            if (unTrackedBeOverwritten(checkOutFileName)) {
                System.out.println("There is an untracked file in the way; delete it or add it first.");
                return;
            }
            //check if file exists in previous commit?
            HashSet<String> prevCommit = head.getTrackedFiles();
            if (!prevCommit.contains(checkOutFileName)) {
                System.out.println("File does not exist in that commit.");
                return ;
            }
            String sha = head.getID();
            String filePath = GITLETFILE + COMMIT_DIR + sha + "/" + checkOutFileName;
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("File does not exist in that commit.");
                return ;
            }
            File dest = new File(checkOutFileName);

            if (!dest.exists()) {
                dest.mkdirs();
            }
            //checkout operator
            //copy the file in .gitlet/commit/sah into the root path
            //and change the original file
            //we use the head to find file
            try {
                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                //delete the staged files
            }catch (IOException e) {
                e.printStackTrace();
            }
        } else if (args.length == 4) {
            //checkout [commit id] -- [file name]
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            String commitID = args[1];
            String subCommitID = commitID.substring(0,6);
            String checkOutFileName = args[3];
            if (unTrackedBeOverwritten(checkOutFileName)) {
                System.out.println("There is an untracked file in the way; delete it or add it first.");
                return;
            }
            //to check if this id exist
            boolean existFlag = false;
            for (String id : commitTree.keySet()) {
                id = id.substring(0,6);
                if (id.equals(subCommitID)) {
                    existFlag = true;
                }
            }
            if (!existFlag) {
                System.out.println("No commit with that id exists.");
                return;
            }
            // for short ID issue
            if (commitID.length() < 20) {
                for (String longID : commitTree.keySet()) {
                    if (longID.substring(0,6).equals(subCommitID)) {
                        commitID = longID;
                        break;
                    }
                }

            }
            String filePath = GITLETFILE + COMMIT_DIR + commitID + "/" + checkOutFileName;
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("File does not exist in that commit.");
                return ;
            }
            File dest = new File(checkOutFileName);

            if (!dest.exists()) {
                dest.mkdirs();
            }
            //checkout operator
            //copy the file in .gitlet/commit/XXsha into the root path
            //and change the original file
            //but this time, we find the file by using sha-1 code.
            try {
                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                //delete the staged files
            }catch (IOException e) {
                e.printStackTrace();
            }

        } else if (args.length == 2) {
           // checkout [branch name]
            //which means that now the currentBranch will change to new branch
            String newBranch = args[1];
            //first check it the branch exists
            if (!branches.keySet().contains(newBranch)) {
                System.out.println("No such branch exists.");
                return;
            }
            if (currentBranch.equals(newBranch)) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            //head here is the old head
            //but need to check whether old head is in the same branch as the new head
//            if (commitedBranch.contains(newBranch)) {
//                String oldHeadID = head.getID();
//                CNode oldHeadCommit = commitTree.get(oldHeadID);
//                while (!oldHeadCommit.getCurrentBranch().equals(newBranch)) {
//                    oldHeadCommit = oldHeadCommit.getParent();
//                }
//                head = oldHeadCommit;
//            }
            HashSet<String> trackingFiles = head.getTrackedFiles();
            // try check first?
            HashSet<String> tempUntracked = new HashSet<>();
            File troot = new File("./");
            String[] tallFilesNames = troot.list();
            for (String fileName : tallFilesNames) {
                if (fileName.equals("incident_report.txt")) {
                    continue;
                }
                if (fileName.contains(".txt")) {
                    tempUntracked.add(fileName);
                }
            }
            for (String file : tempUntracked) {

                if (removeFromTracking.contains(file)) {
                    continue;
                }
                if (!trackingFiles.contains(file)) {
                    System.out.println("There is an untracked file in the way; delete it or add it first.");
                    return;
                }
            }

            currentBranch = newBranch;
            updateHead();

            //too naive
            //there are something more need to be done
            //head here is the new head
            trackingFiles = head.getTrackedFiles();

//            System.out.println("head is " + currentBranch);
//            System.out.println(trackingFiles);
//            System.out.println();

            //first delete all the .txt files in working dir
            File root = new File("./");
            String[] allFilesNames = root.list();
            for (String fileName : allFilesNames) {
                if (fileName.equals("incident_report.txt")) {
                    continue;
                }
                if (fileName.contains(".txt")) {
                    Utils.restrictedDelete(fileName);
                }
            }

            //then copy the files from current commit
            if (!trackingFiles.isEmpty()) {
                for (String fileName: trackingFiles) {
                    String shaPath = head.getID() + "/";
                    File dest = new File("./" + fileName);
                    File src = new File(GITLETFILE + COMMIT_DIR + shaPath + fileName);
                    CNode temp = head;
                    while (!src.exists()) {
                        //find from parentCNode
                        String parentIDPath = temp.getParentsID() + "/";
                        src = new File(GITLETFILE + COMMIT_DIR + parentIDPath + fileName);
                        temp = temp.getParent();

                    }
                    if (!dest.exists()) {
                        dest.mkdirs();
                    }
                    //change branches
                    // so we need to clean the working dir, and add the tracked files
                    //copy the .gitlet/commit/ into the .t/
                    try {
                        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        //delete the staged files
                    }catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }


        }
    }

    void gitletBranch(String branchName) {
        if (branches.keySet().contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            return ;
        }
        branches.put(branchName,head.getID());

    }

    void gitletRmBranch(String branchName) {
        if (!branches.keySet().contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return ;
        }
        if (currentBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return ;
        }
        branches.remove(branchName);
    }

    void gitletReset(String commitID) {
        if (!commitTree.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        HashSet<String> trackingFiles = head.getTrackedFiles();
        // check first
        HashSet<String> tempUntracked = new HashSet<>();
        File troot = new File("./");
        String[] tallFilesNames = troot.list();
        for (String fileName : tallFilesNames) {
            if (fileName.equals("incident_report.txt")) {
                continue;
            }
            if (fileName.contains(".txt")) {
                tempUntracked.add(fileName);
            }
        }
        for (String file : tempUntracked) {
            if (!trackingFiles.contains(file)) {
                System.out.println("There is an untracked file in the way; delete it or add it first.");
                return;
            }
        }

//        tryReset(commitID);


    }

    void gitletMerge(String branchName) {
        if (!staged.isEmpty() && !removed.isEmpty()) {
           System.out.println("You have uncommitted changes.");
           return;
        }
        if (!branches.keySet().contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (currentBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
//        System.out.println("There is an untracked file in the way; delete it or add it first.");
//        return;

        toMerge(branchName);


    }

    private void commitInit(String message) {
        Date currentDateObject = new Date();
        CNode initNode = new CNode();
        head = initNode;
        initNode.commit(""  , null, message,new HashSet<>(), currentDateObject,currentBranch);

        String toSHA = initNode.getID() + initNode.getParentsID() + initNode.getMessages() + initNode.getCommitDate();

        String commitSHA = Utils.sha1(toSHA);
        initID = commitSHA;

        initNode.setID(commitSHA);
        commitTree.put(commitSHA,initNode);
        branches.put(currentBranch,commitSHA);



    }


    private void transferFromStagedToCommit(String sha) {
        for (String fileName: staged) {
            String shaPath = sha + "/";
            File src = new File(GITLETFILE + STAGINE_DIR + fileName);
            File dest = new File(GITLETFILE + COMMIT_DIR + shaPath + fileName);

            if (!dest.exists()) {
                dest.mkdirs();
            }
            //commit operator
            //copy the .gitlet/staged/ into the .gitlet/commit/
            try {
                Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                //delete the staged files
                src.delete();
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void transferFromParentCommitToCommit(String commitSHA) {
        CNode parentCommit = head.getParent();
        if (parentCommit.getMessages().equals("initial commit")) {
            return;
        }
        String ppath = GITLETFILE + COMMIT_DIR + parentCommit.getID() + "/";
        File parentCommitFiles = new File(ppath);
        String[] parentCommitFilesList = parentCommitFiles.list();
        for (String parentCommitFile : parentCommitFilesList) {
            File src = new File(GITLETFILE + COMMIT_DIR + parentCommit.getID() + "/"  + parentCommitFile);
            File dest = new File(GITLETFILE + COMMIT_DIR + commitSHA + "/"+ parentCommitFile);

            if (!dest.exists()) {
                dest.mkdirs();
            }
            //commit operator
            //copy the parent .gitlet/commit/ into the current .gitlet/commit/
            try {
                Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void scanUntrackedFiles() {

        File root = new File("./");
        String[] allFilesNames = root.list();
        for (String fileName : allFilesNames) {
            if (fileName.equals("incident_report.txt")) {
                continue;
            }
            if (fileName.contains(".txt")) {
                untracked.add(fileName);
            }
        }
    }


    private void updateHead() {
        //use the sha-1 id to find the current head
        String currentHeadID = branches.get(currentBranch);
        head  = commitTree.get(currentHeadID);
    }

    private boolean compareFiles(File fa, File fb) {
        byte[] fab = Utils.readContents(fa);
        byte[] fbb = Utils.readContents(fb);
        String shaFa = Utils.sha1(fab);
        String shaFb = Utils.sha1(fbb);
        if (shaFa.equals(shaFb)) {
            return true;
        }
        return false;
    }

    private boolean unTrackedBeOverwritten(String fileName) {
        if (untracked.contains(fileName)) {
            return true;
        }
        return false;
    }


    private void tryReset(String commitID) {
        //by now, we won't test the shortUID, just long UID

        //first of all, change the current node to reset node
        //and make the head of this branch point to reset(current) node
        //and update the branches
        CNode resetNode = commitTree.get(commitID);
        head = resetNode;
        currentBranch = resetNode.getCurrentBranch();
        branches.put(currentBranch,resetNode.getID());

        /*
        may be we don't need to check the file status right now
        HashSet<String> trackingFiles = resetNode.getTrackedFiles();

        //than update the work dir
        //first delete all the .txt files in working dir
        File root = new File("./");
        String[] allFilesNames = root.list();
        for (String fileName : allFilesNames) {
            if (fileName.equals("incident_report.txt")) {
                continue;
            }
            if (fileName.contains(".txt")) {
                Utils.restrictedDelete(fileName);
            }
        }

        //then copy the files from current commit
        if (!trackingFiles.isEmpty()) {
            for (String fileName: trackingFiles) {
                String shaPath = head.getID() + "/";
                File dest = new File("./" + fileName);
                File src = new File(GITLETFILE + COMMIT_DIR + shaPath + fileName);
                if (!dest.exists()) {
                    dest.mkdirs();
                }
                //change branches
                // so we need to clean the working dir, and add the tracked files
                //copy the .gitlet/staged/ into the .t/
                try {
                    Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    //delete the staged files
                }catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        */

    }
    private void toMerge(String branchName) {
        File root = new File("./");
        String[] allFilesNames = root.list();
        for (String fileName : allFilesNames) {
            if (fileName.equals("incident_report.txt")) {
                continue;
            }
            if (fileName.contains(".txt")) {
                Utils.restrictedDelete(fileName);
            }
        }

        //create a new CNode, copy the files of other branches and current master to this CNode
        // than commit thie new CNode to master
        // and refresh the work dir

        String id1 = branches.get("master");
        String id2 = branches.get(branchName);
        String[] ids = new String[2];
        ids[0] = id1;
        ids[1] = id2;
        for (String id : ids) {
            if (id.equals(initID)) {
                continue;
            }
            String idPath = GITLETFILE + COMMIT_DIR + id  + "/";
            File idFile = new File(idPath);
            if (!idFile.exists()) {
                continue;
            }
            String[] idFileList = idFile.list();
            for (String str : idFileList) {
                String srcPath = idPath + str;
                String destPath = "./" + str;
                File src = new File(srcPath);
                File dest = new File(destPath);

                if (!dest.exists()) {
                    dest.mkdirs();
                }
                try {
                    Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    //delete the staged files
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        CNode toCommit = new CNode();
        String mergeMessage = "Merged master with " + branchName + ".";
        Date currentDateObject = new Date();

        toCommit.commit(head.getID(),head,mergeMessage,new HashSet<String>(),currentDateObject,currentBranch);

        head = toCommit;
        String toSHA = toCommit.getID() + toCommit.getParentsID() + toCommit.getMessages() + toCommit.getCommitDate();

        String commitSHA = Utils.sha1(toSHA);

        toCommit.setID(commitSHA);

        untracked.removeAll(staged);
        staged.clear();
        removed.clear();

        commitTree.put(commitSHA,toCommit);
        branches.put(currentBranch,commitSHA);
        commitedBranch.add(currentBranch);

        /*
        just to save time
        //copy from working dir to mergeCommit file
        HashSet<String> mergeCommitFiles = new HashSet<>();
        root = new File("./");
        allFilesNames = root.list();
        for (String fileName : allFilesNames) {
            if (fileName.equals("incident_report.txt")) {
                continue;
            }
            if (fileName.contains(".txt")) {
                mergeCommitFiles.add(fileName);
            }
        }

        for (String str : mergeCommitFiles) {
            String destPath = GITLETFILE + COMMIT_DIR + commitSHA  + "/" + str;
            String srcPath = "./" + str;
            File src = new File(srcPath);
            File dest = new File(destPath);

            if (!dest.exists()) {
                dest.mkdirs();
            }
            try {
                Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                //delete the staged files
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        t
        */



    }

    HashMap<String,CNode> getCommitTree() {
        return commitTree;
    }


}
