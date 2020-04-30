package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;


/** CommitTree is a nested tree structure with
 *  Each node representing individual commits */
public class CommitTree implements Serializable {
    
    /** CommitTrees has two instance variables
     * root - which represents the first commitNode when gitlet is initialized
     * head - name of current active branch
     * hashMap - a map of all commits ever created for quick referencing and access
     * treeBranch - a map of branch names to head nodes of branches.
     * workingDir - working folder
     * stagingMap - hashMap of staged files
     * toRemove - files that the next commit will not contain. */
    private CommitNode root;
    private String head;
    private HashMap<String, CommitNode> hashMap;
    private HashMap<String, CommitNode> treeBranch;
    private static File workingDir = new File(".");
    private HashMap<String, byte[]> stagingMap;
    private HashSet<String> toRemove;
    private boolean isConflict;
    
    /** One argument Constructor for the commitTree class which takes in a commit message */
    public CommitTree() {
        workingDir.mkdirs();
        toRemove = new HashSet<>();
        stagingMap = new HashMap<>();
        hashMap = new HashMap<>();
        treeBranch = new HashMap<>();
        root = new CommitNode("initial commit");
        head = "master";
        treeBranch.put(head, root);
        isConflict = false;
    }
    
    /** Adds a current copy of the file (in the workingDir) to the staging area.
     *  Prints error message if file represented by abstract pathname does not exist.
     *  The String filename is a relative path to the file. */
    public void add(String filename) {
        File workFile = new File(workingDir, filename);
        CommitNode curr = treeBranch.get(head);
        /** If file is untracked, then re-track the file. */
        if (!workFile.exists()) {
            System.out.println("File does not exist.");
        } else {
            /** If the current working version of the file is not identical to
             *  the version in head commit, stages it. */
            if (!Arrays.equals(Utils.readContents(workFile), curr.fileMap.get(filename))) {
                /** Writes contents of file from workingDir to stagingDir.*/
                stagingMap.put(filename, Utils.readContents(workFile));
            }
            curr.track(filename);
        }
    }
    
    /** Creates a new commit with staged and tracked files.
     *  NOTE: creates copies of files from the staging area, which may be a different version from
     *  the workingDir.
     *  Clears all files in the staging area after commit, and changes
     *  head pointer to the most recent commit. */
    public void commit(String message) {
        if (!message.trim().isEmpty()) {
            /** Aborts if no staged files and no files marked for untracking */
            CommitNode curr = treeBranch.get(head);
            if (stagingMap.isEmpty() && toRemove.isEmpty()) {
                System.out.println("No changes added to the commit.");
            } else {
                /** Creates a new child node, updates it's files and sets it as head. */
                CommitNode child = new CommitNode(curr.getCommitID(), message);
                curr.commitChildren.add(child);
                child.updateFiles();
                treeBranch.put(head, child);
                /** If stagingDir is not empty, then clears it. */
                stagingMap.clear();
            }
        } else {
            System.out.println("Please enter a commit message.");
        }
    }
    
