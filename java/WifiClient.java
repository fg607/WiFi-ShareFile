import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Override;
import java.lang.Runnable;
import java.lang.String;
import java.lang.Thread;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Enumeration;

public class WifiClient {
	
	public static Socket socket =  null;
	public static String ip = null;
	public static String fileName = null;
	public static final  int PORT = 2008;
	public static String osName = null;
	public static ArrayList<FileInfo> mFileInfoList;
	public static String mFilePath;
	public static long mFileSize;
	
	
	
	public static void main(String[] args){

		while(true)
		{
			//get translate file
			mFileInfoList = getTranslateFile();
			/*if(translateFile != null)
			{
				fileName = translateFile.getName();
			}
			else
			{
				continue;
			}*/

			if(mFileInfoList.size() == 0){

				continue;
			}
		
			//scan port opened host 
			if(ip == null){
				
				ip = scanPortOpendHost();
				
			}
	
			
			if(ip == null)
			{
				continue;
			}
			
			//get needed socket
			socket = getSocket();
			if(socket == null)
			{
				continue;
			}

			notifyFilesNumber(mFileInfoList.size());

			for(FileInfo fileInfo:mFileInfoList){

				mFilePath = fileInfo.getFilePath();
				fileName = fileInfo.getFileName();
				mFileSize = fileInfo.getFileSize();

				if(fileInfo.exists()){

					startTranslate();
				}

			}

			mFileInfoList.clear();



		}
		
	}

	public static void startTranslate() {

		//notify server create the same file
		String fileInfo = fileName + "$#$" + mFileSize;
		notifyCreateFile(fileInfo);

		//get the server ready signal
		if(getBackSignal())
        {
            transmitFile(mFilePath);	//translate file
        }
	}


	public static class FileInfo {

		private File file;
		private String filePath;
		private String fileName;
		private long fileSize;

		public FileInfo(String filePath){

			this.filePath = filePath;
			file = new File(filePath);
		}


		public File getFile() throws FileNotFoundException{
			if(file.exists()){

				return file;

			}else{

				throw new FileNotFoundException();
			}

		}

		public boolean exists(){

			return file.exists();
		}

		public String getFilePath() {
			return filePath;
		}

		public String getFileName() {

			if(fileName != null){
				return fileName;
			}

			if(file.exists()){
				return file.getName();
			}else {

				return null;
			}

		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public long getFileSize() {

			if(file.exists()){
				return file.length();
			}else {

				return -1;
			}

		}

		@Override
		public String toString() {
			return "FileInfo{" +
					"file=" + file +
					", filePath='" + filePath + '\'' +
					", fileName='" + fileName + '\'' +
					", fileSize='" + fileSize + '\'' +
					'}';
		}
	}


