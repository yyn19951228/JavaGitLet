package gitlet;

import static org.junit.Assert.*;

import jh61b.junit.textui;
import org.junit.Test;



public class GitletTest3 {

    @Test
    public void testBranch() {
        Gitlet gitlet = new Gitlet();
        gitlet.init();
        gitlet.add("doge.txt");
        gitlet.add("wug.txt");
        gitlet.commit("Added doge and wug");
        gitlet.log();
        gitlet.branch("awesomeBranch");
        gitlet.add("moreDog.txt");
        gitlet.commit("added moreDog");
        gitlet.checkout("awesomeBranch", 0);
        gitlet.log();
        gitlet.status();
        gitlet.removeBranch("awesomeBranch");
        assertEquals(2, gitlet.getBranches().size());
        gitlet.checkout("master", 0);
        gitlet.removeBranch("awesomeBranch");
        assertEquals(1, gitlet.getBranches().size());
    }
    public static void main(String[] args) {
        System.exit(textui.runClasses(GitletTest3.class));
    }

}
