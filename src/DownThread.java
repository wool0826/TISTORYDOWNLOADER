import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Semaphore;

public class DownThread extends Thread{
	private String dest;
	private String link;
	private Boolean done;
	
	public final static Semaphore sema = new Semaphore(4);
	
	public DownThread(String dest, String link){
		this.dest = dest;
		this.link = link;
		done = false;
	}
	
	public void run(){
		
		try {
			sema.acquire();
			
			done = false;
			
			saveImage(dest, link);
			
			sema.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void saveImage(String dest, String link) {
		try {
			URL url = new URL(link);

			InputStream is = url.openStream();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest));

			byte[] b = new byte[4096];
			int length;
			while ((length = is.read(b)) != -1) {
				bos.write(b, 0, length);
			}

			is.close();
			bos.close();
			
			done = true;
		} catch (Exception e) {
			e.printStackTrace();
			done = false;
		}
	}
	
	public Boolean getDone(){
		return done;
	}
	
	public String getLink(){
		return link;
	}
	
	public String getDest(){
		return dest;
	}
}
