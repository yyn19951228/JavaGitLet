package gitlet;

import static org.junit.Assert.*;

import jh61b.junit.textui;
import org.junit.Test;




public class GitletTest2 {

    @Test
    public void testStatus() {
        Gitlet gitlet = new Gitlet();
        gitlet.init();
        gitlet.add("test1.txt");
        gitlet.add("test2.txt");
        gitlet.status();
        gitlet.commit("testing commit");
        gitlet.status();
    }
    public static void main(String[] args) {
        System.exit(textui.runClasses(GitletTest2.class));
    }

}
