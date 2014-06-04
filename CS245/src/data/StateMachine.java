package data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class StateMachine {

	private String name;
	
	private HashMap<Integer, State> stateMap;
	
	private int period;
	
	public StateMachine(String name, int period)
	{
		this.name = name;
		
		this.period = period;
		
		stateMap = new HashMap<Integer, State>();
	}
	
	public void addState(int index, State s)
	{
		stateMap.put(index, s);
	}
	
	public State getState(int index)
	{
		return stateMap.get(index);
	}
	
	public int getNumberOfStates()
	{
		return stateMap.size();
	}
	
	public Collection<State> getStates()
	{
		return stateMap.values();
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getPeriod()
	{
		return period;
	}
	
	public Set<Integer> getStateMapKeys()
	{
		return stateMap.keySet();
	}
}
