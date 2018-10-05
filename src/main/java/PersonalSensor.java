import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class PersonalSensor extends TimerTask{

	public int x;
	public int y;
	public Person p;
	public int fireRate;
	public String ID;
	public double errorMargin;
	
	public PersonalSensor(int x, int y, Person p, int fireRate, double errorMargin, int ID) {
		this.x = x;
		this.y = y;
		this.p = p;
		this.fireRate = fireRate;
		this.ID = "ps"+ID+p.ID;
		this.errorMargin = errorMargin;
	}
	
	public void updateParameters() {
		x = p.x;
		y = p.y;
	}
	
	@Override
	public void run() {
		double error1 = -errorMargin+ThreadLocalRandom.current().nextDouble(0,errorMargin*2);
		double error2 = -errorMargin+ThreadLocalRandom.current().nextDouble(0,errorMargin*2);
		int location1 = (int) Math.round(error1+x);
		int location2 = (int) Math.round(error2+y);
		String location = "["+location1+","+location2+"]";
		StartSimulator.db.insertStatement("INSERT INTO location "
				+ "(observationID, sensorID, simpleResult, time, location,featureOfInterest) "
				+ "VALUES (unhex(replace(uuid(),'-','')), '"+ID+"', "
				+ "'"+location+"', NOW(3),"
				+ "ST_GeomFromText('POINT("+((double)location1)/100+" "+((double)location2)/100+")'),'"+p.ID+"')");
	}
}
