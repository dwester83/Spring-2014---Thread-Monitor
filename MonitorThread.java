
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MonitorThread extends Thread
{
	int threadID;
	Socket clientSocket;
	
	final char CONNECT = '1';
	final char UPDATE = '2';
	final char CREATE_THREADS = '3';
	final char RESUME = '4';
	final char KILL = '5';
	
	// MonitorThread Constructor.
	// Save the client socket.
	public MonitorThread(Socket cSocket, int newThreadID)
	{
		clientSocket = cSocket;
		threadID = newThreadID;
	}
	
	// Entry point for new MonitorThread.
	public void run()
	{
		System.out.println("MonitorThread is running for thread " + threadID);
		
		// Create output and input ports for this socket.
		OutputStream os;
		InputStream is;
		try {
			os = clientSocket.getOutputStream();
			is = clientSocket.getInputStream();
		}
		catch (IOException ex) {
			System.out.println("Failed to get input/output streams for thread " + threadID);
			return;
		}
		DataInputStream in = new DataInputStream( is );
		
		// Loop, telling the clients to construct up to 20 threads.
		for (int clientThread = 1; clientThread <= 20; clientThread++)
		{
			// Tell client to create a new worker thread.
			byte[] createCommand = new byte[] { (byte)CREATE_THREADS, (byte)'1', (byte)0 };
			try {
				os.write(createCommand);
				os.flush();
			}
			catch (IOException ex) { 
				System.out.println("Failed to send CREATE_THREADS for thread " + threadID);
				return;
			}
			
			// Sleep for a second before telling the workers to beginning measuring.
			try { Thread.sleep(1000); }	catch(InterruptedException e) {}
			
			// Tell client to beginning measuring transactions.
			byte[] resumeCommand = new byte[] { (byte)RESUME, (byte)0 };
			try {
				os.write(resumeCommand);
				os.flush();
			}
			catch (IOException ex) {
				System.out.println("Failed to send RESUME for thread " + threadID);
				return;
			}
			
			// Sleep for 10 seconds while the threads are measuring transactions.
			try { Thread.sleep(10 * 1000); }	catch(InterruptedException e) {}
			
			// Ask the client for the transactions counted in that 10 seconds.
			byte[] updateCommand = new byte[] { (byte)UPDATE, (byte)0 };
			try {
				os.write(updateCommand);
				os.flush();
			}
			catch (IOException ex) {
				System.out.println("Failed to send UPDATE for thread " + threadID );
				return;
			}
			
			// Wait on the response from the client.
			byte[] inputBuffer = new byte[128];
			int bytesRead = 0;
			try { 
				bytesRead = in.read(inputBuffer);
				System.out.println("Thread " + threadID + " received " + bytesRead + " bytes.");
			} 
			catch(IOException ex) {
				System.out.println("Thread " + threadID + " failed to receive.  Ending.");
				return;
			}
			
			// Output what we saw from the client.
			String messageString = new String(inputBuffer, 1, bytesRead-1);
			System.out.println("Thread " + threadID + ": Transaction Count with " + clientThread + " threads: " + messageString);
		}
		
		// We are done.  Tell the client to die.
		System.out.println("This client's measurements are done.  Ending.");
		byte[] killCommand = new byte[] { (byte)KILL, (byte)0 };
		try {
			os.write(killCommand);
			os.flush();
		}
		catch (IOException ex) { System.out.println("Failed to send KILL."); return;}
	
		// Sleep for a few seconds before closing the connection and ending.
		try { Thread.sleep(5 * 1000); }	catch(InterruptedException e) {}
		
		// Close the sockets connection and end.
		try { clientSocket.close(); } catch (IOException ex) {}
		try { Thread.sleep(1000); }	catch(InterruptedException e) {}
	}
}
