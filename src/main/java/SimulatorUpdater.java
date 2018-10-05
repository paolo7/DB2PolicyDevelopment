import java.util.TimerTask;
import java.util.Date;

public class SimulatorUpdater extends TimerTask{
	public Date now; // to display current time

	// Add your task here
	@Override
	public void run() {
		now = new Date(); // initialize date
		System.out.println("Time: " + now); // Display current time
		for (Room r : StartSimulator.rooms) {
			r.updateParameters();
		}
		for (Person r : StartSimulator.people) {
			r.updateParameters();
		}
		for (PersonalSensor ps : StartSimulator.personalSensors) {
			ps.updateParameters();
		}
	}
	
	
}
