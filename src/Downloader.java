import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {
	String HomeDir = System.getProperty("user.dir").toString() + "\\MyPictures\\";
	JFrame frame = new JFrame("Tistory Original Image Downloader by wool0826 ver 1.1");
	
	// ScrollPane에 JTextPane을 넣어서 하나의 컴포넌트처럼 취급.
	JTextPane details = new JTextPane();
	JScrollPane scr = new JScrollPane();
	
	// Pane에 goLink와 link를 묶어서 하나의 컴포넌트로 취급
	JPanel pane = new JPanel();
	JButton golink = new JButton("GO");
	JTextField link = new JTextField(71);
	
	// JTextPane에서 Style을 설정해 주기 위한 컴포넌트
	StyledDocument doc = details.getStyledDocument();
	SimpleAttributeSet err = new SimpleAttributeSet();
	SimpleAttributeSet pas = new SimpleAttributeSet();
	
	// UI를 깔끔하게 하기 위한 컴포넌트
	private final Component horizontalStrut = Box.createHorizontalStrut(15);
	private final Component verticalStrut = Box.createVerticalStrut(20);

	public void createFrame() {
		
		// 프레임 설정
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setSize(880, 500);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// JTextPane 스타일 설정
		StyleConstants.setForeground(err, Color.decode("#800517"));
		StyleConstants.setBold(err, true);

		StyleConstants.setForeground(pas, Color.decode("#0000A0"));
		StyleConstants.setBold(pas, true);
		
		
		// 각 컴포넌트 설정
		
		// go 버튼을 눌렀을 시
		golink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// MyPictures 폴더가 있는 지 없는 지 버튼을 누를 때 마다 검사함.
				// 존재하지 않을 경우 폴더 생성
				File f = new File(HomeDir);
				if (!f.exists())
					f.mkdirs();

				
				// 버튼을 여러 번 눌러서 생길 수 있는 오류를 막기 위해서 Enabled를 false로 설정.
				golink.setEnabled(false);

				
				// 어떻게 처리하였는지 결과를 위해서 선언.
				int down = 0;
				int pass = 0;
				int error = 0;

				String getLink = link.getText();

				// link에 적혀있는 문자열이 http:// 로 시작되는 URL 형식인지 판단
				// 1. 형식이 맞다면 접속
				// 2. 형식이 맞지 않다면 접속하지 않고 상태를 처음으로 복원.
				Pattern p = Pattern.compile(
						"^(https?):\\/\\/([^:\\/\\s]+)(:([^\\/]*))?((\\/[^\\s/\\/]+)*)?\\/([^#\\s\\?]*)(\\?([^#\\s]*))?(#(\\w*))?$");
				Matcher m = p.matcher(getLink);

				if (!m.matches())
					getLink = "";

				if (getLink.equals("")) {
					golink.setEnabled(true);
					return;
				}
				
				
				// Jsoup을 이용해서 link에 접속.
				Connection con = Jsoup.connect(getLink);
				con.timeout(15 * 1000);

				try {
					
					Document document = con.get();

					// div id=content인 element를 찾아냄.
					Element content = document.getElementById("content");

					// 코드에 따라 content가 아니라 contents일 수 있음.
					if (content == null)
						content = document.getElementById("contents");
					
					// 둘 다 없을 때는 document로 진행.
					if (content == null)
						content = con.get();

					
					// 위에서 걸러낸(content) 자료 내에서 img 태그를 검색.
					Elements elements = content.getElementsByTag("img");

					
					// 폴더 명으로 지정하기 위해서 title을 추출. 폴더 명에 맞지 않는 특수문자들은 지워주고 좌측과 우측에 빈 칸을 trim으로 지워 줌.
					String title = document.title().toString();
					title = title.replaceAll("[\\/:*<>|?\"]", "");
					title = title.trim();

					String dest = HomeDir + title + "\\";

					// 위에서 만들어 낸 폴더 명인 폴더가 있는 지 확인 후 없으면 생성.
					f = new File(dest);
					if (!f.exists())
						f.mkdirs();

					// img 태그가 붙어있는 코드들에서 src를 / 단위로 분해함.
					for (Element el : elements) {
						String[] arr = el.attr("src").toString().split("/");

						// tistory에서 유저가 올린 파일은 cfile00.tistory.com~~~~ 와 같은 형식으로 올라가므로 cfile이 존재하는 링크인지 확인
						if (arr[2].contains("cfile")) {
							
							// 존재하는 파일인지 이름을 통해서 확인.
							f = new File(dest + el.attr("filename").toString());

							if (!f.exists()) {

								int status = saveImage(dest + el.attr("filename").toString(),
										arr[0] + "//" + arr[2] + "/original/" + arr[4]);

								if (status == 0) {
									down++;
									try {
										doc.insertString(doc.getLength(),
												"LINK: " + arr[0] + "//" + arr[2] + "/original/" + arr[4] + "\n",  null);
										doc.insertString(doc.getLength(),
												"FILE_NAME: " + el.attr("filename").toString() + " \n", null);
										doc.insertString(doc.getLength(), "STATUS: 다운로드됨\n\n", null);
									} catch (Exception ex) {}
								} else {
									error++;
									try {
										doc.insertString(doc.getLength(),
												"LINK: " + arr[0] + "//" + arr[2] + "/original/" + arr[4] + "\n", err);
										doc.insertString(doc.getLength(),
												"FILE_NAME: " + el.attr("filename").toString() + " \n", err);
										doc.insertString(doc.getLength(), "STATUS: 다운로드 오류\n\n", err);
									} catch (Exception ex) {}
								}

							} else {
								pass++;
								try {
									doc.insertString(doc.getLength(),
											"LINK: " + arr[0] + "//" + arr[2] + "/original/" + arr[4] + "\n", pas);
									doc.insertString(doc.getLength(),
											"FILE_NAME: " + el.attr("filename").toString() + " \n", pas);
									doc.insertString(doc.getLength(), "STATUS: 존재하는 파일\n\n", pas);
								} catch (Exception ex) {}
							}
						}
					}
				} catch (IOException err) {
					err.printStackTrace();
				}

				try {
					doc.insertString(doc.getLength(), "다운로드 됨: " + down + " 오류 발생: " + error + " 존재하는 파일: " + pass
							+ " 처리됨: " + (down + error + pass) + "\n\n", null);
				} catch (Exception ex) {
				}

				golink.setEnabled(true);
			}
		});
		
		// 힌트를 위해 새로운 객체 생성
		GhostText hint = new GhostText(this.link, "이곳에 링크를 입력해주세요.");
		
		
		// JTextPane 설정
		details.setBorder(new LineBorder(Color.LIGHT_GRAY));
		details.setEditable(false);
		details.setFont(new Font("Dialog", Font.PLAIN, 12));
		details.setText("사진이 저장될 경로: " + HomeDir);
				
		JScrollPane scrollPane = new JScrollPane(details);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(null);
		scrollPane.setRowHeaderView(horizontalStrut);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// TODO Auto-generated method stub
				e.getAdjustable().setValue(e.getAdjustable().getMaximum());
			}			
		});
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 15));
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		verticalStrut.setPreferredSize(new Dimension(0, 5));
		verticalStrut.setMinimumSize(new Dimension(0, 15));
		
		panel.add(verticalStrut);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		pane.add(link);
		pane.add(golink);
		frame.getContentPane().add(pane, BorderLayout.NORTH);
		
		frame.setVisible(true);

		// 프로그램 시작 시 힌트(GhostText)를 보여주기 위해서 JTextPane에 Focus를 줌.
		details.requestFocus();
	}
	public static void main(String[] args) {
		Downloader down = new Downloader();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}

		down.createFrame();
	}

	public static int saveImage(String dest, String link) {
		try {
			URL url = new URL(link);

			InputStream is = url.openStream();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest));

			byte[] b = new byte[2048];
			int length;
			while ((length = is.read(b)) != -1) {
				bos.write(b, 0, length);
			}

			is.close();
			bos.close();

			return 0;
		} catch (Exception e) {
			e.printStackTrace();

			return -1;
		}
	}
}