    /** Retrieves the file from the head commit */
    public void fileCheckout(String filename) {
        CommitNode headNode = treeBranch.get(head);
        /** Checks that the file exists in the head commit*/
        if (headNode.fileMap.containsKey(filename)) {
            /** Copies the file to the workingDir */
            Utils.writeContents(Utils.join(workingDir, filename),
                                headNode.fileMap.get(filename));
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }
    
    /** Retrieves the node with commitID and checks out specific file */
    public void commitCheckout(String commitID, String filename) {
        /** Converts shortUID to long form. */
        for (String longID: hashMap.keySet()) {
            if (longID.substring(0, commitID.length()).equals(commitID)) {
                commitID = longID;
                break;
            }
        }
        /** Checks validity of commitID (Points to a real CommitNode) */
        if (!hashMap.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
        } else if (!hashMap.get(commitID).fileMap.containsKey(filename)) {
            /** Checks validity of filename (Points to real file) */
            System.out.println("File does not exist in that commit.");
        /** Checks if there is an untracked file in the working directory */
        } else {
            /** Writes contents of file in given commit to workingdir */
            Utils.writeContents(new File(workingDir, filename),
                    hashMap.get(commitID).fileMap.get(filename));
        }
    }
    
    /** Checks out all the files in head node of branch, and makes it the current branch */
    public void branchCheckout(String branchname) {
        /** Checks that the branch exists. */
        if (treeBranch.containsKey(branchname)) {
            /** Checks if the branch is already the current branch. */
            if (branchname.equals(head)) {
                System.out.println("No need to checkout the current branch.");
            } else {
                CommitNode node = treeBranch.get(branchname);
                CommitNode headNode = treeBranch.get(head);
                /** Check that no workingDir files that aren't tracked is overwritten.
                 *  Refers to files that are not staged yet or tracked by head commit.*/
                for (String filename: node.fileMap.keySet()) {
                    if (checkWorkingDirUntracked(filename)) {
                        System.out.println("There is an untracked file in the way; delete it "
                                           + "or add it first.");
                        return;
                    }
                }
                /** Removes tracked files that are not present in the given branch. */
                for (String filename: headNode.fileMap.keySet()) {
                    if (!node.fileMap.containsKey(filename)) {
                        Utils.restrictedDelete(filename);
                    }
                }
                /** Retrieves files from head of branch and copies
                 * to workingDir, and changes it the current branch */
                for (String filename: node.fileMap.keySet()) {
                    Utils.writeContents(new File(workingDir, filename),
                                        node.fileMap.get(filename));
                }
                head = branchname;
                /** Clears the staging area. */
                stagingMap.clear();
            }
        } else {
            System.out.println("No such branch exists.");
        }
    }
    
    /** Prints a log of all commits backwards from the most recent one */
    public void log() {
        CommitNode temp = treeBranch.get(head);
        while (temp != null) {
            temp.printlog();
            temp = hashMap.getOrDefault(temp.getCommitParent(), null);
        }
    }
    
    /** Prints an unordered log of all commits */
    public void globalLog() {
        for (CommitNode node: hashMap.values()) {
            node.printlog();
        }
    }
    
    /** Prints out the ids of all commits that have the given commit message, one per line.
     *  If no such commit exists, prints error message. */
    public void find(String message) {
        boolean exception = true;
        for (CommitNode node: hashMap.values()) {
            if (node.getCommitMessage().equals(message)) {
                System.out.println(node.getCommitID());
                exception = false;
            }
        }
        if (exception) {
            System.out.println("Found no commit with that message.");
        }
    }
    
    public void rm(String filename) {
        /** Check if head commit or stagingDir contains file, else error. */
        if (!treeBranch.get(head).fileMap.containsKey(filename)
            && !stagingMap.containsKey(filename)) {
            System.out.println("No reason to remove the file.");
            return;
        } else if (treeBranch.get(head).fileMap.containsKey(filename)) {
            Utils.restrictedDelete(filename);
            treeBranch.get(head).untrack(filename);
            if (stagingMap.containsKey(filename)) {
                stagingMap.remove(filename);
            }
        } else if (!treeBranch.get(head).fileMap.containsKey(filename)
                   && stagingMap.containsKey(filename)) {
            stagingMap.remove(filename);
        }
    }
    
    public void status() {
        System.out.println("=== Branches ===");
        ArrayList<String> sorted = new ArrayList();
        for (String str: treeBranch.keySet()) {
            sorted.add(str);
        }
        Collections.sort(sorted, String.CASE_INSENSITIVE_ORDER);
        for (String branchName: sorted) {
            if (branchName.equals(head)) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println();
        
        System.out.println("=== Staged Files ===");
        for (String filename: stagingMap.keySet()) {
            System.out.println(filename);
        }
        System.out.println();
        
        System.out.println("=== Removed Files ===");
        for (String filename: toRemove) {
            System.out.println(filename);
        }
        System.out.println();
        
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }
    
    public void branch(String name) {
        if (treeBranch.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
        } else {
            treeBranch.put(name, treeBranch.get(head));
        }
    }
    
    public void rmbranch(String branchName) {
        if (!treeBranch.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (head.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            treeBranch.remove(branchName);
        }
    }

    public void reset(String commitID) {
        /** Checks that commit with ID exists. */
        if (!hashMap.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
        } else {
            CommitNode headNode = treeBranch.get(head);
            CommitNode currNode = hashMap.get(commitID);
            /** Check that no workingDir files that aren't tracked is overwritten.
             *  Refers to files that are not staged yet or tracked by head commit. */
            for (String filename : currNode.fileMap.keySet()) {
                if (checkWorkingDirUntracked(filename)) {
                    System.out.println("There is an untracked file in the way; delete it "
                                       + "or add it first.");
                    return;
                }
            }
            /** For files tracked by head commit, and not tracked by resetting commit,
             * remove them. */
            for (String filename : headNode.fileMap.keySet()) {
                if (!currNode.fileMap.containsKey(filename)) {
                    rm(filename);
                }
            }
            /** Retrieves files from commit and copies to workingDir,
             * and changes commit to the current branch head */
            for (String filename : currNode.fileMap.keySet()) {
                Utils.writeContents(new File(workingDir, filename), currNode.fileMap.get(filename));
            }
            treeBranch.put(head, currNode);
            /** Clears the staging area, and removal area. */
            stagingMap.clear();
            toRemove.clear();
        }
    }
    
    /** Helper method to ensure integrity of workingDir. */
    private boolean checkWorkingDirUntracked(String filename) {
        /** If filename is in workingDir, then make sure it's either
           * tracked by head commit or staged. Return true if unsafe.
         * Used by checkout and reset methods. */
        CommitNode headNode = treeBranch.get(head);
        for (String name: Utils.plainFilenamesIn(workingDir)) {
            if (name.equals(filename)
                && !headNode.fileMap.containsKey(filename)
                && !stagingMap.containsKey(filename)) {
                return true;
            }
        }
        return false;
    }

    /** ============================================================================ */
    /** ============================== START OF MERGE ============================== */
    /** ============================================================================ */

    public void merge(String branchName) throws IOException {
        if (!stagingMap.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!treeBranch.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        CommitNode branchHead = treeBranch.get(branchName);
        CommitNode splitNode = splitFinder(branchName, head);
        CommitNode currHead = treeBranch.get(head);
        if (Utils.plainFilenamesIn(workingDir) != null) {
            for (String str: Utils.plainFilenamesIn(workingDir)) {
                if (!currHead.fileMap.containsKey(str)
                        && !stagingMap.containsKey(str)
                        && branchHead.fileMap.containsKey(str)
                        && splitNode.fileMap.containsKey(str)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it or add it first.");
                    return;
                }
            }
        }
        for (String str: Utils.plainFilenamesIn(workingDir)) {
            if (!currHead.fileMap.containsKey(str)
                    && !stagingMap.containsKey(str)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                return;
            }
        }
        if (branchName.equals(head)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        if (splitNode.equals(branchHead)) {
            System.out.println("Given branch is an ancestor of the current branch.");
        } else if (splitNode.equals(currHead)) {
            head = branchName;
            System.out.println("Current branch fast-forwarded");
        } else {
            ArrayList<String> commonFiles = new ArrayList<>();
            ArrayList<String> currSplit = new ArrayList<>();
            ArrayList<String> onlyBranch = new ArrayList<>();
            ArrayList<String> branchCurr = new ArrayList<>();
            ArrayList<String> splitBranch = new ArrayList<>();
            for (String str: splitNode.fileMap.keySet()) {
                if (branchHead.fileMap.containsKey(str)
                        && currHead.fileMap.containsKey(str)) {
                    commonFiles.add(str);
                } else if (!branchHead.fileMap.containsKey(str)
                        && currHead.fileMap.containsKey(str)) {
                    currSplit.add(str);
                }
            }
            for (String str: branchHead.fileMap.keySet()) {
                if (!splitNode.fileMap.containsKey(str)
                        && !currHead.fileMap.containsKey(str)) {
                    onlyBranch.add(str);
                }
                if (!splitNode.fileMap.containsKey(str)
                        && currHead.fileMap.containsKey(str)) {
                    branchCurr.add(str);
                }
                if (splitNode.fileMap.containsKey(str)
                        && currHead.fileMap.containsKey(str)) {
                    splitBranch.add(str);
                }
            }
            caseChecker(commonFiles, currSplit, onlyBranch,
                    branchCurr, splitBranch, splitNode, currHead, branchHead);
            if (isConflict) {
                System.out.println("Encountered a merge conflict.");
                isConflict = false;
            } else {
                this.commit("Merged " + head + " with "
                        + branchName + ".");
            }
        }
    }

    private void caseChecker(ArrayList<String> common,
                             ArrayList<String> currSplit,
                             ArrayList<String> branchOnly, ArrayList<String> branchCurr,
                             ArrayList<String> splitBranch,
                             CommitNode splitNode, CommitNode currHead,
                             CommitNode branchHead) throws IOException {
        /** Cases concerning files present in all three nodes */
        for (String str: common) {

            String splitFileID = Utils.sha1(splitNode.fileMap.get(str));
            String currFileID = Utils.sha1(currHead.fileMap.get(str));
            String branchFileID = Utils.sha1(branchHead.fileMap.get(str));

            /** Case 1: Modified in given branch but not in current branch since split */
            if (splitFileID.equals(currFileID) && !splitFileID.equals(branchFileID)) {
                Utils.writeContents(new File(workingDir, str),
                        branchHead.fileMap.get(str));
                stagingMap.put(str, branchHead.fileMap.get(str));
            } else if (splitFileID.equals(branchFileID) && !splitFileID.equals(currFileID)) {
                /** Case 2: Modified in current but not in given since split */
                continue;
            }
        }

        /** Cases concerning files present in only current and split */
        for (String str: currSplit) {

            String splitFileID = Utils.sha1(splitNode.fileMap.get(str));
            String currFileID = Utils.sha1(currHead.fileMap.get(str));

            /** File is unchanged from split and not present in branch */
            if (splitFileID.equals(currFileID)) {
                this.rm(str);
            }

            /** Current has been modified since split and is not present in the given branch */
            if (!splitFileID.equals(currFileID)) {
                //Magic string concatenation file joining voodoo goes here
                String currFileString = new String(currHead.fileMap.get(str));
                String branchFileString = "";
                String toBeConcatenated = "<<<<<<< HEAD\n" + currFileString
                        + "=======\n" + branchFileString + ">>>>>>>\n";
                byte[] tbcBytes = toBeConcatenated.getBytes();
                currHead.fileMap.put(str, tbcBytes);
                Utils.writeContents(new File(workingDir, str),
                        currHead.fileMap.get(str));
                conflictSwitch();
            }
        }

        /** Cases concerning files present in split and given branches only */
        for (String str: splitBranch) {
            String splitFileID = Utils.sha1(splitNode.fileMap.get(str));
            String branchFileID = Utils.sha1(branchHead.fileMap.get(str));

            /** Case where branch file hasn't changed since split */
            if (splitFileID.equals(branchFileID)) {
                continue;
            } else {
                /** File modified since split and not present in curr */
                String currFileString = new String(currHead.fileMap.get(str));
                String branchFileString = new String(branchHead.fileMap.get(str));
                String toBeConcatenated = "<<<<<<< HEAD\n" + currFileString
                        + "=======\n" + branchFileString + ">>>>>>>\n";
                byte[] tbcBytes = toBeConcatenated.getBytes();
                currHead.fileMap.put(str, tbcBytes);
                Utils.writeContents(new File(workingDir, str),
                        currHead.fileMap.get(str));
                conflictSwitch();
            }
        }

        /** Cases concerning files present in current and given branches */
        for (String str: branchCurr) {

            String currFileID = Utils.sha1(currHead.fileMap.get(str));
            String branchFileID = Utils.sha1(branchHead.fileMap.get(str));

            /** Case where the two files haven't changed at all */
            if (currFileID.equals(branchFileID)) {
                continue;
            } else {
                /** Case where the files are different */
                String currFileString = new String(currHead.fileMap.get(str));
                String branchFileString = new String(branchHead.fileMap.get(str));
                String toBeConcatenated = "<<<<<<< HEAD\n" + currFileString
                        + "=======\n" + branchFileString + ">>>>>>>\n";
                byte[] tbcBytes = toBeConcatenated.getBytes();
                currHead.fileMap.put(str, tbcBytes);
                Utils.writeContents(new File(workingDir, str),
                        currHead.fileMap.get(str));
                conflictSwitch();
            }
        }

        /** Cases concerning files present only in given */
        for (String str: branchOnly) {
            /** All files only present in branch is to be checked out to curr */
            stagingMap.put(str, branchHead.fileMap.get(str));
            Utils.writeContents(new File(workingDir, str),
                    branchHead.fileMap.get(str));
        }
    }

    public void conflictSwitch() {
        if (!isConflict) {
            isConflict = true;
        }
    }

    private CommitNode splitFinder(String branchName, String headbranch) {
        CommitNode headNode = treeBranch.get(headbranch);
        CommitNode branchNode = treeBranch.get(branchName);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date headTime;
        Date branchTime;
        while (headNode != branchNode) {
            headTime = sdf.parse(headNode.commitDate, new ParsePosition(0));
            branchTime = sdf.parse(branchNode.commitDate, new ParsePosition(0));
            if (headTime.compareTo(branchTime) < 0) {
                branchNode = hashMap.get(branchNode.getCommitParent());
            } else {
                headNode = hashMap.get(headNode.getCommitParent());
            }
        } return branchNode;
//        ArrayList<String> headID = new ArrayList();
//        ArrayList<String> branchID = new ArrayList();
//        String splitID = "null";
//        for (int i = 0; headNode != null; i++) {
//            headID.add(i, headNode.commitID);
//            headNode = hashMap.getOrDefault(headNode.getCommitParent(), null);
//        }
//        for (int i = 0; branchNode != null; i++) {
//            branchID.add(i, branchNode.commitID);
//            branchNode = hashMap.getOrDefault(branchNode.getCommitParent(), null);
//        }
//        for (String str: headID) {
//            if (branchID.contains(str)) {
//                splitID = str;
//            } else {
//                continue;
//            }
//        }
//        return treeBranch.get(splitID);
    }

    /** ========================================================================== */
    /** ============================== END OF MERGE ============================== */
    /** ========================================================================== */
    
    
    /** Nested class for commitTree that keeps track of all the content of each commit
     *  which includes the parentID, commit message, Time stamp, and commitID(sha-1 code) */
    private class CommitNode implements Serializable {
        private String commitParent;
        private String commitMessage;
        private String commitDate;
        private ArrayList<CommitNode> commitChildren;
        private String commitID;
        private HashMap<String, byte[]> fileMap;
        
        /** Two argument constructor for commitNode that takes in the parentID and commit message
         *  It creates a time stamp, sha-1 code, assigns the parent of that commitNode and the
         *  appropriate children of that commitNode.
         *  fileMap - hashMap of file names to blobs, keySet also works as tracking check
         *  untrack - list of all files untracked by this commit, the next commit
         *            will not be "tracking" these files.  */
        CommitNode(String parentID, String message) {
            fileMap = new HashMap<>();
            commitParent = parentID;
            commitMessage = message;
            commitDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            commitChildren = new ArrayList<>();
            commitID = Utils.sha1(commitDate, commitMessage);
            hashMap.put(commitID, this);
        }
        
        /** Single input argument constructor that creates the root node with parent null. */
        CommitNode(String message) {
            this(null, message);
        }
        
        /** Getter method for retrieving the sha-1 code of a commitNode */
        public String getCommitID() {
            return commitID;
        }
        
        /** Getter method for retrieving commit of a commitNode */
        public String getCommitMessage() {
            return commitMessage;
        }
        
        /** Getter method for retrieving the parent of a commitNode */
        public String getCommitParent() {
            return commitParent;
        }
        
        /** Getter method for retrieving the date of a commitNode as a string */
        public String getCommitDate() {
            return commitDate;
        }
        
        /** Updater method to retrieve files from ParentNode and staging folder,
         *  and add them to this commit. Used in commit method. */
        private void updateFiles() {
            /** Makes copies of parent node versions. */
            HashMap<String, byte[]> parentFileMap = treeBranch.get(head).fileMap;
            fileMap = new HashMap<>(parentFileMap);
    
            /**  Removes files that are untracked by parent commit, and clear toRemove */
            for (String untrackFile: toRemove) {
                fileMap.remove(untrackFile);
            }
            toRemove.clear();
            
            /** If there is files in the stagingDir, add it to fileMap */
            fileMap.putAll(stagingMap);
        }
        
        /** Helper method for printing logs. */
        private void printlog() {
            System.out.println("===");
            System.out.println("Commit " + getCommitID());
            System.out.println(getCommitDate());
            System.out.println(getCommitMessage());
            System.out.println();
        }
        
        /** Add file to be untracked by commit. */
        private void untrack(String filename) {
            toRemove.add(filename);
        }
        
        /** Re-tracks file in add method */
        private void track(String filename) {
            toRemove.remove(filename);
        }
        
    }


    public static void main(String[] args) {
//        Main.main("init");
//        Main.main(new String[] {"add", "f.txt"});
//        Main.main(new String[] {"add", "g.txt"});
//        Main.main(new String[] {"commit", "TWO FILES"});
        
//        Main.main(new String[] {"branch", "other"});
//        Main.main(new String[] {"add", "h.txt"});
//        Main.main(new String[] {"rm", "g.txt"});
//        Main.main(new String[] {"commit", "Add h.txt and remove g.txt"});
//
//        Main.main(new String[] {"checkout", "other"});
//        Main.main(new String[] {"rm", "f.txt"});
//        Main.main(new String[] {"add", "k.txt"});
//        Main.main(new String[] {"commit", "Add k.txt and remove f.txt"});

        Main.main(new String[] {"log"});
        Main.main(new String[] {"checkout", "master"});
        Main.main(new String[] {"log"});
        Main.main(new String[] {"add", "m.txt"});
        Main.main(new String[] {"find", "TWO FILES"});

//        Main.main(new String[] {"reset", "595089d9fc30a1862d25b88595efe0d8904f692d"});
//        Main.main(new String[] {"status"});
//        Main.main(new String[] {"log"});
//        Main.main(new String[] {"checkout", "other"});
//        Main.main(new String[] {"log"});
//        Main.main(new String[] {"checkout", "master"});
//        Main.main(new String[] {"log"});
//        Main.main(new String[] {"reset", "m.txt"});
//        Main.main(new String[] {"log"});
    }
        
}


