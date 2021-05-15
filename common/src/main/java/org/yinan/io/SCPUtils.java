package org.yinan.io;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author yinan
 * @date 2021/5/15
 */
public class SCPUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(SCPUtils.class);

    public static boolean scp(String remoteIp, Integer port,
                           String username, String password,
                           String remoteFileName, String localFileName) {
        SSHClient sshClient = new SSHClient();
        try {
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(remoteIp, port);
            sshClient.authPassword(username, password);
            sshClient.newSCPFileTransfer().download(remoteFileName,
                    localFileName);
            return true;
        } catch (Exception e) {
            LOGGER.error("scp remote error: {}", e.toString());
            return false;
        } finally {
            try {
                sshClient.close();
            } catch (IOException e) {
                LOGGER.error("close scp error: {}", e.toString());
            }
        }

    }
}
