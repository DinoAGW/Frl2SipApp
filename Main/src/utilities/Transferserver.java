package utilities;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Transferserver {
	private String host;
	private int port;
	private String user;
	private String privateKeyPath;
	private String keyPasswort;
	private JSch jsch;
	private Session session;
	private Channel channel;
	private ChannelSftp sftpChannel;

	public Transferserver() throws Exception {
		host = Custom.getSftpAdresse();
		port = 22;
		user = Custom.getSftpUsername();
		privateKeyPath = Drive.home + Custom.getSftpKeyFile();
		keyPasswort = Custom.getSftpKeyPwd();
		jsch = new JSch();
		jsch.addIdentity(privateKeyPath, keyPasswort);
		jsch.setKnownHosts("~/.ssh/known_hosts");
		session = jsch.getSession(user, host, port);
		session.connect();
		channel = session.openChannel("sftp");
		channel.connect();
		sftpChannel = (ChannelSftp) channel;
	}

	public void disconnect() {
		if (sftpChannel != null && sftpChannel.isConnected()) {
			sftpChannel.disconnect();
		}
		if (session != null && session.isConnected()) {
			session.disconnect();
		}
	}

	public void uploadFile(String localFilePath, String remoteFilePath) throws Exception {
		sftpChannel.put(localFilePath, remoteFilePath);
	}

	public List<String> ls(String remoteFilePath) throws Exception {
		List<String> ret = new ArrayList<>();
		Vector<LsEntry> lses = sftpChannel.ls(remoteFilePath);
		for (LsEntry lse : lses) {
			if (lse.getAttrs().isDir()) {
				ret.add(lse.getFilename() + "/");
			} else {
				ret.add(lse.getFilename());
			}
		}
		return ret;
	}

	public void getFile(String remoteFilePath, String localFilePath) throws Exception {
		InputStream is = sftpChannel.get(remoteFilePath);
		Files.copy(is, Paths.get(localFilePath));
	}

	public void removeFile(String remoteFilePath) throws Exception {
		sftpChannel.rm(remoteFilePath);
	}

	public void removeFolder(String remoteFolderPath) throws Exception {
		if (!remoteFolderPath.endsWith("/")) {
			throw new Exception("Ordner muss mit / enden");
		}
//		System.out.println("Aufgabe: lösche '" + remoteFolderPath + "'");
		Vector<LsEntry> lses = sftpChannel.ls(remoteFolderPath);
		for (LsEntry lse : lses) {
			if (lse.getFilename().startsWith(".")) {
				continue;
			}
			if (lse.getAttrs().isDir()) {
				removeFolder(remoteFolderPath + lse.getFilename() + "/");
			} else {
//				System.out.println("Lösche '" + remoteFolderPath + lse.getFilename() + "'");
				sftpChannel.rm(remoteFolderPath + lse.getFilename());
			}
		}
//		System.out.println("Lösche '" + remoteFolderPath + "'");
		sftpChannel.rmdir(remoteFolderPath);
	}

	public static void main(String[] args) throws Exception {
		Transferserver ts = new Transferserver();
		try {
//		ts.uploadFile("test.txt", "/exchange/lza/lza-zbmed/dev/gms/test.txt");
//			ts.ls("/exchange/lza/lza-zbmed/dev/gms/");
//			ts.removeFolder("/exchange/lza/lza-zbmed/test/frl/PROD_2025_07_28_6523540/");
		} finally {
			ts.disconnect();
		}
	}
}
