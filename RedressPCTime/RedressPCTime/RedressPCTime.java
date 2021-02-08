
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
 * ��������ʱ�䡣�����ڵ��������ʱ������ϻ���ɵ�ʱ�䲻��Сƫ��
 * ���ߵ����ж������ʱ�䲻��
 * �������Ҫ����֧��
 * Ŀǰֻ֧��windows�� δ֧��linux
 * @author bluecn
 * @Date 2021-1-28
 * @funtion У��winϵͳ��ʱ�䡣linux�漰��ҪrootȨ��,��ʱû��
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
		
		//�������½��̣��ٳ�ʼ�����ڣ�������������
		new Thread(){
			public void run(){
				AdjuTime();
			}
		}.start();
		
		mainPane.initial();
		

		
	}
	
	private void initial() {
		
		Display display = Display.getDefault();
		//���õ�����󻯰�ť
		final Shell shell = new Shell(display,SWT.SHELL_TRIM ^ SWT.MAX);
		shell.setSize(366, 350);
		shell.setText("winϵͳʱ��У��");
		//ȡϵͳ��Ԥ�õ�ͼ�꣬ʡ�ò�������ʱ���üӸ�ͼ���ļ�
        shell.setImage(display.getSystemImage(SWT.ICON_INFORMATION));
        
      //����ϵͳ���ؼ�
        final Tray tray = display.getSystemTray();
        final TrayItem trayItem = new TrayItem(tray, SWT.NONE);
        
      //��������ʱ����������ʾ�ģ�����ϵͳ��ͼ������
        trayItem.setVisible(false);
        trayItem.setToolTipText(shell.getText());
 
        trayItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                toggleDisplay(shell, tray);
            }
        });
 
        final Menu trayMenu = new Menu(shell, SWT.POP_UP);
        MenuItem showMenuItem = new MenuItem(trayMenu, SWT.PUSH);
        showMenuItem.setText("��ʾ����(&s)");
        
      //��ʾ���ڣ�������ϵͳ���е�ͼ��
        showMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                toggleDisplay(shell, tray);
            }
        });
 
        trayMenu.setDefaultItem(showMenuItem);
 
        new MenuItem(trayMenu, SWT.SEPARATOR);
 
        //ϵͳ���е��˳��˵�������ֻ��ͨ������˵��˳�
        MenuItem exitMenuItem = new MenuItem(trayMenu, SWT.PUSH);
        exitMenuItem.setText("�˳�����(&x)");
 
        exitMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	keepGetTime = false;
                shell.dispose();
            }
        });
        
      //��ϵͳ��ͼ��������Ҽ�ʱ���¼�������ϵͳ���˵�
        trayItem.addMenuDetectListener(new MenuDetectListener(){
            public void menuDetected(MenuDetectEvent e) {
                trayMenu.setVisible(true);
            }
        });
 
        trayItem.setImage(shell.getImage());
 
        //ע�ᴰ���¼�������
        shell.addShellListener(new ShellAdapter() {
 
            //���������С����ťʱ���������أ�ϵͳ����ʾͼ��
            public void shellIconified(ShellEvent e) {
                toggleDisplay(shell, tray);
            }
 
            //������ڹرհ�ťʱ��������ֹ���򣬶�ʱ���ش��ڣ�ͬʱϵͳ����ʾͼ��
            public void shellClosed(ShellEvent e) {
                e.doit = false; //���ĵ�ԭ��ϵͳ��������¼�
                toggleDisplay(shell, tray);
            }
        });
        
        
        
        
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				//��ֹ����
				keepGetTime = false;
				 //�˴����ã�Ϊ���õ��������Уʱ��ʱ����У��һ������
				justStart = true;
			}
		});
		btnNewButton.setBounds(24, 272, 80, 27);
		btnNewButton.setText("\u7EC8\u6B62\u6821\u65F6");
		
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setFont(SWTResourceManager.getFont("΢���ź�", 12, SWT.NORMAL));
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
		
		//���� SWT.V_SCROLL  �ʹ����˴�ֱ������������ҪScrolledComposite
		text_1 = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		text_1.setFont(SWTResourceManager.getFont("΢���ź�", 12, SWT.NORMAL));
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
				 //����������ʽƥ�����֣����ûŪ�ɹ�
