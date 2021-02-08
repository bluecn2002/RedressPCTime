
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.custom.StyledText;

/**
 * 纠正电脑时间。适用于电脑里面的时间零件老化造成的时间不断小偏移
 * 或者电脑中毒引起的时间不对
 * 本软件需要网络支持
 * 目前只支持windows。 未支持linux
 * @author bluecn
 * @Date 2021-1-28
 * @funtion 校正win系统的时间。linux涉及需要root权限,暂时没做
 */
public class RedressPCTime {
	private static Text text_1;
	private static Text text_2;
	private static StyledText styledText;
	private static Label label_1;
	private Button btnNewButton;
	private Button btnNewButton_1;
	private Button button;

	RedressPCTime mainPane;
	protected static  int sleepTime = 120;
	protected static  int setsleepTime = 120;
	protected static boolean keepGetTime = true;
	protected static Boolean justStart = true;
	protected static int lineCount = 0;
	protected static int locatX = 200;
	protected static int locatY = 200;
	
	static String oldTimeStamp = "0";
	static String newTimeStamp = "0";
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		
		RedressPCTime mainPane = new RedressPCTime();
		
		//先启动新进程，再初始化窗口，才能正常运行
		new Thread(){
			public void run(){
				AdjuTime();
			}
		}.start();
		
		mainPane.initial();
		

		
	}
	
	private void initial() {
		
		Display display = Display.getDefault();
		//禁用掉了最大化按钮
		final Shell shell = new Shell(display,SWT.SHELL_TRIM ^ SWT.MAX);
		shell.setSize(366, 350);
		shell.setText("win系统时间校正");
		//取系统中预置的图标，省得测试运行时还得加个图标文件
        shell.setImage(display.getSystemImage(SWT.ICON_INFORMATION));
        
      //构造系统栏控件
        final Tray tray = display.getSystemTray();
        final TrayItem trayItem = new TrayItem(tray, SWT.NONE);
        
      //程序启动时，窗口是显示的，所以系统栏图标隐藏
        trayItem.setVisible(false);
        trayItem.setToolTipText(shell.getText());
 
        trayItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                toggleDisplay(shell, tray);
            }
        });
 
        final Menu trayMenu = new Menu(shell, SWT.POP_UP);
        MenuItem showMenuItem = new MenuItem(trayMenu, SWT.PUSH);
        showMenuItem.setText("显示窗口(&s)");
        
      //显示窗口，并隐藏系统栏中的图标
        showMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                toggleDisplay(shell, tray);
            }
        });
 
        trayMenu.setDefaultItem(showMenuItem);
 
        new MenuItem(trayMenu, SWT.SEPARATOR);
 
        //系统栏中的退出菜单，程序只能通过这个菜单退出
        MenuItem exitMenuItem = new MenuItem(trayMenu, SWT.PUSH);
        exitMenuItem.setText("退出程序(&x)");
 
        exitMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	keepGetTime = false;
                shell.dispose();
            }
        });
        
      //在系统栏图标点击鼠标右键时的事件，弹出系统栏菜单
        trayItem.addMenuDetectListener(new MenuDetectListener(){
            public void menuDetected(MenuDetectEvent e) {
                trayMenu.setVisible(true);
            }
        });
 
        trayItem.setImage(shell.getImage());
 
        //注册窗口事件监听器
        shell.addShellListener(new ShellAdapter() {
 
            //点击窗口最小化按钮时，窗口隐藏，系统栏显示图标
            public void shellIconified(ShellEvent e) {
                toggleDisplay(shell, tray);
            }
 
            //点击窗口关闭按钮时，并不终止程序，而时隐藏窗口，同时系统栏显示图标
            public void shellClosed(ShellEvent e) {
                e.doit = false; //消耗掉原本系统来处理的事件
                toggleDisplay(shell, tray);
            }
        });
        
        
        
        
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				//终止进程
				keepGetTime = false;
				 //此处设置，为了让点击“继续校时”时可以校正一次日期
				justStart = true;
			}
		});
		btnNewButton.setBounds(24, 272, 80, 27);
		btnNewButton.setText("\u7EC8\u6B62\u6821\u65F6");
		
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		lblNewLabel.setBounds(24, 65, 61, 24);
		lblNewLabel.setText("\u65E5\u5FD7\uFF1A");
		
