import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.ByteBuffer;

public class Client {
	
	public static InetAddress try_ip() {
	    	
	    	InetAddress ip = null;
	    	
	    	try
			{
		 		// get ip address of server
		 		
				ip = InetAddress.getLocalHost();
				
				
			}
			
			catch(UnknownHostException ex)
			{
				// this really should not happen
				
				System.out.print("\nException: this really should not happen.");
			}
			
	    	return ip;
	    }
	
	public static ArrayList<Byte> toBytes(ArrayList<Float> x){
		
		/* This function converts each floating point values to four bytes and 
		 * returns a byte arraylist */
		
		ArrayList<Byte> xy = new ArrayList<Byte>();
		
		int pos = 0;
		
		byte[] bytes = new byte[4];
	    
		while(pos < x.size()) {
	    	
	    	ByteBuffer.wrap(bytes).putFloat(x.get(pos)); // takes each float and wraps to 4 bytes
	    	
	    	for(byte b : bytes) {
	    	
	    		xy.add(b);
	    	}
	    	
	    	pos++;
	    }
	    
		return xy;	
	}
	
	public static ArrayList<ArrayList<Float>> readFile(String FILENAME) throws NumberFormatException, IOException{
		
		/* This function takes in a string filename and convets to a 2D float arraylist */
		
		KMeans k = new KMeans(); // K Means object
		
		ArrayList<ArrayList<Float>> xi = new ArrayList<>();
		
		BufferedReader br = null;
		
		FileReader fi = null;
		
		fi = new FileReader(FILENAME);
		
		br = new BufferedReader(fi);
		
		xi = k.toArray(br); 		// from K Means class
		
		return xi;
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
 		
	public static byte[] seq_number(int seq) {
		
		/*This function converts integer seq number to byte array[2] and returns it*/
		
		byte b = (byte)seq;
		
		byte[] seq_no = new byte[2];
     	
		seq_no[1]=(byte) (b & 0xFF);
     	
		seq_no[0] = (byte) ((b >> 8) & 0xFF);
     	
		return seq_no;
	}
	
	public static byte[] data_size(int length) {
		
		/*This function converts data_vector_size to byte array[2] and returns it*/
		
		byte b = (byte)length;
		
		byte[] size = new byte[2];
     	
		size[1]=(byte) (b & 0xFF);
     	
		size[0] = (byte) ((b >> 8) & 0xFF);
     	
		return size;
	}
	
	public static byte[] payload(ArrayList<ArrayList<Byte>> byteArray, int lower, int upper, int pkt_size) {
		
		/*This function make a packet to send with the byte data in it*/
	    
		int i = 0;
	    
		int j = pkt_size*4;
	    
		byte[] pktBuf = new byte[pkt_size*8];
		
		for(byte b: byteArray.get(0).subList(lower, upper)) {
    	
			pktBuf[i] = b;
     		
			i++;
    	
		}
     	
		for(byte b: byteArray.get(1).subList(lower, upper)) {
	   	
			pktBuf[j] = b;
     		
			j++;
     	}
     	
		return pktBuf;
	}
	
	public static byte[] get_header(int seq_no, int pkt_size) {
		
		/*This function constructs the header for the particular seq no and pkt_size 
		 * and returns a byte array*/
		
		byte[] pktBuf = new byte[pkt_size*8 + 5];
	    
		byte[] seq = seq_number(seq_no);
	    
		byte[] packet_type = packet_type("00");
		
		pktBuf[0] = packet_type[0];
	    
		pktBuf[1] = seq[0];
	    
		pktBuf[2] = seq[1];
	    
		return pktBuf;
	}
	
 	public static ArrayList<Float> get_floats(byte[] receive) {
		
 	/*  This function converts the byte data  to floating point values*/
 		
 		byte[] payload = Arrays.copyOfRange(receive,1, receive.length);
		
	    ArrayList<Float> x = new ArrayList<Float>();
	    
	    int pos = 0;
	    
	    int upper = 4;
	    
	    int lower = 0;
	    
	    while(upper < payload.length + 4) {
	    
	    	byte[] temp = new byte[4];
	    	
	    	int i = 0;
	    	
	    	for(int k = lower; k < upper; k++) {
	    	
	    		temp[i] = payload[k];
	    		
	    		i++;
	     }
	     
	    	upper = upper + 4;
	     
	    	lower = lower + 4;
	     
	    	x.add(ByteBuffer.wrap(temp).getFloat(pos));	// wraps the four bytes from the curr pos to float
	    
	    }
	    
	    return x;
	
 	}
	
	public static ArrayList<ArrayList<Byte>> get_bytes(ArrayList<ArrayList<Float>> xi){
		
			/* This function puts the bytes into a 2D array */
		
	     ArrayList<ArrayList<Byte>> byteArray = new ArrayList<>();
	     
	     byteArray.add(0,toBytes(xi.get(0)));
	     
	     byteArray.add(1,toBytes(xi.get(1)));
	     
	     return byteArray;
	}
	
	public static byte[] generate_packet(ArrayList<ArrayList<Byte>> byteArray, int pkt_size, int upper, int lower, int seq) {
		
		/* This function generates the packet to be send as bytes of size 50 or less */
		
		byte[] pktBuf = new byte[pkt_size*8];
	    
		int p = 0;
		
		pktBuf = get_header(seq, pkt_size);

		byte[] pkt_payload = payload(byteArray, lower, upper,pkt_size);
	    
		byte[] data_size = data_size(pkt_size);
	    
		pktBuf[3] =  data_size[0];
	    
		pktBuf[4] =  data_size[1];
	    
		for(int i = 5; i < pktBuf.length; i++) {
	    
			pktBuf[i] = pkt_payload[p];
	    	
			p++;
	    	
		}	
		
		return pktBuf;
	}
	
	public static void try_data(DatagramSocket ds, InetAddress ip, ArrayList<ArrayList<Byte>> byteArray, int DATA_VECTOR_LENGTH) throws IOException {
		
		/* This function sends the data vectors and recieves dack appropriately*/
		
		int MAX_PKT_SIZE = 50;
	    
		int number_of_pkts = DATA_VECTOR_LENGTH/MAX_PKT_SIZE;
		
		int rem_pkts = DATA_VECTOR_LENGTH % MAX_PKT_SIZE;
		
		int iter = 0;
		
		int seq = 0;
		
		int lower = 0;
		
		int upper  = 0;
		
		byte[] pktBuf;		 
		
		// appending 100 x y to one packet(50 data vectors)
		
		while(iter <= number_of_pkts) {
		
		
			if(iter != number_of_pkts)	{
					 
			
				upper += MAX_PKT_SIZE * 4;
				
				pktBuf = generate_packet(byteArray, MAX_PKT_SIZE, upper, lower, seq);
				
				lower += MAX_PKT_SIZE * 4;
				
			}
			
			else {
					 
					 upper = DATA_VECTOR_LENGTH*4;
				 	
					 pktBuf = generate_packet(byteArray,  rem_pkts, upper, lower, seq);
				 }
				 
				 iter++;
				
				DatagramPacket DpSend = 
			            new DatagramPacket(pktBuf,pktBuf.length, ip, 9991);
		     	
				ds.send(DpSend);
				
		     	int timeout = 1000; // time in millisec 
		     	
		     	int count = 0;
		     	
		     	String dack_packet_type = "01";
		     	
		     	byte[] dack = new byte[3];

		     	//	the timer program for the intial data packets 
		     	
		     	try {
		     	
		     		while(count<1) {
		     		
		     			DatagramPacket DpReceive = new DatagramPacket(dack,dack.length);
		     			
		     			try {
		     			
		     				
		     				
		     				ds.setSoTimeout(timeout); 
		     				
		     				ds.receive(DpReceive);
		     				
		     				count++;
		     				
		     				System.out.print("The "+iter +" " +"DACK Packet Received is: ");
		     				
		     				System.out.println(Arrays.toString(dack));
		     			}
		     		
		     			catch(SocketTimeoutException ex) {
		     				
		     			// Timeout Exception thrown and caught
		     				
		     				timeout *= 2;
		     				
		     				ds.send(DpSend);
		     				
		     			}
		     			
		     			finally {
		     			
		     				if(timeout == 4000)
		     				
		     					ds.close();
		     			}
		     		}
		     	}
		     	
		     	catch(SocketException ex1) {
		     		
		     		//Socket Exception thrown and caught
		     		
		     		System.out.println("Communication Failure as Timeout occured  " );
			  	    
		     	}
		     	
		     	if(dack[0] == packet_type(dack_packet_type)[0] && dack[2] == seq) {
			    
		     		seq += 1;
			     	
			     	
		     	}	
		     	
		}
	
	}
	
    public static void try_req(DatagramSocket ds, InetAddress ip) throws IOException {
    	
    	/* This func requests for computation and receives rack appropriately*/
    	
    	 byte[] REQ = new byte[1];
		 
		 byte[] packet_type = packet_type("02");
		 
		 REQ[0] = packet_type[0]; 	
//	 
		 DatagramPacket DpReq = 
	            new DatagramPacket(REQ,REQ.length, ip, 9991);
		 
		
		 ds.send(DpReq);
		
		 System.out.println("\nSending the req packet");
		
		 int timeout = 1000; // millisececonds 
    	
		 int count = 0;

    	//	receiving the rack packet from server 
    	
   	
		 byte[] RACK = new byte[1];
    	
		 String RACK_packet_type = "03";
		 
//   		the timer program for the REQ packet 
    	
		 try {
    		
    		while(count<1) {
    			
    			DatagramPacket DpRACK = new DatagramPacket(RACK,RACK.length);
    		
    			try {
    				
    				ds.setSoTimeout(timeout); 
    				
       				ds.receive(DpRACK);
    				
    				count++;
    				
    			}
//    		
    			catch(SocketTimeoutException ex) {
    				
    				// Timeout Exception thrown and caught
    				
    				timeout *= 2;
    				
    				ds.send(DpReq);
    				
    			}
    			
    			finally {
    			
    				if(timeout == 4000)
    				
    					ds.close();
    			}
    		}
    	}
    	catch(SocketException ex1) {
    		
    		// Socket Exception thrown and caught
    		
	  	   	 System.out.println("Socket closed: Communication Failure");
	  	     	
    	}
    	
		 if(RACK[0] == packet_type(RACK_packet_type)[0]) {
	     			
			 System.out.println(" \nRack packet Received is valid");  
	     			
	     			
	     
		 }

    	System.out.println("The Rack Packet received is : "+ Arrays.toString(RACK));
		 
    }
		
    public static void try_clus(DatagramSocket ds) throws IOException {
    	
    	/* This func waits for the clus packet recives the bytes and calculates
    	 * the centroid using K Means class and sends cack appropriately*/
    	
    	byte[] receive_Clus = new byte[1000];
     	
     	int timeout = 30000;
     	
     	DatagramPacket DpClus = new DatagramPacket(receive_Clus,receive_Clus.length);
     	
     	try {
     	
     		ds.setSoTimeout(timeout);
     		
     		ds.receive(DpClus);
     
     	}
     	
     	catch(SocketException ex2) {
	  	
     		System.out.println("Socket closed: Communication Failure");
     	}
     	
     	

     	byte[] received_CLUS_data = Arrays.copyOf(receive_Clus,DpClus.getLength());
     	
     	System.out.println("\nthe received cluster data is : " + Arrays.toString(received_CLUS_data));
     	
     	//	Checking for validity and Sending the Cack_packet as an ack for clus
     	
     	byte[] send_Cack = new byte[1];
     	
     	if(received_CLUS_data[0] == packet_type("04")[0]) 
     	
     	{     	
     		send_Cack[0] = packet_type("05")[0];
     	
     		DatagramPacket DpCack = new DatagramPacket(send_Cack,send_Cack.length,DpClus.getAddress(),DpClus.getPort());
     	
     		ds.send(DpCack);
     
     	}
     	
     	// Convert to Floats 
     	
     	ArrayList<Float> centroid = new  ArrayList<Float>();
     	
     	ArrayList<Float> m1 = new ArrayList<Float>();
    	
    	ArrayList<Float> m2 = new ArrayList<Float>();
    	
     	centroid.addAll(get_floats(received_CLUS_data)); 
     	
     	System.out.println("\nCentroids:[m1(x,y) , m2(x,y)] are "+ Arrays.toString(centroid.toArray()));
     	
     	m1.add(centroid.get(0)); //x
     	
     	m1.add(centroid.get(1)); //y
     	
     	m2.add(centroid.get(2));
     	
     	m2.add(centroid.get(3));
     	
     	System.out.println("m1: " + Arrays.toString(m1.toArray()));	// Centroid for C1
     	
     	System.out.println("m2: " + Arrays.toString(m2.toArray())); // Centroid for C2
    	
    }
   
 
   
    public static void main(String[] args) throws NumberFormatException, IOException { 
   	
    	final String FILENAME = "C://Users//anude//Desktop//UMCP//1st Sem//ENTS 640//Project/data02.txt";	// file to read
		
    	
    	ArrayList<ArrayList<Byte>> byteArray = new ArrayList<>();
    	
    	DatagramSocket ds = new DatagramSocket();
		
		InetAddress ip = try_ip();	
			
		 	
		ArrayList<ArrayList<Float>> xi = readFile(FILENAME);
		    
			
		int DATA_VECTOR_LENGTH = xi.get(0).size();
			
			// getting the coordinates as [x coord, y coord] into xi[float] for easier access
		
		byteArray = get_bytes(xi);
	   
	     	// Data vector upload phase
			
	   	try_data(ds, ip, byteArray, DATA_VECTOR_LENGTH);
	     	
	     	// Computation request phase
		 
	   	try_req(ds, ip);
	     	
	     	// Result download phase 
	     	
    	try_clus(ds);
	     	
    }	// end of main

}	// end of class
