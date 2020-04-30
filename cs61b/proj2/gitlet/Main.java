package gitlet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;


/** Driver class for Gitlet, the tiny stupid version-control system.
         *  @author
         */
public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        CommitTree gitlet;
        File gitletFolder = new File(".gitlet");
        if (!gitletFolder.exists()) {
            gitletFolder.mkdir();
        }
        /** De-serialize gitlet if it exists, otherwise sets it to null. */
        File inFile = new File(".gitlet/gitlet.dat");
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            gitlet = (CommitTree) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            gitlet = null;
        }
        /** Check error cases. */
        checkErrors(gitlet, args);
            
          /** Parse user inputs */
        if (args[0].equals("checkout")) {
            parseCheckout(gitlet, args);
        } else if (args.length == 1) {
            switch (args[0]) {
                case "init": {
                    if (gitlet == null) {
                        gitlet = new CommitTree();
                    } else {
                        System.out.println(
                                "A gitlet version-control system "
                                + "already exists in the current directory.");
                    }
                    break;
                }
                case "log": {
                    gitlet.log();
                    break;
                }
                case "global-log": {
                    gitlet.globalLog();
                    break;
                }
                case "status": {
                    gitlet.status();
                    break;
                }
                default: {
                    break;
                }
            }
        } else if (args.length == 2) {
            args2(gitlet, args);
        }
    
        /** Serialize gitlet. */
        File outFile = new File(".gitlet/gitlet.dat");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(gitlet);
            out.close();
        } catch (IOException excp) {
            excp.printStackTrace();
        }
    }
    
    public static void checkErrors(CommitTree gitlet, String[] args) {
        ArrayList<String> commands = new ArrayList<>(Arrays.asList("init", "add",
                                                                   "commit", "rm", "log",
                                                                   "global-log", "find", "status",
                                                                   "checkout", "branch",
                                                                   "rm-branch", "reset", "merge"));
        ArrayList<String> commands0arg = new ArrayList<>(Arrays.asList("init", "log", "global-log",
                                                                       "status"));
        ArrayList<String> commands1arg = new ArrayList<>(Arrays.asList("add", "commit", "rm",
                                                                       "find", "checkout",
                                                                       "branch", "rm-branch",
                                                                       "reset", "merge"));
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        } else if (!commands.contains(args[0])) {
            System.out.println("No command with that name exists.");
            return;
        } else if (!((args.length == 1 && commands0arg.contains(args[0]))
                     || (args.length == 2 && commands1arg.contains(args[0]))
                     || (args.length == 3 && args[0].equals("checkout") && args[1].equals("--"))
                     || ((args.length == 4) && args[0].equals("checkout")
                         && args[2].equals("--")))) {
            System.out.println("Incorrect operands");
            return;
        } else if (gitlet == null && !args[0].equals("init")) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }
    }
    
    public static void parseCheckout(CommitTree gitlet, String[] args) {
        switch (args.length) {
            case 2: {
                gitlet.branchCheckout(args[1]);
                break;
            }
            case 3: {
                gitlet.fileCheckout(args[2]);
                break;
            }
            case 4: {
                gitlet.commitCheckout(args[1], args[3]);
                break;
            }
            default: {
                break;
            }
        }
    }
    
    public static void args2(CommitTree gitlet, String[] args) {
        switch (args[0]) {
            case "add": {
                gitlet.add(args[1]);
                break;
            }
            case "commit": {
                gitlet.commit(args[1]);
                break;
            }
            case "rm": {
                gitlet.rm(args[1]);
                break;
            }
            case "find": {
                gitlet.find(args[1]);
                break;
            }
            case "branch": {
                gitlet.branch(args[1]);
                break;
            }
            case "rm-branch": {
                gitlet.rmbranch(args[1]);
                break;
            }
            case "reset": {
                gitlet.reset(args[1]);
                break;
            }
            case "merge": {
                try {
                    gitlet.merge(args[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            default: {
                break;
            }
        }
    }


}







