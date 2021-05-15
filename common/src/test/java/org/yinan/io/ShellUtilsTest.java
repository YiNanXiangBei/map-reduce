package org.yinan.io;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.apache.commons.net.telnet.TelnetClient;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author yinan
 * @date 2021/5/15
 */
public class ShellUtilsTest {

    @Test
    public void scpDownload() {
        assertTrue(ShellUtils.scpDownload("192.168.1.102", 22, "yinan",
                "Q1w2e3r4t5", "/home/yinan/1.txt", "./1.txt"));
    }

    @Test
    public void scpUpload() throws IOException {
        for (int i = 1; i <= 10; i++) {

            assertTrue(ShellUtils.scpUpload("192.168.1.102", 22,
            "yinan", "Q1w2e3r4t5", "/home/yinan/" + i + ".txt", "./2.txt"));

        }
    }

    @Test
    public void telnet() throws IOException {
        assertTrue(ShellUtils.telnet("192.168.1.102", 22, 10000));
        assertTrue(ShellUtils.telnet("192.168.1.102", 9000, 10000));
        assertFalse(ShellUtils.telnet("192.168.1.102", 2320, 10000));
        assertFalse(ShellUtils.telnet("192.168.2.102", 2320, 10000));

    }

    @Test
    public void exec() throws IOException {
        SSHClient sshClient = new SSHClient();
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        sshClient.connect("192.168.1.102", 22);
        sshClient.authPassword("yinan", "Q1w2e3r4t5");
        assertTrue(ShellUtils.exec(sshClient, "touch /home/yinan/11.txt"));
        sshClient.close();
    }
}