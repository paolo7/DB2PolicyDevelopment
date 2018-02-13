import java.util.concurrent.ThreadLocalRandom;

public class Person {

	Room destinationRoom;
	
	public static int stepsize = 10;
	
	public int x;
	public int y;
	
	public int destinationX;
	public int destinationY;
	
	public String ID;

	public Person(int x,int y, int ID) {
		destinationRoom = StartSimulator.rooms.get(ThreadLocalRandom.current().nextInt(StartSimulator.rooms.size()));
		this.x = x;
		this.y = y;
		this.ID = "p"+ID;
	}
	
	public void updateParameters() {
		if (ThreadLocalRandom.current().nextDouble(0,1) > 0.99) {			
			setDestinationRoom();
			}
		if (x < destinationX-stepsize) x += stepsize;
		if (x > destinationX+stepsize) x -= stepsize;
		if (y < destinationY-stepsize) y += stepsize;
		if (y > destinationY+stepsize) y -= stepsize;
	}
	
	public void setDestinationRoom() {
		destinationRoom = StartSimulator.rooms.get(ThreadLocalRandom.current().nextInt(StartSimulator.rooms.size()));
		destinationX = destinationRoom.x1+ThreadLocalRandom.current().nextInt(0,destinationRoom.getWidth());
		destinationY = destinationRoom.y1+ThreadLocalRandom.current().nextInt(0,destinationRoom.getHeight());
	}
}
