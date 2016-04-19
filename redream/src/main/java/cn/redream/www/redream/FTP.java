package cn.redream.www.redream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class FTP {
	public static final String FTP_CONNECT_SUCCESSS = "ftp连接成功";
	public static final String FTP_CONNECT_FAIL = "ftp连接失败";
	public static final String FTP_DISCONNECT_SUCCESS = "ftp断开连接";
	public static final String FTP_FILE_NOTEXISTS = "ftp上文件不存在";

	public static final String FTP_UPLOAD_SUCCESS = "ftp文件上传成功";
	public static final String FTP_UPLOAD_FAIL = "ftp文件上传失败";
	public static final String FTP_UPLOAD_LOADING = "ftp文件正在上传";

	public static final String FTP_DOWN_LOADING = "ftp文件正在下载";
	public static final String FTP_DOWN_SUCCESS = "ftp文件下载成功";
	public static final String FTP_DOWN_FAIL = "ftp文件下载失败";

	public static final String FTP_DELETEFILE_SUCCESS = "ftp文件删除成功";
	public static final String FTP_DELETEFILE_FAIL = "ftp文件删除失败";

	/**
	 * 服务器名.
	 */
	private String hostName;

	/**
	 * 端口号
	 */
	private int serverPort;

	/**
	 * 用户名.
	 */
	private String userName;

	/**
	 * 密码.
	 */
	private String password;

	/**
	 * FTP连接.
	 */
	private FTPClient ftpClient;

	public FTP() {
		this.hostName = "222.30.60.30";
		this.serverPort = 21;
		this.userName = "Cartoon";
		this.password = "Cartoon";
		this.ftpClient = new FTPClient();
	}

	// -------------------------------------------------------文件上传方法------------------------------------------------




	

	// -------------------------------------------------------文件下载方法------------------------------------------------

	/**
	 * 下载单个文件，可实现断点下载.
	 * 
	 * @param serverPath
	 *            Ftp目录及文件路径
	 * @param localPath
	 *            本地目录
	 * @param fileName       
	 *            下载之后的文件名称
	 * @param listener
	 *            监听器
	 * @throws IOException
	 */
	public void downloadSingleFile(String serverPath, String localPath, String fileName, DownLoadProgressListener listener)
			throws Exception {

		// 打开FTP服务
		try {
			this.openConnect();
			listener.onDownLoadProgress(FTP_CONNECT_SUCCESSS, 0, null);
		} catch (IOException e1) {
			e1.printStackTrace();
			listener.onDownLoadProgress(FTP_CONNECT_FAIL, 0, null);
			return;
		}

		// 先判断服务器文件是否存在
//		FTPFile[] files = ftpClient.listFiles(serverPath);
//		if (files.length == 0) {
//			listener.onDownLoadProgress(MainActivity.FTP_FILE_NOTEXISTS, 0, null);
////			return;
//		}

		//创建本地文件夹
		File mkFile = new File(localPath);
		if (!mkFile.exists()) {
			mkFile.mkdirs();
		}

		localPath = localPath +"/"+ fileName;
		// 接着判断下载的文件是否能断点下载
//		long serverSize = files[0].getSize(); // 获取远程文件的长度
		File localFile = new File(localPath);
		long localSize = 0;
		if (localFile.exists()) {
			localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
//			if (localSize >= serverSize) {
//				File file = new File(localPath);
//				file.delete();
//			}
		}

//		FileOutputStream fos = null;
//		fos = new FileOutputStream(localPath);
//		ftpClient.setBufferSize(1024);
//		//设置文件类型（二进制）
//		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
//		ftpClient.retrieveFile(serverPath, fos);
		
		// 进度
		long step = 10000 / 100;
		long process = 0;
		long currentSize = 0;
		// 开始准备下载文件
		OutputStream out = new FileOutputStream(localFile, true);
		ftpClient.setRestartOffset(localSize);
		InputStream input = ftpClient.retrieveFileStream(serverPath);
		byte[] b = new byte[1024];
		int length = 0;
		while ((length = input.read(b)) != -1) {
			out.write(b, 0, length);
			currentSize = currentSize + length;
			if (currentSize / step != process) {
				process = currentSize / step;
				if (process % 5 == 0) {  //每隔%5的进度返回一次
					listener.onDownLoadProgress(FTP_DOWN_LOADING, process, null);
				}
			}
		}
		out.flush();
		out.close();
		input.close();
		
		// 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
		if (ftpClient.completePendingCommand()) {
			listener.onDownLoadProgress(FTP_DOWN_SUCCESS, 0, new File(localPath));
		} else {
			listener.onDownLoadProgress(FTP_DOWN_FAIL, 0, null);
		}

		// 下载完成之后关闭连接
		this.closeConnect();
		listener.onDownLoadProgress(FTP_DISCONNECT_SUCCESS, 0, null);

		return;
	}

	// -------------------------------------------------------文件删除方法------------------------------------------------

	/**
	 * 删除Ftp下的文件.
	 * 
	 * @param serverPath
	 *            Ftp目录及文件路径
	 * @param listener
	 *            监听器
	 * @throws IOException
	 */
	public void deleteSingleFile(String serverPath, DeleteFileProgressListener listener)
			throws Exception {

		// 打开FTP服务
		try {
			this.openConnect();
			listener.onDeleteProgress(FTP_CONNECT_SUCCESSS);
		} catch (IOException e1) {
			e1.printStackTrace();
			listener.onDeleteProgress(FTP_CONNECT_FAIL);
			return;
		}

		// 先判断服务器文件是否存在
		FTPFile[] files = ftpClient.listFiles(serverPath);
		if (files.length == 0) {
			listener.onDeleteProgress(FTP_FILE_NOTEXISTS);
			return;
		}
		
		//进行删除操作
		boolean flag = true;
		flag = ftpClient.deleteFile(serverPath);
		if (flag) {
			listener.onDeleteProgress(FTP_DELETEFILE_SUCCESS);
		} else {
			listener.onDeleteProgress(FTP_DELETEFILE_FAIL);
		}
		
		// 删除完成之后关闭连接
		this.closeConnect();
		listener.onDeleteProgress(FTP_DISCONNECT_SUCCESS);
		
		return;
	}

	// -------------------------------------------------------打开关闭连接------------------------------------------------

	/**
	 * 打开FTP服务.
	 * 
	 * @throws IOException
	 */
	public void openConnect() throws IOException {
		// 中文转码
		ftpClient.setControlEncoding("UTF-8");
		int reply; // 服务器响应值
		// 连接至服务器
		ftpClient.connect(hostName, serverPort);
		// 获取响应值
		reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			// 断开连接
			ftpClient.disconnect();
			throw new IOException("connect fail: " + reply);
		}
		// 登录到服务器
		ftpClient.login(userName, password);
		// 获取响应值
		reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			// 断开连接
			ftpClient.disconnect();
			throw new IOException("connect fail: " + reply);
		} else {
			// 获取登录信息  原来是getSystemType
//			FTPClientConfig config = new FTPClientConfig(ftpClient
//					.getSystemName().split(" ")[0]);
			FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_UNIX);//我知道是unix了
			config.setServerLanguageCode("zh");
			ftpClient.configure(config);
			// 使用被动模式设为默认
			ftpClient.enterLocalPassiveMode();
			// 二进制文件支持
			ftpClient
					.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
		}
	}

	/**
	 * 关闭FTP服务.
	 * 
	 * @throws IOException
	 */
	public void closeConnect() throws IOException {
		if (ftpClient != null) {
			// 退出FTP
			ftpClient.logout();
			// 断开连接
			ftpClient.disconnect();
		}
	}

	// ---------------------------------------------------上传、下载、删除监听---------------------------------------------
	
	/*
	 * 上传进度监听
	 */
	public interface UploadProgressListener {
		public void onUploadProgress(String currentStep, long uploadSize, File file);
	}

	/*
	 * 下载进度监听
	 */
	public interface DownLoadProgressListener {
		public void onDownLoadProgress(String currentStep, long downProcess, File file);
	}

	/*
	 * 文件删除监听
	 */
	public interface DeleteFileProgressListener {
		public void onDeleteProgress(String currentStep);
	}

}
