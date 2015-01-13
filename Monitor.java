import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Monitor {

	public static void main(String[] args) {
		System.out.println("Monitor Program starting.");

        final int MAX_THREADS = 20;
        int threadCount = 0;
        MonitorThread[] threadArray = new MonitorThread[MAX_THREADS];
		
		// Construct a ServerSocket on Port 8000.
        ServerSocket serverSocket = null;
        try { serverSocket = new ServerSocket(8000); }
        catch (IOException ex) { 
        	System.out.println("Failed to construct port 8000 server socket.");
        	try { serverSocket.close(); } catch(IOException ex2) {}
        	return;
        }
		
		while (true) {
			// Wait for a client system to make a connection request.
			System.out.println("Monitor waiting on ServerSocket Port 8000.");
			Socket clientSocket;
			try { clientSocket = serverSocket.accept(); }
			catch (IOException ex) {
	        	System.out.println("Server Socket failed on accept operation.");
	        	return;
			}

			// Pass this connection off to another thread to act as its monitor.
			// This new thread, once started will begin executing in its run() method.
			System.out.println("Connection accepted.  Creating and start a MonitorThread.");
			threadArray[threadCount] = new MonitorThread(clientSocket, threadCount+1);
			System.out.println("Starting thread " + (threadCount + 1) );
			threadArray[threadCount].start();

			// Set up for a new MonitorThread.
			++threadCount;
		}
	}
}
