package logic;

import java.util.LinkedList;
import java.util.List;

public class StatRecorder {

	public List<Double> avgTimeRuleApplication;
	public StatRecorder() {
		avgTimeRuleApplication = new LinkedList<Double>();
	}
	public double getAvgTime() {
		return getAvg(avgTimeRuleApplication);
	}
	private double getAvg(List<Double> list) {
		double tot = 0;
		for(Double d: list) tot += d;
		return tot/list.size();
	}
	
	
}
