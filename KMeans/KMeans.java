import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


public class KMeans {
	
	private static final String FILENAME = "N:\\GradStudy\\ENTS 640/data01.txt";	// file to upload
	
	static final double converge = Math.pow(10, -5); 	// convergence constant 
	
	
	
	public ArrayList<Float> getAverage(ArrayList<ArrayList<Float>> C){
		
		ArrayList<Float> m = new ArrayList<Float>();
		
		float sum_x = 0;
	
		float avg_x = 0;
		
		float sum_y = 0;
		
		float avg_y= 0;
		
		for(int i = 0; i< C.size()-1;i++) {
		
			sum_x += C.get(0).get(i);
			
		}
		
		avg_x = sum_x/C.get(0).size();
		
		for(int i = 0; i< C.size()-1;i++) {
			
			sum_y += C.get(1).get(i);
			
		}
		
		avg_y = sum_y/C.get(1).size();
		
		m.add(avg_x);
		
		m.add(avg_y);
		
		return m;
		
	}
	
	public ArrayList<Float> getInit(ArrayList<ArrayList<Float>> xi) {
	
		ArrayList<Float> m = new ArrayList<Float>();	
		
		/*This function calculates 
		the range between the max and 
		min value for each dimension and returns the random initial centroid*/
		
		Random rand = new Random();
		
		ArrayList<Float> x = xi.get(0);
		
		ArrayList<Float> y = xi.get(1);
		
		float max_1 = x.get(0);
        
		float min_1 = y.get(0);
        
		float range_x;
        
        for (float i : x) {
        
        	if (i > max_1) 
            
        		max_1 = i;
             
        	else if (i < min_1) 
            
        		min_1 = i;      
           }
        
        range_x = max_1 - min_1;
        
        float x1= rand.nextFloat()*range_x + min_1;
        
        float max_2 = x.get(1);
        
        float min_2 = y.get(1);
        
        float range_y;
        
        for (float i : y) {
        
        	if (i > max_2) 
            
        		max_2 = i;
             
        	else if (i < min_2) 
            
        		min_2 = i;    
           }
       
        range_y = max_2 - min_2;
        
        float y1 = rand.nextFloat()*range_y + min_2;
        
        m.add(x1);
        
        m.add(y1);
        
        return m;
        
	  }
	
	
	public float calcDistance(ArrayList<Float> m,  ArrayList<Float> x) {
		
		int pos = 0;
		
		float distance = 0;
		
		distance = (float) (Math.sqrt(Math.pow((m.get(0) - x.get(0)),2) + 
					Math.pow((m.get(1) - x.get(1)),2)));
		
		
		return distance;
	}
	
	public  ArrayList<ArrayList<Float>> toArray(BufferedReader br) throws NumberFormatException, IOException {
		ArrayList<ArrayList<Float>> xi = new ArrayList<>();
		
		ArrayList<Float> x = new ArrayList<Float>();
		ArrayList<Float> y = new ArrayList<Float>();
		String curr;
		
		while ((curr = br.readLine()) != null) {
			
				
				String xcoord = curr.split(",")[0];
		        float xxcoord =Float.parseFloat(xcoord.substring(1, xcoord.length()));
		      
		        x.add(xxcoord);
		        String ycoord = curr.split(",")[1];
		        float yycoord = Float.parseFloat(ycoord.substring(0, ycoord.length()-1));
		        y.add(yycoord);
		        
		  }
			// 2D array of x and y coordinates
			
			xi.add(x);
			xi.add(y);
			
		return xi;
		
	}
	
	public void getCentroid() throws NumberFormatException, IOException {
		
		ArrayList<ArrayList<Float>> xi = new  ArrayList<ArrayList<Float>>(); // holds the 2D array
	
		ArrayList<Float> m1 = new ArrayList<Float>();		// holds the centroid C1
		
		ArrayList<Float> m2 = new ArrayList<Float>();		// holds the centroid C2
		
		ArrayList<Float> m1_old = new ArrayList<Float>();		// holds the prev centroid C1
		
		ArrayList<Float> m2_old = new ArrayList<Float>();		// holds the prev centroid C2

	
		ArrayList<ArrayList<Float>> C1 = new  ArrayList<ArrayList<Float>>();
		
		ArrayList<ArrayList<Float>> C2 = new  ArrayList<ArrayList<Float>>();
		
				BufferedReader br = null;
		
				FileReader fi = null;
				
				
				fi = new FileReader(FILENAME);
				
				br = new BufferedReader(fi);
				
				// getting the coordinates as [x coord, y coord] into xi for easier access
		
				xi = toArray(br);
				
				// C1 centroid initialization
				
				m1_old = getInit(xi);
		       
		        
		        // C2 centroid initialization
				
				m2_old = getInit(xi);
				
				
				
				//Array lists for the vector coordinates
				ArrayList<Float> x = xi.get(0);	//	x(1)
				
				ArrayList<Float> y = xi.get(1);	// 	x(2)
				
				ArrayList<Float> d1 = new ArrayList<Float>();
						 
				ArrayList<Float> d2 = new ArrayList<Float>();
				
				ArrayList<Float> xy = new ArrayList<Float>();
				
				// creating empty array lists initially for each cluster 
				
				C1.add(new ArrayList<Float>());
				
				C2.add(new ArrayList<Float>());
				
				C1.add(new ArrayList<Float>());
				
				C2.add(new ArrayList<Float>());
				
				int pos = 0;
				
				float limit = 0;
			
			while(limit < converge) {
				
				while(pos< x.size()) {
					
					xy.add(x.get(pos));
		
					xy.add(y.get(pos));
					
					float distance_1 = calcDistance(m1_old,xy);  // distance from m1
					
					float distance_2 = calcDistance(m2_old,xy);  // distance from m2
					
					if(distance_1 <= distance_2) {
						
						C1.get(0).add(xy.get(0));
					
						C1.get(1).add(xy.get(1));
						
					}
					
					else {
						
						C2.get(0).add(xy.get(0));
				
						C2.get(1).add(xy.get(1));
						
					}
				
					xy.clear();
					
					pos++;
				
				}
				
					m1 = getAverage(C1);
				
					m2 = getAverage(C2);
					
					limit = calcDistance(m1_old, m1) + calcDistance(m2_old, m2);// convergence limit
					
					m1_old = m1;
					
					m2_old = m2;
					
					
			}
		
			//System.out.println("m1: "+ m1);
	
			//System.out.println("m2: "+ m2);
					
				}
				
					
}
				
				
				
	
	
	
	


			
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				/*catch (IOException e) {

					e.printStackTrace();

				} finally {

					try {

						if (br != null)
							br.close();

						if (fi != null)
							fi.close();

					} catch (IOException ex) {

						ex.printStackTrace();

					}
*/



