package data;

public class State {
	
	private String name;
	
	private String[] actions;
	
	private Transition[] transitions;
	
	private int tIndex;
	
	public State(String name, int numberOfTransitions)
	{	
		this.name = name;
		
		if(numberOfTransitions > 0)
			transitions = new Transition[numberOfTransitions];
		
		tIndex = 0;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void addTransition(Transition t)
	{
		try
		{
			transitions[tIndex++] = t;
		}
		catch(ArrayIndexOutOfBoundsException a)
		{
			System.out.println("NumberOfTransition and the out transition from " + name + " does not match");
		}
	}
	
	public Transition[] getTransitions()
	{
		return transitions;
	}
	
	public void setActions(String[] actions)
	{
		this.actions = actions;
	}
	
	public String[] getActions()
	{
		return actions;
	}

}
