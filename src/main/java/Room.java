import java.util.concurrent.ThreadLocalRandom;

public class Room {

	public int x1;
	public int x2;
	public int y1;
	public int y2;
	
	public String ID;
	public double CO2;
	public double CO2var;
	
	public Room(int x1, int x2, int y1, int y2, int ID) {
		CO2 = 0;
		CO2var = 0;
		

		this.ID = "r"+ID;
		if (x1 >= x2) throw new RuntimeException("ERROR, attempted to create a room with wrong x coordinates: "+x1+" >= "+x2);
		if (y1 >= y2) throw new RuntimeException("ERROR, attempted to create a room with wrong y coordinates: "+y1+" >= "+y2);
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}
	
	public int getWidth() {
		return x2-x1;
	}
	public int getHeight() {
		return y2-y1;
	}
	public int getCentreX() {
		return x1+getWidth()/2;
	}
	public int getCentreY() {
		return y1+getHeight()/2;
	}
	
	public boolean intersects(Room r) {
		return !(r.x1 >= x2 || r.x2 <= x1 || r.y1 >= y2 || r.y2 <= y1 );
		//return (x1 <= r.x2 && x2 >= r.x1 && y1<= r.y2 && y2 >= r.y1);
	}
	
	public void updateParameters() {
		if (ThreadLocalRandom.current().nextDouble(0,1) > 0.95) {			
			CO2var = CO2var/2;
			}
		CO2var += -0.1+ThreadLocalRandom.current().nextDouble(0,0.2);
		if (CO2 < 10) CO2var += ThreadLocalRandom.current().nextDouble(0,0.01);
		if (CO2 > 50) CO2var += -ThreadLocalRandom.current().nextDouble(0,0.01);
		CO2 += CO2var;
		if (CO2 < 0) CO2 = 0;
		if (CO2 > 100) CO2 = 100;
		
		
	}
	
}