	/**
	 * destory app
	 */
	public static  void destory(){
		
		//close the socket
				if(socket != null)
				{
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				
				System.exit(0);
		
	}

	public static void notifyFilesNumber(Integer number) {

		OutputStream opstream = null;
		try {
			opstream = socket.getOutputStream();
			opstream.write(String.valueOf(number).getBytes());
			opstream.flush();

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	/**
	 * get needed socket
	 */
	
	public static Socket getSocket()
	{
		Socket socketTemp = null;
		try{
			socketTemp = new Socket(ip,PORT);
			return socketTemp;
			}
		catch(Exception e)
		{
			System.out.println("获取网络连接失败");
			//重置ip,需再次扫描ip
			ip = null;
			return null ;	
		}
	}
	/**
	 *get the server ready signal
	 */
	public static boolean getBackSignal()
	{
		
		try {
			InputStream ipstream = socket.getInputStream();
			byte[] buffer = new byte[1024];
			int temp = 0;
                        String isready;
			temp = ipstream.read(buffer);
			if(temp != -1){
                           isready = new String(buffer,0,temp);
                         }else{
                            return false;
                         }
			
	
			if(isready.equals("transmitready"))
			{
			
				return true;
            }				
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	
	/**
	 * get Linux system localAddress
	 */
	public static String getLinuxLocalAddress()
	{
		Enumeration<NetworkInterface>  netInterfaces = null;
		InetAddress ip = null;
		Enumeration<InetAddress> ips = null;
		try
		{
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while(netInterfaces.hasMoreElements())
			{
				NetworkInterface ni = netInterfaces.nextElement();
				if(ni.getDisplayName().equals("wlan0"))
				{
				     ips = ni.getInetAddresses();
				 	while(ips.hasMoreElements())
					{
						ip = ips.nextElement();
						if(ip != null && (ip instanceof Inet4Address ) ) //get IPv4 address
						{
							return ip.getHostAddress();
						}
						
					}
				}
				
			}
		}
		catch(Exception e){
			
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * scan the port opened hosts
	 */	
	public static String scanPortOpendHost()
	{
		String localIp = null;
		
		//judge different system ,get localhost ip
		try
		{
		    osName = System.getProperty("os.name");
			osName = osName.toLowerCase();
			if(osName.contains("linux"))
			{
				localIp = getLinuxLocalAddress();
			}
			else if(osName.contains("windows")){
				
				localIp = InetAddress.getLocalHost().getHostAddress();
			}
			
		}
		catch(Exception e)
		{
			System.out.println("获取本地ip地址失败！");
			return null;
		}
		
		if(localIp == null){
			System.out.println("获取ＩＰ失败，请检查网络连接是否正常");
			return null;
		}
		//get ip peroid
		int index = localIp.lastIndexOf(".");
		
		String ipPeroid = localIp.substring(0, index+1);
		String lastPeroid = localIp.substring(index+1);
		
		int i_lastPeroid = Integer.parseInt(lastPeroid);
		
		//scan back and forward 10 ip 
		for(int i = 1;i<=10;i++)
		{
			int temp = i_lastPeroid + i;
            int temp1 = i_lastPeroid - i;
			
			if(isPortOpened(ipPeroid + temp))
			{
				return ipPeroid + temp;
			}
			else if(temp1>1)
			{
				if(isPortOpened(ipPeroid + temp1))
				{
					return ipPeroid + temp1;
				}
			} 
		}
		
		System.out.println("没有发现开启服务主机，请检查主机服务是否开启！");
		return null;
	}
	
	
/**
 * get translate file
 */
	public static  ArrayList<FileInfo>  getTranslateFile()
	{
		ArrayList<FileInfo> fileInfoArrayList = new ArrayList<>();
        String str = null;
		String[] sFiles = {};
        InputStreamReader stdin = new InputStreamReader(System.in);//get  console input
        BufferedReader bufin = new BufferedReader(stdin);
		System.out.print("拖入需要传输的文件（输入“exit” 退出）　");

        try {
		str = bufin.readLine();
		} catch (IOException e) {
			System.out.println("读入文件失败！");
		}


        if(str.equals("exit")){
        	destory();
        }
		osName = System.getProperty("os.name");
		osName = osName.toLowerCase();

		if(osName.contains("linux")) {
			sFiles =  str.split("'");
		}else if(osName.contains("windows")){

			sFiles =  str.split("\"");
		}

		for(int i = 0;i < sFiles.length;i++){

			if(sFiles[i].trim() != ""){

				fileInfoArrayList.add(new FileInfo(sFiles[i]));
			}
		}
		return fileInfoArrayList;

       /* str = str.trim();//drop the blank
        str =  str.replaceAll("\"", ""); //drop the "
        str =  str.replaceAll("'", "");//drop the '*/
     


		//check  file illgle
	/*	File  file = new File(str);
		
		if(file.isFile())
			return file;
		else
		{
			System.out.println("不正确的输入！！！");
			return null;
		}*/
		
	}
	
	
	/**
	 * check the port is open
	 */
public static 	boolean isPortOpened(String ip)
{

	Socket tempSocket = null;
	  try {
	    	System.out.println("扫描 ip:"+ip+" 端口:"+PORT+"........");
			System.out.println("");
			tempSocket = new Socket(ip,PORT);
			System.out.println("ip: "+ ip + " " + PORT +"端口服务开放");
			return true;
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	    finally{
	    	if(tempSocket!=null)
	    	{
				try {
					tempSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    }
}

/**
 * 	notify server to create file
 */
public static void notifyCreateFile(String fileInfo)
{
	try {
		OutputStream opstream = socket.getOutputStream();
			opstream.write(fileInfo.getBytes("UTF-8"));
		opstream.flush();
		
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

/**
 * translate file
 */
    public static void transmitFile(String filePath){
		
		InputStream ipstream = null;
		OutputStream opstream = null;
		File file = new File(filePath);
		
		try{

			ipstream = new FileInputStream(file);
			
			 opstream = socket.getOutputStream();
			
			byte[] buffer = new byte[1024*1024];
			
			int temp = 0;
			
			System.out.println("********************************");
			
			String text = "正在传输" + file.getName() +".........";
			System.out.println(text);
			System.out.println("--------------------------------");
			while((temp = ipstream.read(buffer))!=-1)
			{
				
				opstream.write(buffer,0,temp);
				
			}
			opstream.flush();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			text = file.getName() +"传输完成！";
			System.out.println(text);
			System.out.println("********************************");
			
			
		}catch(IOException e){
			
			//e.printStackTrace();
			System.out.println("网络异常，文件传输失败！");
			System.out.println("********************************");
		}
		finally{
			
			try {
				ipstream.close();
				//opstream.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		}


}
