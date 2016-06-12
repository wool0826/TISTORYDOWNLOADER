import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	String HomeDirectory = System.getProperty("user.dir").toString() + "\\MyPictures\\";
	JFrame frame = new JFrame("Tistory Original Image Downloader ver 1.1");

	JTextPane details = new JTextPane();
	JScrollPane scrollPane;
	// Pane�� goLink�� link�� ��� �ϳ��� ������Ʈ�� ���
	JPanel pane = new JPanel();
	JButton golink = new JButton("GO");
	JTextField linkField = new JTextField();

	// JTextPane���� Style�� ������ �ֱ� ���� ������Ʈ
	StyledDocument doc = details.getStyledDocument();
	SimpleAttributeSet errorAttr = new SimpleAttributeSet();
	SimpleAttributeSet passAttr = new SimpleAttributeSet();

	// UI�� ����ϰ� �ϱ� ���� ������Ʈ
	private final Component horizontalStrut = Box.createHorizontalStrut(15);
	private final Component verticalStrut = Box.createVerticalStrut(20);

	// ������ �ٿ�ε� ����� ���� ��ũ�� �����̸� ���� ������ �Ҵ�
	private ArrayList<String> destList = new ArrayList<>();
	private ArrayList<String> linkList = new ArrayList<>();

	public void createFrame() {

		// ������ ����
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setSize(640, 480);
		frame.setMinimumSize(new Dimension(350, 300));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// JTextPane ��Ÿ�� ����
		StyleConstants.setForeground(errorAttr, Color.decode("#800517"));
		StyleConstants.setBold(errorAttr, true);

		StyleConstants.setForeground(passAttr, Color.decode("#0000A0"));
		StyleConstants.setBold(passAttr, true);

		// �� ������Ʈ ����

		// go ��ư�� ������ ��
		golink.addActionListener(new GoActionListener());

		// ��Ʈ�� ���� ���ο� ��ü ����
		GhostText hint = new GhostText(this.linkField, "�̰��� ��ũ�� �Է����ּ���.");

		// JTextPane ����
		// details.setBorder(new LineBorder(Color.LIGHT_GRAY));
		details.setEditable(false);
		details.setFont(new Font("Dialog", Font.PLAIN, 12));
		details.setText("������ ����� ���: " + HomeDirectory + "\n\n");

		scrollPane = new JScrollPane(details);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(new LineBorder(Color.LIGHT_GRAY));
		scrollPane.setRowHeaderView(horizontalStrut);

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 15));
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		verticalStrut.setPreferredSize(new Dimension(0, 5));
		verticalStrut.setMinimumSize(new Dimension(0, 15));

		panel.add(verticalStrut);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		linkField.setPreferredSize(new Dimension(250, 29));
		pane.add(linkField);
		pane.add(golink);
		frame.getContentPane().add(pane, BorderLayout.NORTH);

		frame.setVisible(true);

		// ���α׷� ���� �� ��Ʈ(GhostText)�� �����ֱ� ���ؼ� JTextPane�� Focus�� ��.
		details.requestFocus();
	}

	public void result_print(String link, String dest, String type) {
		SimpleAttributeSet attr = null;

		switch (type) {
		case "ERROR":
			attr = errorAttr;
		case "PASS":
			attr = passAttr;
		}

		try {
			doc.insertString(doc.getLength(), "LINK: " + link + "\n", attr);
			doc.insertString(doc.getLength(), "FILE_NAME: " + dest + " \nSTATUS: " + type + "\n\n", attr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Downloader down = new Downloader();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		down.createFrame();
	}

	class GoActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			destList.clear();
			linkList.clear();

			// MyPictures ������ �ִ� �� ���� �� ��ư�� ���� �� ���� �˻���.
			// �������� ���� ��� ���� ����
			File f = new File(HomeDirectory);

			if (!f.exists())
				f.mkdirs();

			// ��ư�� ���� �� ������ ���� �� �ִ� ������ ���� ���ؼ� Enabled�� false�� ����.
			golink.setEnabled(false);

			int done = 0;
			int error = 0;
			int pass = 0;

			String getLink = linkField.getText();

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

				// ���� ������ �����ϱ� ���ؼ� title�� ����. ���� �� ���� �ʴ� Ư�����ڵ��� �����ְ� ������ ������ ��
				// ĭ�� trim���� ���� ��.
				String title = document.title().toString();
				title = title.replaceAll("[\\/:*<>|?\"]", "");
				title = title.trim();

				String dest = HomeDirectory + title + "\\";

				// ������ ����� �� ���� ���� ������ �ִ� �� Ȯ�� �� ������ ����.
				f = new File(dest);

				if (!f.exists())
					f.mkdirs();

				// img �±װ� �پ��ִ� �ڵ�鿡�� src�� / ������ ������.
				for (Element element : elements) {
					String[] arr = element.attr("src").toString().split("/");

					// tistory���� ������ �ø� ������ cfile00.tistory.com~~~~ �� ���� ��������
					// �ö󰡹Ƿ� cfile�� �����ϴ� ��ũ���� Ȯ��
					if (arr[2].contains("cfile")) {

						// �����ϴ� �������� �̸��� ���ؼ� Ȯ��.
						f = new File(dest + element.attr("filename").toString());

						if (!f.exists()) {
							linkList.add(arr[0] + "//" + arr[2] + "/original/" + arr[4]);
							destList.add(dest + element.attr("filename").toString());
						} else {
							pass++;
							result_print(arr[0] + "//" + arr[2] + "/original/" + arr[4],
									dest + element.attr("filename").toString(), "PASS");
						}
					}
				}
			} catch (IOException err) {
				err.printStackTrace();
			}

			// process downloading.
			int size = destList.size();

			DownThread t[] = new DownThread[size];

			for (int i = 0; i < size; i++) {
				t[i] = new DownThread(destList.get(i), linkList.get(i));
			}

			for (int i = 0; i < size; i++) {
				try {
					t[i].start();
					t[i].join();
				} catch (InterruptedException errr) {
					errr.printStackTrace();
				}
			}

			for (int i = 0; i < size; i++) {
				if (t[i].getDone()) {
					done++;
					result_print(t[i].getLink(), t[i].getDest(), "DONE");
				} else {
					error++;
					result_print(t[i].getLink(), t[i].getDest(), "ERROR");
				}
			}

			try {
				doc.insertString(doc.getLength(), "�ٿ�ε� ��: " + done + " ���� �߻�: " + error + " �����ϴ� ����: " + pass
						+ " ó����: " + (done + error + pass) + "\n\n", null);
			} catch (Exception ex) {
			}

			details.setCaretPosition(details.getDocument().getLength());
			
			golink.setEnabled(true);
		}

	}
}