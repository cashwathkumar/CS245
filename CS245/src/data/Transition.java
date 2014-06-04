package data;

public class Transition {

	private String condition;
	
	private State toState;
	
	public Transition(String condition, State toState)
	{
		this.condition = condition;
		this.toState = toState;
	}
	
	public String getCondition()
	{
		return condition;
	}
	
	public State getNextState()
	{
		return toState;
	}
}
