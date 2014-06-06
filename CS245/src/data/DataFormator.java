package data;

import java.util.HashSet;
import java.util.Set;

import reader.InputParser;

public class DataFormator {
	
	private String[] includes;
	
	private String[] enumDefs;
	
	private String[] smNames;
	
	private State[][] states;
	
	private StringBuilder mainFn;
	
	private StringBuilder[] tickFns;
	
	private int timerGCD;

	public DataFormator(InputParser in)
	{
		formatData(in);
	}
	
	public String[] getIncludes()
	{
		return includes;
	}
	
	public String[] getEnumDefs()
	{
		return enumDefs;
	}
	
	public String[] getTickFns()
	{
		String[] tickFnsStr = new String[tickFns.length];
		
		for(int i = 0; i < tickFns.length; i++)
			tickFnsStr[i] = tickFns[i].toString();
		
		return tickFnsStr;
	}
	
	public String getMainFn()
	{
		return mainFn.toString();
	}
	
	private void formatData(InputParser in)
	{
		/* Read includes*/
		readIncludes(in.getIncludes());
		
		StateMachine[] stateMachines = in.getStateMachines();
		
		if(stateMachines.length > 0)
		{
			/* Read state Machine names*/
			readNames(stateMachines);
			
			/*Read states*/
			readStates(stateMachines);
			
			/* Construct enum defs*/
			constructEnums();
			
			/* Construct main function*/
			constructMainFn(stateMachines);
			
			/* Construct tick functions*/
			constructTickFns();
		}
		
	}
	
	private void readIncludes(String[] in)
	{
		includes = new String[in.length + 1];	
		
		for(int i = 0; i < in.length; i++)
		{
			includes[i] = "#include \"" + in[i] + "\"";
		}
		
		includes[includes.length - 1] = linuxIncludes;
	}
	
	private void readNames(StateMachine[] sms)
	{
		smNames = new String[sms.length];
		
		for(int i = 0; i < sms.length; i++)
			smNames[i] = sms[i].getName();
	}
	
	private StringBuilder constructTimerDefs(StateMachine[] sms)
	{
		int[] periods = new int[sms.length];
		StringBuilder smPeriodDefs = new StringBuilder();
		
		for(int i = 0; i < sms.length; i++)
		{
			periods[i] = sms[i].getPeriod();
			smPeriodDefs.append("\tconst unsigned long ");
			smPeriodDefs.append(smNames[i]);
			smPeriodDefs.append("_period = ");
			smPeriodDefs.append(Integer.toString(periods[i]));
			smPeriodDefs.append(";\n");
		}
		
		timerGCD = gcdOfArray(periods); 
		String timerPeriod = Integer.toString(timerGCD);
		
		/* Append gcd defs*/
		smPeriodDefs.append("\n\tconst unsigned long GCD = ");
		smPeriodDefs.append(timerPeriod);
		smPeriodDefs.append(end +"\n\n");
		smPeriodDefs.append("\tunsigned char i; // Index for scheduler's for loop\n");
		
		/* Construct linux specific timer declarations*/
		constructLinuxTimerDecl(smPeriodDefs);
		
		return smPeriodDefs;
	}
	
	private void constructLinuxTimerDecl(StringBuilder timerDefs)
	{
		timerDefs.append("\n\tstruct itimerval itv;");
		timerDefs.append("\n\tstruct sigaction sa;");
	}
	
	private void readStates(StateMachine[] sms)
	{
		states = new State[sms.length][];
		
		for(int i = 0; i < sms.length; i++)
		{
			states[i] = new State[sms[i].getNumberOfStates()];
			
			int j = 1;
			
			Set<Integer> stateIndices = sms[i].getStateMapKeys();
			
			for(int index : stateIndices)
			{
				if(index == 0)
				{
					states[i][0] = sms[i].getState(index);
				}
				else
				{
					states[i][j] = sms[i].getState(index);
					j++;
				}
			}
		}
	}
	
	private void constructEnums()
	{
		enumDefs = new String[states.length];
		
		for(int i = 0; i < enumDefs.length; i++)
		{
			enumDefs[i] = "";
			
			/* sm should have atleast one state for enum defs*/
			if(states[i].length > 0)
			{
				enumDefs[i] = "enum " + smNames[i] + "_states { ";
				
				for(int j = 0; j < states[i].length; j++)
				{
					enumDefs[i] += states[i][j].getName();
					
					if(j+1 < states[i].length)
						enumDefs[i] += ", ";
				}
				
				enumDefs[i] += " };";
			}
		}
	}
	