/*				 String matchResult = "";  
				 //������ʽƥ�����֣��Է�������
			        Pattern pattern = Pattern.compile("[0-9]");  
			        Matcher matcher = pattern.matcher(setTime);  
			        System.out.println("matcher= " + matcher);
			        while (matcher.find()) { matchResult = matcher.group(0);}//ֻȡ��һ�� 
			        System.out.println("setTime= "+ setTime);
			        System.out.println("matchResult= "+ matchResult);
*/				 
				 if( setTime != "" /*&& matchResult != "" */){ 
					 
					try {
						setsleepTime = Integer.parseInt(setTime);
						text_2.setText("");
						label_1.setText("��ǰ���ʱΪ��" + setsleepTime);
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
				
				//����Уʱ
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
		//����
		center(shell);
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
						
				//����ʼ������ǰ��
//				OS.SetWindowPos(shell.handle , OS.HWND_TOPMOST, locatX , locatY , 366, 350 , SWT.NULL);
				display.sleep();
			}
		}
		keepGetTime = false;
		display.dispose();
		
	}

	/**
	 * ����־�ı�����ӻ�õ�ʱ��
	 * @param text Ҫ��ӵ�ʱ������ڻ򱨴�
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
     * �����ǿɼ�״̬ʱ�������ش��ڣ�ͬʱ��ϵͳ����ͼ��ɾ��
     * ����������״̬ʱ������ʾ���ڣ�������ϵͳ������ʾͼ��
     * @param shell ����
     * @param tray ϵͳ��ͼ��ؼ�
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
     * ���ھ�����ʾ
     * @param shell Ҫ��ʾ�Ĵ���
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
	 * ����ʱ�䲿�����Ȼ�ȡ����ʱ�䣬�پ���
	 */
	private static void AdjuTime() {

		String keyUrl = "http://api.m.taobao.com/rest/api3.do?api=mtop.common.getTimestamp";
		String rLine;	
		
		  while(keepGetTime){
			  try{
				  //��ʼ��������
				  URL url = new URL(keyUrl);
				  HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				  //���ó�ʱʱ��
				  urlConnection.setConnectTimeout(3000);
				  urlConnection.setReadTimeout(3000);
				  InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream(),"utf-8");
				  BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				  //��ȡ��ҳ����
				  while ((rLine = bufferedReader.readLine()) != null){
					   //�ж�Ŀ��ڵ��Ƿ����
					   if(rLine.contains("SUCCESS")){

						    String[] temp = rLine.split("\"");
	//					    System.out.println(temp[17]);
						   
						    //��¼ʱ��ֻ����ʱС��ʱ�����������ϵͳʱ��
						    oldTimeStamp = newTimeStamp;
						    newTimeStamp = temp[17];

						    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(temp[17])), ZoneId.systemDefault());

						    final LocalDate nowDate= localDateTime.toLocalDate();
						    final LocalTime nowTime= localDateTime.toLocalTime();
						    
						    //swt�ķ�UI���̲���UI����ʱ�ı�׼д��				   
					        Display.getDefault().asyncExec(new Runnable() {
			                 public void run() {
			                     //�Կؼ��Ĳ�������
			                	 RedressPCTime.AppendText(nowTime.toString());
			                	
			                	 if(justStart){
			                		 RedressPCTime.AppendText("׼���������ڣ� " + nowDate.toString());
			                	 }
			                 	}
					        });
					      
						    
						     //�޸�ϵͳʱ��
					        //����ʱ��ֵ�����120--240֮��Ÿ�ʱ��
					        if( ( Long.parseLong(newTimeStamp) - Long.parseLong(oldTimeStamp) > Integer.toUnsignedLong(sleepTime) ) 
					        		&& ( Long.parseLong(newTimeStamp) - Long.parseLong(oldTimeStamp) <Integer.toUnsignedLong(sleepTime + 120) ) ){
					        	
					        	sleepTime = 5000;
					        	
					        	try{
									/**
									 * ��ȡ����ϵͳ������
									 * */
									String name = System.getProperty("os.name").toLowerCase();
									if(name.contains("windows")){	// Window ����ϵͳ
										String cmd = "cmd.exe /c time " + nowTime.toString().substring(0, 8);
										 Runtime.getRuntime().exec(cmd); // �޸�ʱ��
										 if(justStart){
											 
											 cmd = "cmd.exe /c date " + nowDate.toString();
											 Runtime.getRuntime().exec(cmd); // �޸�����
											 justStart = false;
										 }
									}
	//								if(name.contains("linux")){
	//					                // Linux ϵͳ ��ʽ��yyyy-MM-dd HH:mm:ss   date -s "2017-11-11 11:11:11"
	//					                FileWriter excutefw = new FileWriter("/usr/updateSysTime.sh");
	//					                BufferedWriter excutebw=new BufferedWriter(excutefw);
	//					                excutebw.write("date -s \"" + nowDate +" "+ nowTime +"\"\r\n");
	//					                excutebw.close();
	//					                excutefw.close();
	//					                String cmd_date ="sh /usr/updateSysTime.sh";
	//					                String res = runAndResult(cmd_date);
	//					                System.out.println("cmd :" + cmd_date + " date :" + dataStr_ +" time :" + timeStr_);
	//					                System.out.println("linux ʱ���޸�" + res);
	//								}
									
								}catch(Exception e){
									e.printStackTrace();
								}
					        } else{
					        	sleepTime = setsleepTime;
					        }
					   }
				  }
				  
				  //�رչܵ���Դ�����ú�Ī���������ܸ�û�ر��йأ�ǰ�����������Ѿ�settimeout
				  if(bufferedReader !=null){bufferedReader.close();}
				  if(inputStreamReader != null){inputStreamReader.close();}
				  urlConnection.disconnect();
				  
			  } catch(UnknownHostException uhe){

	             Display.getDefault().asyncExec(new Runnable() {
	                 public void run() {
	                     //�Կؼ��Ĳ�������
	                	 RedressPCTime.AppendText("�޷����ӶԷ�������");
	                 }
	             });
	  
				  uhe.printStackTrace();
			  } catch(java.net.SocketException se){

		             Display.getDefault().asyncExec(new Runnable() {
		                 public void run() {
		                     //�Կؼ��Ĳ�������
		                	 RedressPCTime.AppendText("�������");
		                 }
		             });

		   		  se.printStackTrace();
			  } catch(java.net.SocketTimeoutException ste){
				  
				  Display.getDefault().asyncExec(new Runnable() {
		                 public void run() {
		                     //�Կؼ��Ĳ�������
		                	 RedressPCTime.AppendText("���ӳ�ʱ��");
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



