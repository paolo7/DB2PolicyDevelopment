import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;

import dbconnection.DBConnection;
import dbconnection.MySQLConnection;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

public class StartSimulator extends PApplet{

	public static int millisecondsBetweenWorldUpdates = 1000;
	
	public static int roomSensorFireRateBase = 1100;
	public static int roomSensorFireRateRandomness = 50;
	public static double roomSensorErrorMargin = 2;
	
	public static int personalSensorFireRateBase = 800;
	public static int personalSensorFireRateRandomness = 50;
	public static double personalSensorErrorMargin = 2;
	
	public static int windowWidth = 1100;
	public static int windowHeight = 900;
	
	public static DBConnection db;
	
	public static int roomSensorNumber = 15;
	public static int roomsNumber = 8;
	public static int personalSensorNumber = 6;
	public static int peopleNumber = 10;
	public static int minRoomSize = 100;
	public static int maxRoomSize = 280;
	public static int imgx = 0;
	public static int imgy = 0;
	boolean dragging = false;
	public static double maxCO2 = 10;
	public static double minCO2 = 0;
	
	public static float personCircleWidth = 12f;
	public static float sensorCircleWidth = 6f;
	
	public static int mapWidth;
	public static int mapHeight;
	public static List<Room> rooms;
	public static List<Person> people;
	public static List<RoomSensor> roomSensors;
	public static List<PersonalSensor> personalSensors;
	
	public static boolean assignRoomSensorsEqually = true;
	public static int roomSensorEqualAllocationIndex = 0;
	public static boolean assignPersonalSensorsEqually = true;
	public static int personalSensorEqualAllocationIndex = 0;
	
	public static PGraphics pg;
	public static Timer time;
	
	
	private static Map<String,String> typeDefinitionTables = new HashMap<String,String>(); 
	private static Map<String,String> observablePropertyTables = new HashMap<String,String>();
	private static Map<String,String> URILabels = new HashMap<String,String>();
	
