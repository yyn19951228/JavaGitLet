package gitlet;

//import java.io.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author You!
 */
public class Main {

    private static final String GITLETFILE = ".gitlet/";
    private static final String STAGINE_DIR = "staged/";
    private static final String COMMIT_DIR = "commit/";
//    private static gitLet gl;
    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.print("Please enter a command.");
            return;
        } else if (args[0].equals("init")) {
            if (args.length > 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitLet gl = new gitLet();
            gl.gitletInit();
            serialize(gl);
            return;
        }
        gitLet gl = (gitLet) deserialize(GITLETFILE + "ser");
        if (!checkOperator(args)) {
            return;
        }
        switch (args[0]) {
            case "add":
                gl.gitletAdd(args[1]);
                break;
            case "commit":
                gl.gitletCommit(args[1]);
                break;
            case "rm":
                gl.gitletRm(args[1]);
                break;
            case "log":
                gl.gitletLog();
                break;
            case "global-log":
                gl.gitletGlobalLog();
                break;
            case "find":
                gl.gitletFind(args[1]);
                break;
            case "status":
                gl.gitletStatus();
                break;
            case "checkout":
                gl.gitletCheckout(args);
                break;
            case "branch":
                gl.gitletBranch(args[1]);
                break;
            case "rm-branch":
                gl.gitletRmBranch(args[1]);
                break;
            case "reset":
                gl.gitletReset(args[1]);
                break;
            case "merge":
                gl.gitletMerge(args[1]);
                break;
            default:System.out.print("No command with that name exists.");
                break;
        }
        serialize(gl);
    }

    static void serialize(Object o) {
        File toSer = new File(GITLETFILE + "ser");

        try {
            FileOutputStream fos = new FileOutputStream(toSer);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(o);
            oos.close();
        } catch (FileNotFoundException e) {
            toSer.mkdirs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Object deserialize(String fileName) {
        Object objToRead = new Object();
        File fileToRead = new File(fileName);

        try {
            ObjectInputStream inp =
                    new ObjectInputStream(new FileInputStream(fileToRead));
            objToRead = (gitLet) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            return null;
        }
        return objToRead;
    }

    private static boolean checkOperator(String... args) {
        switch (args[0]) {
            case "add":
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            case "commit":
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            case "rm":
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            case "log":
                if (args.length > 1) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            case "global-log":
                if (args.length > 1) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            case "find":
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            case "status":
                if (args.length > 1) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            case "checkout":
                if (args.length > 4) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            case "branch":
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            case "rm-branch":
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            case "reset":
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            case "merge":
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    return false;
                }
                break;
            default:break;
        }
        return true;
    }

}
