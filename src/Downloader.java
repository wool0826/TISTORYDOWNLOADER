import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {
	String HomeDir = System.getProperty("user.dir").toString() + "\\MyPictures\\";
	JFrame frame = new JFrame("Tistory Original Image Downloader by wool0826");
	JTextField link = new JTextField(71);
	JTextArea details = new JTextArea(23, 75);
	JButton golink = new JButton("GO");
	JScrollPane scr = new JScrollPane();

	public void createFrame() {
		frame.setLayout(new FlowLayout());
		frame.setSize(880, 500);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		golink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				File f = new File(HomeDir);
				if (!f.exists()) f.mkdirs();
				
				golink.setEnabled(false);
				
				int down = 0;
				int pass = 0;
				int error = 0;
				
				String getLink = link.getText();

				Connection con = Jsoup.connect(getLink);
				con.timeout(15 * 1000);
				
				try {
					Document document = con.get();
					
					Element content = document.getElementById("content");
					
					if(content == null) content = document.getElementById("contents");
					if(content == null) content = con.get();
										
					Elements elements = content.getElementsByTag("img");
					
					String title = document.title().toString();
					title = title.replaceAll("[\\/:*<>|?\"]", "");
					title = title.trim();
					
					String dest = HomeDir + title + "\\";
					
					f = new File(dest);
					if(!f.exists()) f.mkdirs();				

					for (Element el : elements) {
						String[] arr = el.attr("src").toString().split("/");

						if (arr[2].contains("cfile")) {
							f = new File(dest + el.attr("filename").toString());

							if (!f.exists()) {
								details.append("LINK: " + arr[0] + "//" + arr[2] + "/original/" + arr[4] + "\n");
								details.append("FILE_NAME: " + el.attr("filename").toString() + " \n");
								

								int status = saveImage(dest + el.attr("filename").toString(),arr[0] + "//" + arr[2] + "/original/" + arr[4]);
								
								if(status == 0){
									down++;
									details.append("STATUS: 다운로드됨\n\n");
								} else {
									error++;
									details.append("STATUS: 다운로드 오류\n\n");
								}
								
							} else {
								details.append("LINK: " + arr[0] + "//" + arr[2] + "/original/" + arr[4] + "\n");
								details.append("FILE_NAME: " + el.attr("filename").toString() + " \n");
								details.append("STATUS: 존재하는 파일\n\n");

								pass++;
							}
						}
					}
				} catch (IOException err) {
					err.printStackTrace();
				}

				details.append("다운로드 됨: " + down + " 오류 발생: " + error + " 존재하는 파일: " + pass + " 처리됨: " + (down + error + pass) + "\n\n");
				golink.setEnabled(true);
			}
		});
		GhostText hint = new GhostText(this.link, "이곳에 링크를 입력해주세요.");

		JScrollPane scrollPane = new JScrollPane(details);
		scrollPane.setBounds(10, 60, 780, 500);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		
		details.setEditable(false);
		details.setText("사진이 저장될 경로: " + HomeDir + "\n\n");

		frame.add(link);
		frame.add(golink);
		frame.add(scrollPane);

		frame.setVisible(true);

		details.requestFocus();
	}

	public static void main(String[] args) {
		Downloader down = new Downloader();

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