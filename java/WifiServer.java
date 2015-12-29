import java.lang.Override;
import java.lang.Runnable;
import java.lang.System;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;

public class WifiServer{
	private static ServerSocket servers;
	private static boolean exit = false;
	private static String saveFilesPath;
	private static Socket connectSocket;
	private static InputStream ipstream;
	private static FileOutputStream fileopstream;
	private static long fileSize = 0;

	
	public static void main(String[] args){
		saveFilesPath = "/home/fg607/WifiSharedFolder";
		startSocketServer();
		System.out.println("等待接收文件......(输入'exit'退出)");
		String str;
		InputStreamReader stdin = new InputStreamReader(System.in);//get  console input
		BufferedReader bufin = new BufferedReader(stdin);
		while (!exit){
			try {

				str = bufin.readLine();
				if(str.equals("exit")){
					exit = true;
					new Thread(new Runnable() {
						@Override
						public void run() {

							try {
								Thread.sleep(100);
								System.exit(0);

							}catch (InterruptedException e){

								e.printStackTrace();
							}

						}
					}).start();
				}else {

					System.out.println("不能识别的命令！！！");
				}
			}catch (IOException e){
				System.out.println("获取命令出错！！！");

			}

		}

		try {
			servers.close();

		}catch (IOException e){

			System.out.println("关闭服务异常！！！");
		}



		
	}

	public static void startSocketServer(){
		//create a new thread to start server
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub

				try {

					if(servers == null){
						servers = new ServerSocket(2008);
					}

					while(!exit)
					{
						try {
							connectSocket = servers.accept();

							new SocketThread(connectSocket).start();

						} catch (IOException e) {
							// TODO Auto-generated catch block
							System.out.println("服务关闭！");
						}


					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("创建服务失败！！！");
				}

			}

		}).start();


	}

	static class SocketThread extends Thread{

		private Socket socket = null;

		public SocketThread(Socket socket){

			this.socket = socket;

		}

		public void run(){


			//obtain translate filename and create the same file
			
			int fileNumber = receiveFileNmber(socket);
			if(fileNumber == -1 || fileNumber < 0){
				return;
			}
			
			for(int i =0;i< fileNumber;i++){

			File file = createFile(socket,saveFilesPath);

			if(file == null)
				return;

			//notify custom to translate file

			noticeReady(socket,"transmitready");

			//start translating

			receiveFileData(socket,file);
			}

			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("关闭端口异常");
			}


		}
	}

	
	public static int receiveFileNmber(Socket socket){
		
		InputStream ipstream = null;
		String receiveData;
		int number ;
		
		try {
			ipstream = socket.getInputStream();
			byte[] buffer = new byte[1024];
			int temp = 0;
			
			temp = ipstream.read(buffer);
			
			if(temp!=-1){
				receiveData = new String(buffer,0,temp);
				return Integer.parseInt(receiveData);
			}
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return -1;
		
		
		
	}
	public static File createFile(Socket socket,String path){

		InputStream ipstream = null;
		File file = null;
		String filename = null;
                String receiveData;
		try {

			ipstream = socket.getInputStream();

			byte[] buffer = new byte[1024];
			int temp = 0;

			temp = ipstream.read(buffer);
			if(temp!=-1)
				receiveData = new String(buffer,0,temp);
			else
				return file;
                        
                        int flag = receiveData.indexOf("$#$",0);

		        filename = receiveData.substring(0, flag);
                fileSize = Long.decode(receiveData.substring(flag+3));
			String filepath = path+"/"+filename;


			File folder = new File(path);

			if(!folder.exists())
				folder.mkdir();

			file = new File(filepath);
			file.createNewFile();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("网络异常，文件接受失败！！！");
		}

		return file;

	}

	public static void noticeReady(Socket socket,String msg){


		OutputStream opstream = null;

		try {

			opstream = socket.getOutputStream();

			opstream.write(msg.getBytes());

			opstream.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}


	public static void receiveFileData(Socket socket,File file){

		String fileName = file.getName();

		try {

			ipstream = socket.getInputStream();

			fileopstream = new FileOutputStream(file);

			byte[] buffer = new byte[1024*1024];
			int temp = 0;

			System.out.println("********************************");
			String str = "正在接收文件" + fileName + "...........";
			System.out.println(str);
			System.out.println("--------------------------------");
			int receiveSize = 0;
			while((temp= ipstream.read(buffer))!= -1){

				fileopstream.write(buffer, 0, temp);
				
				receiveSize += temp;
				
				if(receiveSize == fileSize){
					break;
				}

			}

			if(receiveSize != fileSize){
				deleteFile(file);
				str = "网络出现异常，文件接收失败！";
				System.out.println(str);
				System.out.println("********************************");
				System.out.println("等待接收文件......(输入'exit'退出)");
				return;
			}


			fileopstream.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();

			deleteFile(file);
			String str = "网络出现异常，文件接收失败！";
			System.out.println(str);
			System.out.println("********************************");
			System.out.println("等待接收文件......(输入'exit'退出)");
			return;
		}
		String str = fileName +"接收完毕！";
		System.out.println(str);
		System.out.println("********************************");
		System.out.println("等待接收文件......(输入'exit'退出)");

	}


	public static void deleteFile(File file){

		if(file.exists()){
			file.delete();
		}


	}




}