	public static void main(String[] args) {
		db = new MySQLConnection("root","root","SensorReadingDB");
		db.initialiseDB();
		
		typeDefinitionTables.put("employee", "http://obsp.com/Employee");
		typeDefinitionTables.put("room", "http://obsp.com/Room");
		observablePropertyTables.put("location", "http://obsp.com/Location");
		observablePropertyTables.put("CO2concentration", "http://obsp.com/CO2concentration");
		URILabels.put("http://obsp.com/Employee", "Employee");
		URILabels.put("http://obsp.com/Room", "Room");
		URILabels.put("http://obsp.com/Location", "Location");
		URILabels.put("http://obsp.com/CO2concentration", "CO2-concentration");
		
		try {
			DBReasoner.analyseDB(typeDefinitionTables,observablePropertyTables,URILabels);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mapWidth = (int) 100+maxRoomSize*2+Math.round(roomsNumber/8)*(maxRoomSize) ;
		mapHeight = (int) 100+maxRoomSize*2+Math.round(roomsNumber/8)*(maxRoomSize) ;
        
        rooms = new LinkedList<Room>();
        people = new LinkedList<Person>();
        roomSensors = new LinkedList<RoomSensor>();
        personalSensors = new LinkedList<PersonalSensor>();
        time = new Timer();
        initialiseMap();
        
        SimulatorUpdater updater = new SimulatorUpdater();
        time.schedule(updater, 0, millisecondsBetweenWorldUpdates);
        
        
        PApplet.main("StartSimulator");
    }
	
	public static void initialiseMap() {
		while(rooms.size() < roomsNumber) {
			createNewRoom();
		}
		while(people.size() < peopleNumber) {
			createNewPerson();
		}
		while(roomSensors.size() < roomSensorNumber) {
			createNewRoomSensor();
		}
		while(personalSensors.size() < personalSensorNumber) {
			createNewPersonalSensor();
		}
	}
	
	public static void createNewPerson() {
		Room startingRoom = rooms.get(ThreadLocalRandom.current().nextInt(rooms.size()));
		Person p = new Person(startingRoom.x1+ThreadLocalRandom.current().nextInt(0,startingRoom.getWidth()),
				startingRoom.y1+ThreadLocalRandom.current().nextInt(0,startingRoom.getHeight()),people.size());
		p.setDestinationRoom();
		people.add(p);
		db.insertStatement("INSERT INTO employee "
				+ "(employeeCode, entityID, name, surname,age) "
				+ "VALUES (unhex(replace(uuid(),'-','')), '"+p.ID+"','"+Utilities.generateName()+"','"+Utilities.generateName()+"',"
						+ ThreadLocalRandom.current().nextInt(18,60)+")");
	}
	
	public static void createNewPersonalSensor() {
		int personIndex = personalSensorEqualAllocationIndex;
		if(assignPersonalSensorsEqually) {
			personalSensorEqualAllocationIndex++;
			if (personalSensorEqualAllocationIndex >= people.size())
				personalSensorEqualAllocationIndex = 0;
		} else {
			personIndex = ThreadLocalRandom.current().nextInt(people.size());
		}
		Person assignedPerson = people.get(personIndex);
		PersonalSensor s = new PersonalSensor(assignedPerson.x,assignedPerson.y, 
				assignedPerson,personalSensorFireRateBase,personalSensorErrorMargin,personalSensors.size());
		personalSensors.add(s);
		time.schedule(s, 0, personalSensorFireRateBase+ThreadLocalRandom.current().nextInt(personalSensorFireRateRandomness));
	}
	
	public static void createNewRoomSensor() {
		int roomIndex = roomSensorEqualAllocationIndex;
		if(assignRoomSensorsEqually) {
			roomSensorEqualAllocationIndex++;
			if (roomSensorEqualAllocationIndex >= rooms.size())
				roomSensorEqualAllocationIndex = 0;
		} else {
			roomIndex = ThreadLocalRandom.current().nextInt(rooms.size());
		}
		Room startingRoom = rooms.get(roomIndex);
		RoomSensor s = new RoomSensor(startingRoom.x1+ThreadLocalRandom.current().nextInt(0,startingRoom.getWidth()),
				startingRoom.y1+ThreadLocalRandom.current().nextInt(0,startingRoom.getHeight()), 
				startingRoom,roomSensorFireRateBase,roomSensorErrorMargin,roomSensors.size());
		roomSensors.add(s);
		time.schedule(s, 0, roomSensorFireRateBase+ThreadLocalRandom.current().nextInt(roomSensorFireRateRandomness));
	}
	
	// attempts to create a new room, but only adds it to the list if it does not overlap any of the other rooms
	public static void createNewRoom() {
		int x1 = ThreadLocalRandom.current().nextInt(0, mapWidth-maxRoomSize);
		int y1 = ThreadLocalRandom.current().nextInt(0, mapHeight-maxRoomSize);
		Room r = new Room(x1,
				x1+ThreadLocalRandom.current().nextInt(minRoomSize,maxRoomSize),
				y1,
				y1+ThreadLocalRandom.current().nextInt(minRoomSize,maxRoomSize),
				rooms.size());
		for(Room r1 : rooms) {
			if (r.intersects(r1)) return;
		}
		r.CO2 = ThreadLocalRandom.current().nextDouble(minCO2,maxCO2);
		rooms.add(r);
		db.insertStatement("INSERT INTO room "
				+ "(roomCode, entityID, location) "
				+ "VALUES (unhex(replace(uuid(),'-','')), '"+r.ID+"',"
				+ "ST_GeomFromText('POLYGON((  "
					+((double)r.x1)/100+" "+((double)r.y1)/100+" , "
					+((double)r.x2)/100+" "+((double)r.y1)/100+" , "
					+((double)r.x2)/100+" "+((double)r.y2)/100+" , "
					+((double)r.x1)/100+" "+((double)r.y2)/100+" , "
					+((double)r.x1)/100+" "+((double)r.y1)/100+" "
							+ "))')"
				+ " )");
		System.out.println("Added room n."+rooms.size());
	}
	
	public void settings(){
        size(windowWidth,windowHeight);
    }

    public void setup(){
        fill(255,255,255);
        surface.setResizable(true);
        pg = createGraphics(mapWidth, mapHeight);
    }

    public void draw(){
    	pg.beginDraw();
    	pg.background(120);
    	
    	for (Room r : rooms) {
    		pg.fill(255,255-Math.round(r.CO2*2.5),255-Math.round(r.CO2*2.5));
    		pg.rect(r.x1,r.y1,r.getWidth(),r.getHeight());
    		pg.fill(0,0,0);
    		PFont f = createFont("Arial",8,true);
    		pg.text(r.ID,r.x1,r.y1+10);
    		pg.text("CO2:"+Math.round(r.CO2),r.x1,r.y1+20);
    		
    	}
    	for (Person p : people) {
    		pg.fill(66, 134, 244);
    		pg.ellipse(p.x, p.y, 
    				personCircleWidth, personCircleWidth);
    	}
    	
    	for (RoomSensor s : roomSensors) {
    		pg.fill(0, 252, 38);
    		pg.ellipse(s.x, s.y, 
    				sensorCircleWidth, sensorCircleWidth);
    	}
    	for (PersonalSensor s : personalSensors) {
    		pg.fill(252,248,0);
    		pg.ellipse(s.x, s.y, 
    				sensorCircleWidth, sensorCircleWidth);
    	}
    	pg.endDraw();
    	 if (!mousePressed && dragging) dragging = false;
    	  if (dragging)
    	  {
    	    imgx += -pmouseX+mouseX;
    	    imgy += -pmouseY+mouseY;
    	    if (imgx > 0) imgx = 0;
    	    if (imgy > 0) imgy = 0;
    	  }
    	image(pg, imgx, imgy);
    }
    public void mousePressed()
    {
      dragging = true;
    }
}
