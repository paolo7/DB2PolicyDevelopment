import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class RoomSensor extends TimerTask{

	public int x;
	public int y;
	public Room r;
	public int fireRate;
	public String ID;
	public double errorMargin;
	
	public RoomSensor(int x, int y, Room r, int fireRate, double errorMargin, int ID) {
		this.x = x;
		this.y = y;
		this.r = r;
		this.fireRate = fireRate;
		this.ID = "rs"+ID+r.ID;
		this.errorMargin = errorMargin;
	}
	
	public void run() {
		double error = -errorMargin+ThreadLocalRandom.current().nextDouble(0,errorMargin*2);
		StartSimulator.db.insertStatement("INSERT INTO CO2concentration "
				+ "(observationID, sensorID, simpleResult, time, location) "
				+ "VALUES (unhex(replace(uuid(),'-','')), '"+ID+"', '"+(r.CO2+error)+"', NOW(3),"
				+ "ST_GeomFromText('POINT("+((double)x)/100+" "+((double)y)/100+")') )");
	}
	
}