//		final ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
//		scrolledComposite.setLayout(new FillLayout());
//		scrolledComposite.setBounds(20, 100, 308, 166);
//		
//		final Composite com = new Composite(scrolledComposite,SWT.NONE);
//		com.setLayout(new FillLayout());
//		com.setVisible(false);
//		scrolledComposite.setContent(com);
		
		//加入 SWT.V_SCROLL  就创建了垂直滚动条，不需要ScrolledComposite
		text_1 = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		text_1.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		text_1.setBounds(20, 100, 308, 166);
		
//		
//		scrolledComposite.setContent(styledText);
//		scrolledComposite.setExpandHorizontal(true);
//		scrolledComposite.setExpandVertical(true);
//		scrolledComposite.setMinSize(styledText.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Label label = new Label(shell, SWT.NONE);
		label.setBounds(24, 14, 146, 17);
		label.setText("\u95F4\u9694\u65F6\uFF08\u6BEB\u79D2-\u9ED8\u8BA4120\uFF09\uFF1A");
		
		final Label label_1 = new Label(shell, SWT.NONE);
		label_1.setBounds(24, 37, 134, 17);
		label_1.setText("\u5F53\u524D\u95F4\u9694\u65F6\u4E3A\uFF1A120");
		
		text_2 = new Text(shell, SWT.BORDER);
		text_2.setBounds(176, 11, 73, 23);
		
		Button btnNewButton_1 = new Button(shell, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				
				 String setTime = text_2.getText().trim();
				 //想用正则表达式匹配数字，结果没弄成功
/*				 String matchResult = "";  
				 //正则表达式匹配数字，以防乱输入
			        Pattern pattern = Pattern.compile("[0-9]");  
			        Matcher matcher = pattern.matcher(setTime);  
			        System.out.println("matcher= " + matcher);
			        while (matcher.find()) { matchResult = matcher.group(0);}//只取第一组 
			        System.out.println("setTime= "+ setTime);
			        System.out.println("matchResult= "+ matchResult);
*/				 
				 if( setTime != "" /*&& matchResult != "" */){ 
					 
					try {
						setsleepTime = Integer.parseInt(setTime);
						text_2.setText("");
						label_1.setText("当前间隔时为：" + setsleepTime);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				 }
				
			}
		});
		btnNewButton_1.setBounds(255, 9, 80, 27);
		btnNewButton_1.setText("\u63D0\u4EA4");
		
		Button button = new Button(shell, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				
				//继续校时
				if(keepGetTime != true ){
					keepGetTime = true;
					new Thread(){
						public void run(){
							AdjuTime();
						}
					}.start();
				}
			}
		});
		button.setBounds(110, 272, 80, 27);
		button.setText("\u7EE7\u7EED\u6821\u65F6");
		
		shell.open();
		//置中
		center(shell);
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
						
				//窗口始终在最前面
