import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class RndServer {
	
	public static ArrayList<Byte> get_centroids(ArrayList<ArrayList<Float>> xi) throws NumberFormatException, IOException {
		
		KMeans k = new KMeans();
		
		float[][] centroids;
		
		centroids = k.getCentroid(xi);
		
		byte[] bytes = new byte[4];

		int pos = 0;
		
		ArrayList<Byte> m = new ArrayList<>();
		
		for(int p = 0; p < 2; p++) {
			
			for(int j = 0; j < 2; j++) {
					
				ByteBuffer.wrap(bytes).putFloat(centroids[p][j]);
			
					for(byte b: bytes) {
						
						m.add(pos,b);
						
						++pos;
					}
				
					bytes = new byte[4];
				}
		
			}
			
			
		return m;
	}
	
	public static byte[] packet_type(String s) {
		
		/*This function converts hex string to byte array[2] and returns it*/
		
		int len = s.length();
		
		byte[] data = new byte[len / 2];
		
		for (int i = 0; i < len; i += 2) {
		
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
		                             + Character.digit(s.charAt(i+1), 16));
		    }
		    
		return data;
		
	}
	 
	public static byte[] get_dack(byte[] receive, int seq) {
		
		/*This function constructs the dack packet with the header, payload 
		 * and returns it*/
		
		byte[] dack = new byte[3];
		
		byte[] prev = new byte[3];
		
		byte[] rx_seq = Arrays.copyOfRange(receive,1,3);
		
		if(receive[0] == 0 && rx_seq[1] == seq) {
			
		//byte[] data_size = Arrays.copyOfRange(receive,3,5);
		
			String type = "01";
			
			dack[0] = packet_type(type)[0];
		
			// getting the sequence number into the DACK packet ack field
			
			dack[1] = rx_seq[0];
			
			dack[2] = rx_seq[1];
			
			prev = dack;
		}	
		else if(receive[0] == 0 && rx_seq[1] != seq) {
			
			dack = prev; // sending the prev correctly received pkt_dack
			
		}
		
		else {
			
			dack = new byte[3];
		}
		
		return dack;
	}
	
	public static ArrayList<Float> get_data(byte[] receive) {
		
		/*This function converts byte data to float arraylist and returns it*/
		
		byte[] payload = Arrays.copyOfRange(receive,5, receive.length);
		
	     // server side to convert byte to float input at 4 bytes one time
	     
	     ArrayList<Float> x = new ArrayList<Float>();
	    
	     int pos = 0;
	    
	    int upper = 4;
	    
	    int lower = 0;
	   
	    while(upper<payload.length + 4) {
	    	
	    	byte[] temp= new byte[4];
	    	
	    	int i = 0;
	    	
	    	for(int k=lower;k<upper;k++) {
	    		
	    		temp[i]= payload[k];
	    
	    		i++;
	     }
	     upper = upper + 4;
	    
	     lower = lower + 4;
	     
	     x.add(ByteBuffer.wrap(temp).getFloat(pos));	// wraps the four bytes from the curr pos to float
	    
	    }
	    return x;
	
	}
	
	public static void try_cack(DatagramSocket ds, DatagramPacket DpReq, ArrayList<Byte> m) throws IOException {
		
		int pos = 0;
		
		byte[] clus = new byte[17];			
		
		clus[0] = packet_type("04")[0];
		
		while(pos < m.size()) {
		
			for(int d = 1; d < clus.length; d++) {
				
					clus[d] = m.get(pos);
			
					pos++;
			}
			
		}
		System.out.println("\nSending the clus packet");
		
			DatagramPacket DpClus = 
		            new DatagramPacket(clus,clus.length,DpReq.getAddress()
		            		,DpReq.getPort());
		
			
//		the timer program for the CACK packet 
		
		byte[] CACK = new byte[1];
		
		int count = 0;
		
		int timeout = 1000;
 	
		try {
 		
 		while(count<1) {	
 			
 			DatagramPacket DpCack = new DatagramPacket(CACK,CACK.length);

 			try {
 				
 				
 		
 				ds.setSoTimeout(timeout); 
 				

 				
 				ds.receive(DpCack);
 				
 				System.out.print("\nThe cack packet received is: ");
 				
 				System.out.print(Arrays.toString(CACK));
 				
 				count++;
 			}
// 		
 			catch(SocketTimeoutException ex) {
 			
 			// TimeoutException thrown and caught
 				
 				timeout *= 2;
 				
 				ds.send(DpClus);
 				
 			}
 			
 			finally {
 			
 				if(timeout == 4000)
 				
 					ds.close();
 			}
 		}
 	}	
 	catch(SocketException ex1) {
 		
 	// SocketException thrown and caught
 		
  	   	 System.out.println("Socket closed: Timeout occured" + ex1);
  	     	
 	}
	
	}
	
	public static DatagramPacket try_rack(DatagramSocket ds,  ArrayList<ArrayList<Float>> xi) throws IOException {
		
		byte[] REQ= new byte[1];
		
		int timeout = 3000;
		
		byte[] RACK = new byte[1];
		
		byte[] packet_type = packet_type("03");
		
		RACK[0] = packet_type[0];
		
		DatagramPacket DpReq = new DatagramPacket(REQ, REQ.length);
		
		ds.receive(DpReq);
		
		System.out.println("The last Dack packet has been sent and now expecting a Request Packet\n");
		
		System.out.print("The req packet recevied is : ");
		
		System.out.println(Arrays.toString(REQ));
		
		ds.setSoTimeout(timeout);
		
		
		//	 Sending the rack_packet to the server for computation
		
		if(DpReq.getLength() != 0) {
		
			DatagramPacket DpRack = 
			            new DatagramPacket(RACK,RACK.length,DpReq.getAddress()
			            		,DpReq.getPort());
			
			 ds.send(DpRack);
	
	 }
	
		System.out.println("\nNow sending the rack packet\n ");
		
	    
		System.out.println("Performing the k-means computation of the received data");
   
	get_centroids(xi); // get centroid
	
	return DpReq;
		
	}
	
	public static ArrayList<ArrayList<Float>> try_dack(DatagramSocket ds) throws IOException {
	
		/* This function receives the data vectors and sends dack appropriately*/
		
		ArrayList<ArrayList<Float>> xi = new  ArrayList<ArrayList<Float>>();
		
		xi.add(new ArrayList<Float>());
		
		xi.add(new ArrayList<Float>());
		
		byte[] receive = new byte[1024];
		
		System.out.println("Receiving the data from the client");
		
		DatagramPacket DpReceive = new DatagramPacket(receive, receive.length);
		
		ArrayList<Float> coords  = new  ArrayList<Float>();
		
		byte[] dack = new byte[3];
		
		int i = 0;
		
		int seq = 0; 	// server knows the client init sequence
		
		ds.receive(DpReceive);
		
		int iter = 1;
		

		
		while(i < iter) {	
			
			ds.receive(DpReceive);	// receive the data

			byte[] received_data = Arrays.copyOf(receive,DpReceive.getLength());	// copying the seq no and data size to byte arrays
			
			System.out.print("The "+i+" DATA Packet received is : ");
			
			System.out.println(Arrays.toString(received_data));
			
			dack = get_dack(receive,seq); // check dack
			
			seq+=1;	//  update sequence for sending next packet
				
			if(dack.length != 0)		// get the data if the dack is received correctly

				coords.addAll(get_data(received_data)); 
			
			// Store in 2D array
			
	    	for(int p = 0; p <(DpReceive.getLength()-5)/8; p++) {
				  
	    		xi.get(0).add(coords.get(p));
				
	    	}
	    	
	    	for(int p = (DpReceive.getLength()-5)/8; p <(DpReceive.getLength()-5)/8 * 2; p++) {
	    		
	    			xi.get(1).add(coords.get(p));
			    	
	    	}
				
			if(dack.length == 0)  	// close socket if totally wrong packet header
			
				ds.disconnect();
				
			DatagramPacket Dack = new DatagramPacket(dack,dack.length, 
						DpReceive.getAddress(),DpReceive.getPort());
				
			ds.send(Dack);	// one byte of packet type and 2 bytes of seqno in dack 
			
			
			iter = (int) Math.ceil(xi.get(0).size()/50); /* number of iterations for a file */
			
			iter++;
			
			i++;		// loop variable
			
			coords.clear();
	
		}
		System.out.println("\nThe number of 2D vectors received from the client are: " +xi.get(0).size());
		
		System.out.println("\nSending the Dack packet to the client\n");
		
		return xi;
		
	}
	
	
	
	
	
	
	public static void main (String[] args) throws Exception {
			
		DatagramSocket ds = new DatagramSocket(9991);
		 	
		// Data vector upload phase -> dack
		
		ArrayList<ArrayList<Float>> xi = try_dack(ds);
		
		// Computation request phase -> rack
			
		DatagramPacket DpReq = try_rack(ds, xi);
			
		// Result download phase -> cack
		
		try_cack(ds, DpReq, get_centroids(xi));
     
	} // end of main
	
}// end of class