	private StringBuilder constructOutputsInit()
	{
		StringBuilder outputsInit = new StringBuilder();
		
		HashSet<String> outputVars = new HashSet<String>();
		
		outputsInit.append("\t/* Initialize output variables*/\n");
		
		for(int i = 0; i < states.length; i++)
			for(int j = 0; j < states[i].length; j++)
			{
				String[] actions = states[i][j].getActions();
				
				if(actions != null)
				{	
					for(int k = 0; k < actions.length; k++)
					{

						/* get the first token of the action string*/
						String regex = "([\\+\\-\\*/&%\\|\\^]|[<>]+=)|([=])|([\\+\\-]+)";
						
						if(actions[k].contains("=") || actions[k].contains("+") || actions[k].contains("-"))
						{
							String outputVar = actions[k].split(regex)[0].trim();
						
							if(!outputVars.contains(outputVar))
							{
								outputVars.add(outputVar);
								outputsInit.append("\t" + outputVar + " = 0;\n");
							}
						}
					}
				}
			}
		
		return outputsInit;
	}
	
	private StringBuilder constructTaskDefs()
	{
		StringBuilder taskDefs = new StringBuilder("\tstatic task_t ");
		StringBuilder taskPtrDefs = new StringBuilder("\ttask_t *tasks[] = { ");
		StringBuilder taskInits = new StringBuilder();
		
		for(int i = 0; i < smNames.length; i++)
		{
			String taskNo = Integer.toString(i+1);
			
			taskDefs.append("task");
			taskDefs.append(taskNo);
			
			taskPtrDefs.append("&task");
			taskPtrDefs.append(taskNo);
			
			/* Initialize state*/
			taskInits.append("\ttask");
			taskInits.append(taskNo);
			taskInits.append(".state = -1;\n");
			
			/* Initialize period*/
			taskInits.append("\ttask");
			taskInits.append(taskNo);
			taskInits.append(".period = ");
			taskInits.append(smNames[i]);
			taskInits.append("_period;\n");
			
			/* Initialize elapsedTime*/
			taskInits.append("\ttask");
			taskInits.append(taskNo);
			taskInits.append(".elapsedTime = ");
			taskInits.append(smNames[i]);
			taskInits.append("_period;\n");
			
			/* Initialize tick function ptr*/
			taskInits.append("\ttask");
			taskInits.append(taskNo);
			taskInits.append(".TickFct = &");
			taskInits.append(smNames[i]);
			taskInits.append("_Tick;\n\n");
			
			if(i+1 < smNames.length)
			{
				taskDefs.append(", ");
				taskPtrDefs.append(", ");
			}
		}
		
		taskDefs.append(end);
		taskDefs.append("\n");
		
		taskPtrDefs.append(" };\n");
		
		/* Append task struct ptr defs to one string*/
		taskDefs.append(taskPtrDefs);
		
		taskDefs.append("\t" + numtaskDefStr);
		taskDefs.append("\n\n");
		
		/* Append task struct initializations to one string*/
		taskDefs.append(taskInits);
		
		return taskDefs;
	}
	
	private void constructMainFn(StateMachine[] sms)
	{
		/* Construct defitions related to state Machine periods and timer period*/
		StringBuilder periodDefs = constructTimerDefs(sms);
		
		/* get ouputs*/
		StringBuilder outputInits = constructOutputsInit();
		
		/* Construct task type definitions and initializations*/
		StringBuilder taskDefs = constructTaskDefs();
		
		/* Construct linux specific timer intializations*/
		StringBuilder timerInits = constructLinuxTimerDefs();
		
		mainFn = new StringBuilder("void main()\n{\n");
		
		mainFn.append(periodDefs);
		mainFn.append("\n\n");
		mainFn.append(taskDefs);
		mainFn.append(outputInits);
		mainFn.append("\n");
		mainFn.append(timerInits);
		
		/* main loop body*/
		mainFn.append( "\n\twhile(1) {\n");
		mainFn.append("\t\tfor ( i = 0; i < numTasks; ++i ) {\n");
		mainFn.append("\t\t\tif ( tasks[i]->elapsedTime == tasks[i]->period ) {\n");
		mainFn.append("\t\t\t\t// Task is ready to tick, so call its tick function\n");
		mainFn.append("\t\t\t\ttasks[i]->state = tasks[i]->TickFct(tasks[i]->state);\n");
		mainFn.append("\t\t\t\ttasks[i]->elapsedTime = 0; // Reset the elapsed time\n");
		mainFn.append("\t\t\t}\n");
		mainFn.append("\t\t\ttasks[i]->elapsedTime += GCD; // Account for below wait\n");
		mainFn.append("\t\t}\n");
		mainFn.append("\t\twhile(!TimerFlag); // Wait for next timer tick\n");
		mainFn.append("\t\tTimerFlag = 0;\n");
		mainFn.append("\t}\n");
		
		mainFn.append("}\n");
	}
	
