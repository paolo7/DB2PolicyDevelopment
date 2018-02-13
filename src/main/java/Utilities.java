import java.util.Random;

public class Utilities {

	private static Random rand = new Random();
	 
	private static String[] Beginning = { "Kr", "Ca", "Ra", "Ba", "Pa", "Ta", "Mrok", "Cru",
	         "Ray", "Bre", "Zed", "Dak", "Mor", "Jag", "Mer", "Jar", "Mjol",
	         "Zok", "Mad", "Cry", "Zur", "Creo", "Azak", "Azur", "Rei", "Cro",
	         "Mar", "Luk", "Mac", "Von", "De", "Ja", "Yo" };
	private static String[] Middle = { "air", "ir", "mi", "sor", "me", "clo",
	         "red", "cra", "ark", "arc", "miri", "lori", "cres", "mur", "zer",
	         "marac", "zoir", "slamar", "salmar", "urak", "o", "", "", "", "", "" };
	private static String[] End = { "d", "ed", "ark", "arc", "es", "er", "der",
	         "tron", "med", "ure", "zur", "cred", "mur", "be", "ble", "lo",
	         "ton", "son", "", "", "", "", "" };

	/**
	 * Random human-like name generator
	 * @return
	 */
	public static String generateName() {
	    return Beginning[rand.nextInt(Beginning.length)]
	         + Middle[rand.nextInt(Middle.length)]
	         + End[rand.nextInt(End.length)];
	}
}
