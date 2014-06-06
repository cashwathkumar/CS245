package reader;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import data.State;
import data.StateMachine;
import data.Transition;

public class InputParser {
	
	private StateMachine[] stateMachines;
	
	private String[] includes;
	
	public boolean readInputFile(File input)
	{
		boolean readSuccess = true;
		
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(input));
			
			br.readLine();
			
			readIncludes(br);
			
			int numberOfSM = Integer.parseInt(br.readLine().split("[|\t]+")[1].trim());
			
			stateMachines = new StateMachine[numberOfSM];
			
			for(int i = 0; i < numberOfSM; i++)
			{
				br.readLine(); //read delimiter
				
				String smName = br.readLine().split("[ ]+")[0]; //read state machine name
				int period = Integer.parseInt(br.readLine().split("[|\t]+")[1].trim()); //read period
				stateMachines[i] = new StateMachine(smName, period);
				
				int totalTransitions = readStateInfo(stateMachines[i], br);
				
				readTransitionInfo(stateMachines[i], br, totalTransitions);
			}

			br.close();
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			readSuccess = false;
			System.out.println("File Not Found");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			readSuccess = false;
			e.printStackTrace();
		}
		catch (NumberFormatException ne)
		{
			readSuccess = false;
			System.out.println("Incorrect input not a number");
		}
		catch (ArrayIndexOutOfBoundsException ae)
		{
			readSuccess = false;
			System.out.println("Incorrect input format");
		}
		
		return readSuccess;
	}
	
	private int readStateInfo(StateMachine sm, BufferedReader br)
			throws IOException, NumberFormatException, ArrayIndexOutOfBoundsException
	{
		int totalTransitions = 0;
		
		/* Read number of states*/
		int numberOfStates = Integer.parseInt(br.readLine().split("[|\t]+")[1].trim());
		
		assert(numberOfStates > 0);
		
		/* Dummy reads*/
		br.readLine();
		br.readLine();
	
		/* Read information for each state*/
		for(int i = 0; i < numberOfStates; i++)
		{
			String tokens[] = br.readLine().split("[\t|]+");
			
			String stateName = tokens[0].trim();
			int stateIndex = Integer.parseInt(tokens[1].trim());
			int outTransitions = Integer.parseInt(tokens[2].trim());
			totalTransitions += outTransitions;
			String[] actions = parseAction(tokens);
			
			State s = new State(sm.getName() + "_" + stateName, outTransitions);
			s.setActions(actions);
			
			sm.addState(stateIndex, s);
		}
		
		return totalTransitions;
	}
	
	private void readTransitionInfo(StateMachine sm, BufferedReader br, int numTransitions)
			throws IOException, NumberFormatException, ArrayIndexOutOfBoundsException
	{
		/* Dummy reads*/
		br.readLine();
		br.readLine();
		
		/* Read transition information*/
		for(int i = 0; i < numTransitions; i++)
		{
			String tokens[] = br.readLine().split("[|\t]+");
			
			int fromState = Integer.parseInt(tokens[0].trim());
			int toState = Integer.parseInt(tokens[1].trim());
			String condition = parseCondition(tokens);
			
			
			State fromStateObj = sm.getState(fromState);
			State toStateObj = sm.getState(toState);
			
			Transition t = new Transition(condition, toStateObj);
			
			fromStateObj.addTransition(t);
		}
	}
	
	private void readIncludes(BufferedReader br) throws IOException
	{
		String[] tokens = br.readLine().split("[ |]+");
		
		if(tokens.length > 1)
		{
			includes = new String[tokens.length - 1];
			
			/* Read the specified includes files*/
			for(int i = 0; i < tokens.length - 1; i++)
				includes[i] = tokens[i+1];
		}
		else
		{
			/* no includes specified*/
			includes = null;
		}
		
	}
	
	private String[] parseAction(String[] tokens)
	{
		String[] actions = null;
		
		if(tokens.length - 3 > 0)
			actions = new String[tokens.length - 3];
		
		for(int i = 3, j = 0; i < tokens.length; i++, j++)
			actions[j] = tokens[i].trim();
		
		return actions;
	}
	
	private String parseCondition(String[] tokens)
	{
		String condition = "";
		
		for(int i = 2; i < tokens.length; i++)
			condition += " " + tokens[i];
		
		return condition.trim();
	}
	
	public StateMachine[] getStateMachines()
	{
		return stateMachines;
	}
	
	public String[] getIncludes()
	{
		return includes;
	}
}