	private StringBuilder constructLinuxTimerDefs()
	{
		StringBuilder lTimerDefs = new StringBuilder();
		
		/* append signal initializations*/
		lTimerDefs.append("\t/* Initialize signal*/\n");
		lTimerDefs.append("\tsigemptyset(&sa.sa_mask);\n\tsa.sa_flags = 0;\n\tsa.sa_handler = TimerHandler;\n");
		lTimerDefs.append("\tif (sigaction(SIGALRM, &sa, NULL) == -1)\n\t{\n\t\texit(EXIT_FAILURE);\n\t}\n");
		
		/* append interval timer initializations*/
		int itSecVal = 0;
		int ituSecVal = 0;
		
		/* calculate sec and usec values for interval timer*/
		if(timerGCD < 1000)
			ituSecVal = timerGCD * 1000;
		else
		{
			itSecVal = timerGCD/1000;
			ituSecVal = (timerGCD % 1000) * 1000;
		}
		
		lTimerDefs.append("\n\t/* Initialize timer with the period*/");
		lTimerDefs.append("\n\titv.it_value.tv_sec = ");
		lTimerDefs.append(itSecVal);
		lTimerDefs.append(";");
		
		lTimerDefs.append("\n\titv.it_value.tv_usec = ");
		lTimerDefs.append(ituSecVal);
		lTimerDefs.append(";");
		
		lTimerDefs.append("\n\titv.it_interval.tv_sec = ");
		lTimerDefs.append(itSecVal);
		lTimerDefs.append(";");
		
		lTimerDefs.append("\n\titv.it_interval.tv_usec = ");
		lTimerDefs.append(ituSecVal);
		lTimerDefs.append(";");
		
		lTimerDefs.append("\n\n\t/* Start timer*/");
		lTimerDefs.append("\n\tif (setitimer(ITIMER_REAL, &itv, NULL) == -1)\n\t{\n\t\texit(EXIT_FAILURE);\n\t}\n");
		
		return lTimerDefs;
	}
	
	
	private void constructTickFns()
	{
		tickFns = new StringBuilder[smNames.length];
		
		for(int i = 0; i < smNames.length; i++)
		{
			tickFns[i] = new StringBuilder("int " + smNames[i] + "_Tick(int state) {\n");
			
			constructTransitions(tickFns[i], i);
			
			constructActions(tickFns[i], i);
			
			tickFns[i].append("\treturn state;\n");
			
			tickFns[i].append("}\n");
		}
	}
	
	private void constructTransitions(StringBuilder sb, int index)
	{
		sb.append("\tswitch(state) { // Transitions\n");
		
		/* Initial state*/
		sb.append("\t\tcase -1:\n");
		sb.append("\t\t\tstate = " + states[index][0].getName());
		sb.append(";\n\t\t\tbreak;\n");
		
		for(int i = 0; i < states[index].length; i++)
		{
			sb.append("\t\tcase " + states[index][i].getName() + ":\n");
			
			Transition[] t = states[index][i].getTransitions();
			
			for(int j = 0; j < t.length; j++)
			{
				sb.append("\t\t\tif(");
				
				String condition = t[j].getCondition();
				
				if(condition.equals(""))
				{
					sb.append("1");
				}
				else
				{
					sb.append(condition);
				}
				
				sb.append(")\n\t\t\t{\n");
				
				/*append the transition state*/
				sb.append("\t\t\t\tstate = " + t[j].getNextState().getName());
				sb.append(";\n\t\t\t}\n");
			}
			
			sb.append("\t\t\tbreak;\n");
		}
		
		/* append default case*/
		sb.append("\t\tdefault:\n\t\t\tstate = -1;\n\t\t\tbreak;\n\t}\n\n");
	}
	
	private void constructActions(StringBuilder sb, int index)
	{
		sb.append("\tswitch(state) { // State actions\n");
		
		for(int i = 0; i < states[index].length; i++)
		{
			sb.append("\t\tcase " + states[index][i].getName() + ":\n");
			
			String[] actions = states[index][i].getActions();
			
			if(actions != null)
			{
				for(int j = 0; j < actions.length; j++)
				sb.append("\t\t\t" + actions[j] + "\n");
			}
			sb.append("\t\t\tbreak;\n");
		}
		
		/* append default case*/
		sb.append("\t\tdefault:\n\t\t\tbreak;\n\t}\n");
	}
	
	private int gcdOfArray(int[] input)
	{
		int result = input[0];
		
		for(int i = 1; i < input.length; i++)
		{
			result = gcd(result, input[i]);
		}
		
		return result;
	}
	
	private int gcd(int a, int b)
	{
		if (b == 0)
			return a;
		else
			return gcd(b, a % b);
	}
	
	public static final String timerFlagDefStr = "volatile unsigned char TimerFlag = 0;";
	
	public static final String timerISRFnStr = "void TimerHandler() {\n"
			+ "\tTimerFlag = 1;\n"
			+ "}";
	
	public static final String taskStructDefStr = "typedef struct {\n"
			+ "\tsigned char state;\n"
			+ "\tunsigned long period;\n"
			+ "\tunsigned long elapsedTime;\n"
			+ "\tint (*TickFct)(int);\n"
			+ "} task_t;";
	
	private static final String numtaskDefStr = "const unsigned short numTasks = sizeof(tasks) / sizeof(task_t*);";
	
	public static final String end = ";";
	
	private static final String linuxIncludes = "#include \"sys/time.h\"\n#include \"signal.h\"\n#include \"stdlib.h\"";

}
