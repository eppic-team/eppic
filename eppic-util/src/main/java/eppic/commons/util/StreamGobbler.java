package eppic.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * Class to fix the problem of subprocesses hanging.
 * Quoting the java doc for the Process class:
 * "Because some native platforms only provide limited buffer size for standard
 * input and output streams, failure to promptly write the input stream or read
 * the output stream of the subprocess may cause the subprocess to block, and
 * even deadlock"
 * See http://java.sun.com/javase/6/docs/api/java/lang/Process.html
 *
 * This fix simply spawns to new threads the reading of stdout/stderr of
 * subprocesses and gobbles their content.
 * In our particular case PyMOL would always hang upon sending too many commands
 * (be it by direct xml-rpc communication, xml-rpc using command buffer file or
 * directly through standard input with pymol -p).
 * The hanging was very platform dependent: in Windows PyMol wouldn't hang at all,
 * in Linux it would hang after a few hundred commands, in Mac it would hang very
 * early after something like 100 commands.
 *
 * Example usage:
 * <code>
 *  Process p = Runtime.getRuntime().exec("my_command");
 *	StreamGobbler s1 = new StreamGobbler ("stdout", p.getInputStream ());
 *	StreamGobbler s2 = new StreamGobbler ("stderr", p.getErrorStream ());
 *	s1.start();
 *	s2.start();
 * </code>
 *
 * This fix was taken from
 * http://www.velocityreviews.com/forums/t130884-process-runtimeexec-causes-subprocess-hang.html
 *
 *
 */
public class StreamGobbler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StreamGobbler.class);
    private String name;
    private InputStream is;
    private Thread thread;

    public StreamGobbler (String name, InputStream is) {
        this.name = name;
        this.is = is;
    }

    public void start () {
        thread = new Thread (this);
        thread.start ();
    }

    public void run () {
        try {
            BufferedReader br = new BufferedReader (new InputStreamReader (is));

            while (br.readLine()!=null) {
                //System.out.println ("[" + name + "] " + line);
            }

            is.close ();

        } catch (Exception ex) {
            logger.error("Problem reading stream {}. Error: {}", name, ex.getMessage());
        }
    }
}