//				OS.SetWindowPos(shell.handle , OS.HWND_TOPMOST, locatX , locatY , 366, 350 , SWT.NULL);
				display.sleep();
			}
		}
		keepGetTime = false;
		display.dispose();
		
	}

	/**
	 * 给日志文本框添加获得的时间
	 * @param text 要添加的时间或日期或报错
	 */
	private static void AppendText(String text) {
		text_1.append(text + "\r\n");
		lineCount++;
		if(lineCount>300){
			text_1.setText("");
			lineCount = 0;
		}

	}
	
	/**
     * 窗口是可见状态时，则隐藏窗口，同时把系统栏中图标删除
     * 窗口是隐藏状态时，则显示窗口，并且在系统栏中显示图标
     * @param shell 窗口
     * @param tray 系统栏图标控件
     */
    private static void toggleDisplay(Shell shell, Tray tray) {
        try {
            shell.setVisible(!shell.isVisible());
            tray.getItem(0).setVisible(!shell.isVisible());
            if (shell.getVisible()) {
                shell.setMinimized(false);
                shell.setActive();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /**
     * 窗口居中显示
     * @param shell 要显示的窗口
     */
    private static void center(Shell shell){
        Monitor monitor = shell.getMonitor();
        Rectangle bounds = monitor.getBounds ();
        Rectangle rect = shell.getBounds ();
        locatX = bounds.x + (bounds.width - rect.width) / 2;
        locatY = bounds.y + (bounds.height - rect.height) / 2;
        shell.setLocation (locatX, locatY);
        
        
    }

	/**
	 * 纠正时间部件，先获取网络时间，再纠正
	 */
	private static void AdjuTime() {

		String keyUrl = "http://api.m.taobao.com/rest/api3.do?api=mtop.common.getTimestamp";
		String rLine;	
		
		  while(keepGetTime){
			  try{
				  //开始网络请求
				  URL url = new URL(keyUrl);
				  HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				  //设置超时时间
				  urlConnection.setConnectTimeout(3000);
				  urlConnection.setReadTimeout(3000);
				  InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream(),"utf-8");
				  BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				  //读取网页内容
				  while ((rLine = bufferedReader.readLine()) != null){
					   //判断目标节点是否出现
					   if(rLine.contains("SUCCESS")){

						    String[] temp = rLine.split("\"");
	//					    System.out.println(temp[17]);
						   
						    //记录时间差，只有延时小的时候才用来纠正系统时间
						    oldTimeStamp = newTimeStamp;
						    newTimeStamp = temp[17];

						    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(temp[17])), ZoneId.systemDefault());

						    final LocalDate nowDate= localDateTime.toLocalDate();
						    final LocalTime nowTime= localDateTime.toLocalTime();
						    
						    //swt的非UI进程操作UI进程时的标准写法				   
					        Display.getDefault().asyncExec(new Runnable() {
			                 public void run() {
			                     //对控件的操作代码
			                	 RedressPCTime.AppendText(nowTime.toString());
			                	
			                	 if(justStart){
			                		 RedressPCTime.AppendText("准备更新日期： " + nowDate.toString());
			                	 }
			                 	}
					        });
					      
						    
						     //修改系统时间
					        //两次时间值相差在120--240之间才改时间
					        if( ( Long.parseLong(newTimeStamp) - Long.parseLong(oldTimeStamp) > Integer.toUnsignedLong(sleepTime) ) 
					        		&& ( Long.parseLong(newTimeStamp) - Long.parseLong(oldTimeStamp) <Integer.toUnsignedLong(sleepTime + 120) ) ){
					        	
					        	sleepTime = 5000;
					        	
					        	try{
									/**
									 * 获取操作系统的名称
									 * */
									String name = System.getProperty("os.name").toLowerCase();
									if(name.contains("windows")){	// Window 操作系统
										String cmd = "cmd.exe /c time " + nowTime.toString().substring(0, 8);
										 Runtime.getRuntime().exec(cmd); // 修改时间
										 if(justStart){
											 
											 cmd = "cmd.exe /c date " + nowDate.toString();
											 Runtime.getRuntime().exec(cmd); // 修改日期
											 justStart = false;
										 }
									}
	//								if(name.contains("linux")){
	//					                // Linux 系统 格式：yyyy-MM-dd HH:mm:ss   date -s "2017-11-11 11:11:11"
	//					                FileWriter excutefw = new FileWriter("/usr/updateSysTime.sh");
	//					                BufferedWriter excutebw=new BufferedWriter(excutefw);
	//					                excutebw.write("date -s \"" + nowDate +" "+ nowTime +"\"\r\n");
	//					                excutebw.close();
	//					                excutefw.close();
	//					                String cmd_date ="sh /usr/updateSysTime.sh";
	//					                String res = runAndResult(cmd_date);
	//					                System.out.println("cmd :" + cmd_date + " date :" + dataStr_ +" time :" + timeStr_);
	//					                System.out.println("linux 时间修改" + res);
	//								}
									
								}catch(Exception e){
									e.printStackTrace();
								}
					        } else{
					        	sleepTime = setsleepTime;
					        }
					   }
				  }
				  
				  //关闭管道资源，久用后莫名阻塞可能跟没关闭有关，前面网络阻塞已经settimeout
				  if(bufferedReader !=null){bufferedReader.close();}
				  if(inputStreamReader != null){inputStreamReader.close();}
				  urlConnection.disconnect();
				  
			  } catch(UnknownHostException uhe){

	             Display.getDefault().asyncExec(new Runnable() {
	                 public void run() {
	                     //对控件的操作代码
	                	 RedressPCTime.AppendText("无法连接对方主机。");
	                 }
	             });
	  
				  uhe.printStackTrace();
			  } catch(java.net.SocketException se){

		             Display.getDefault().asyncExec(new Runnable() {
		                 public void run() {
		                     //对控件的操作代码
		                	 RedressPCTime.AppendText("网络错误。");
		                 }
		             });

		   		  se.printStackTrace();
			  } catch(java.net.SocketTimeoutException ste){
				  
				  Display.getDefault().asyncExec(new Runnable() {
		                 public void run() {
		                     //对控件的操作代码
		                	 RedressPCTime.AppendText("连接超时。");
		                 }
		             });

		   		  ste.printStackTrace();
			  } catch (Exception e2) {
				  e2.printStackTrace();
			  }
			  
			  try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  
	}
	
	
	
}



