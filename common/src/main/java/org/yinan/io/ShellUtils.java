package org.yinan.io;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author yinan
 * @date 2021/5/15
 */
public class ShellUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(ShellUtils.class);

    /**
     * scp远程文件到本地
     * @param remoteIp 远程ip
     * @param port 远程端口
     * @param username 用户名
     * @param password 密码
     * @param remoteFileName 远程文件地址
     * @param localFileName 本地文件地址
     * @return 是否下载完成
     */
    public static boolean scpDownload(String remoteIp, Integer port,
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
            LOGGER.error("scp download remote {} error: {}", remoteIp, e.toString());
            return false;
        } finally {
            try {
                sshClient.close();
            } catch (IOException e) {
                LOGGER.error("close remote ip {} error: {}", remoteIp, e.toString());
            }
        }

    }

    /**
     * 将本地文件上传到远端
     * @param remoteIp 远程Ip
     * @param port 远程端口
     * @param username 用户名
     * @param password 密码
     * @param remoteFileName 远程文件位置
     * @param localFileName 本地文件位置
     * @return 是否上传成功
     */
    public static boolean scpUpload(String remoteIp, Integer port,
                                    String username, String password,
                                    String remoteFileName, String localFileName,
                                    String exec) {
        SSHClient sshClient = new SSHClient();
        try {
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(remoteIp, port);
            sshClient.authPassword(username, password);
            sshClient.newSCPFileTransfer().upload(localFileName,
                    remoteFileName);
            exec(sshClient, exec);
            return true;
        } catch (Exception e) {
            LOGGER.error("scp upload remote {} error: {}", remoteIp, e.toString());
            return false;
        } finally {
            try {
                sshClient.close();
            } catch (IOException e) {
                LOGGER.error("close remote ip {} error: {}", remoteIp, e.toString());
            }
        }
    }

    /**
     * 执行netstat命令
     * @param remoteIp 远程ip地址
     * @param port 远程端口
     */
    public static boolean telnet(String remoteIp, Integer port, int timeout)  {
        TelnetClient client = new TelnetClient();
        client.setConnectTimeout(timeout);
        try {
            client.connect(remoteIp, port);
        } catch (IOException e) {
            LOGGER.info("telnet remote ip {}, port {} error: {}", remoteIp, port, e.toString());
            return false;
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                LOGGER.error("close telnet ip {}, port {} error: {}", remoteIp, port, e.toString());
            }
        }

        return true;

    }

    public static boolean exec(SSHClient sshClient, String exec) {
        Session session;
        try {
            session = sshClient.startSession();
            session.exec(exec);
        } catch (ConnectionException | TransportException e) {
            LOGGER.error("exec remote shell error: {}", e.toString());
            return false;
        }

        return true;
    }

    public static String getIpAddress() throws UnknownHostException {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("get local ip error: {}", e.toString());
        }
        return InetAddress.getLocalHost().getHostAddress();
    }

}
