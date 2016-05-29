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
	
	// ScrollPane�� JTextPane�� �־ �ϳ��� ������Ʈó�� ���.
	JTextPane details = new JTextPane();
	JScrollPane scr = new JScrollPane();
	
	// Pane�� goLink�� link�� ��� �ϳ��� ������Ʈ�� ���
	JPanel pane = new JPanel();
	JButton golink = new JButton("GO");
	JTextField link = new JTextField(71);
	
	// JTextPane���� Style�� ������ �ֱ� ���� ������Ʈ
	StyledDocument doc = details.getStyledDocument();
	SimpleAttributeSet err = new SimpleAttributeSet();
	SimpleAttributeSet pas = new SimpleAttributeSet();
	
	// UI�� ����ϰ� �ϱ� ���� ������Ʈ
	private final Component horizontalStrut = Box.createHorizontalStrut(15);
	private final Component verticalStrut = Box.createVerticalStrut(20);

	public void createFrame() {
		
		// ������ ����
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setSize(880, 500);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// JTextPane ��Ÿ�� ����
		StyleConstants.setForeground(err, Color.decode("#800517"));
		StyleConstants.setBold(err, true);

		StyleConstants.setForeground(pas, Color.decode("#0000A0"));
		StyleConstants.setBold(pas, true);
		
		
		// �� ������Ʈ ����
		
		// go ��ư�� ������ ��
		golink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// MyPictures ������ �ִ� �� ���� �� ��ư�� ���� �� ���� �˻���.
				// �������� ���� ��� ���� ����
				File f = new File(HomeDir);
				if (!f.exists())
					f.mkdirs();

				
				// ��ư�� ���� �� ������ ���� �� �ִ� ������ ���� ���ؼ� Enabled�� false�� ����.
				golink.setEnabled(false);

				
				// ��� ó���Ͽ����� ����� ���ؼ� ����.
				int down = 0;
				int pass = 0;
				int error = 0;

				String getLink = link.getText();

				// link�� �����ִ� ���ڿ��� http:// �� ���۵Ǵ� URL �������� �Ǵ�
				// 1. ������ �´ٸ� ����
				// 2. ������ ���� �ʴٸ� �������� �ʰ� ���¸� ó������ ����.
				Pattern p = Pattern.compile(
						"^(https?):\\/\\/([^:\\/\\s]+)(:([^\\/]*))?((\\/[^\\s/\\/]+)*)?\\/([^#\\s\\?]*)(\\?([^#\\s]*))?(#(\\w*))?$");
				Matcher m = p.matcher(getLink);

				if (!m.matches())
					getLink = "";

				if (getLink.equals("")) {
					golink.setEnabled(true);
					return;
				}
				
				
				// Jsoup�� �̿��ؼ� link�� ����.
				Connection con = Jsoup.connect(getLink);
				con.timeout(15 * 1000);

				try {
					
					Document document = con.get();

					// div id=content�� element�� ã�Ƴ�.
					Element content = document.getElementById("content");

					// �ڵ忡 ���� content�� �ƴ϶� contents�� �� ����.
					if (content == null)
						content = document.getElementById("contents");
					
					// �� �� ���� ���� document�� ����.
					if (content == null)
						content = con.get();

					
					// ������ �ɷ���(content) �ڷ� ������ img �±׸� �˻�.
					Elements elements = content.getElementsByTag("img");

					
					// ���� ������ �����ϱ� ���ؼ� title�� ����. ���� �� ���� �ʴ� Ư�����ڵ��� �����ְ� ������ ������ �� ĭ�� trim���� ���� ��.
					String title = document.title().toString();
					title = title.replaceAll("[\\/:*<>|?\"]", "");
					title = title.trim();

					String dest = HomeDir + title + "\\";

					// ������ ����� �� ���� ���� ������ �ִ� �� Ȯ�� �� ������ ����.
					f = new File(dest);
					if (!f.exists())
						f.mkdirs();

					// img �±װ� �پ��ִ� �ڵ�鿡�� src�� / ������ ������.
					for (Element el : elements) {
						String[] arr = el.attr("src").toString().split("/");

						// tistory���� ������ �ø� ������ cfile00.tistory.com~~~~ �� ���� �������� �ö󰡹Ƿ� cfile�� �����ϴ� ��ũ���� Ȯ��
						if (arr[2].contains("cfile")) {
							
							// �����ϴ� �������� �̸��� ���ؼ� Ȯ��.
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
										doc.insertString(doc.getLength(), "STATUS: �ٿ�ε��\n\n", null);
									} catch (Exception ex) {}
								} else {
									error++;
									try {
										doc.insertString(doc.getLength(),
												"LINK: " + arr[0] + "//" + arr[2] + "/original/" + arr[4] + "\n", err);
										doc.insertString(doc.getLength(),
												"FILE_NAME: " + el.attr("filename").toString() + " \n", err);
										doc.insertString(doc.getLength(), "STATUS: �ٿ�ε� ����\n\n", err);
									} catch (Exception ex) {}
								}

							} else {
								pass++;
								try {
									doc.insertString(doc.getLength(),
											"LINK: " + arr[0] + "//" + arr[2] + "/original/" + arr[4] + "\n", pas);
									doc.insertString(doc.getLength(),
											"FILE_NAME: " + el.attr("filename").toString() + " \n", pas);
									doc.insertString(doc.getLength(), "STATUS: �����ϴ� ����\n\n", pas);
								} catch (Exception ex) {}
							}
						}
					}
				} catch (IOException err) {
					err.printStackTrace();
				}

				try {
					doc.insertString(doc.getLength(), "�ٿ�ε� ��: " + down + " ���� �߻�: " + error + " �����ϴ� ����: " + pass
							+ " ó����: " + (down + error + pass) + "\n\n", null);
				} catch (Exception ex) {
				}

				golink.setEnabled(true);
			}
		});
		
		// ��Ʈ�� ���� ���ο� ��ü ����
		GhostText hint = new GhostText(this.link, "�̰��� ��ũ�� �Է����ּ���.");
		
		
		// JTextPane ����
		details.setBorder(new LineBorder(Color.LIGHT_GRAY));
		details.setEditable(false);
		details.setFont(new Font("Dialog", Font.PLAIN, 12));
		details.setText("������ ����� ���: " + HomeDir);
				
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

		// ���α׷� ���� �� ��Ʈ(GhostText)�� �����ֱ� ���ؼ� JTextPane�� Focus�� ��.
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