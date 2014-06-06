package generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import reader.InputParser;
import data.DataFormator;

public class CodeGenerator {
	
	private DataFormator formattedData;
	
	public CodeGenerator(File inputFile)
	{
		InputParser inParser = new InputParser();
		
		boolean readSuccess = inParser.readInputFile(inputFile);
		
		if(readSuccess)
			formattedData = new DataFormator(inParser);
	}

	public void generateCode(File output)
	{
		if(formattedData != null)
		{
			BufferedWriter bw;
			try 
			{
				bw = new BufferedWriter(new FileWriter(output));
				
				generateIncludes(bw);
				
				generateDeclarations(bw);
				
				generateTickFns(bw);
				
				generateMainFn(bw);
				
				System.out.println("File generated");
				
				bw.close();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void generateIncludes(BufferedWriter bw) throws IOException
	{
		String[] includes = formattedData.getIncludes();
		
		for(int i = 0; i < includes.length; i++)
		{
			bw.write(includes[i]);
			bw.write("\n");
		}
		
		bw.write("\n");
	}
	
	private void generateDeclarations(BufferedWriter bw) throws IOException
	{
		/* Timer related declaration*/
		bw.write(DataFormator.timerFlagDefStr);
		bw.write("\n\n");
		bw.write(DataFormator.timerISRFnStr);
		bw.write("\n\n");
		
		/* Task struct declaration*/
		bw.write(DataFormator.taskStructDefStr);
		bw.write("\n\n");
		
		/* State enum declarations*/
		String[] enums = formattedData.getEnumDefs();
		
		for(int i = 0; i < enums.length; i++)
		{
			bw.write(enums[i]);
			bw.write("\n");
		}
		bw.write("\n");
	}
	
	private void generateTickFns(BufferedWriter bw) throws IOException
	{
		String[] tickFns = formattedData.getTickFns();
		
		for(int i = 0; i < tickFns.length; i++)
		{
			bw.write(tickFns[i]);
			bw.write("\n");
		}
	}
	
	private void generateMainFn(BufferedWriter bw) throws IOException
	{
		bw.write(formattedData.getMainFn());
	}
	
	
	public static void main(String args[])
	{
		/* Specify the input file in the command line arguement*/
		if(args.length == 1)
		{
			File inputFile = new File(args[0]);
			
			CodeGenerator cg = new CodeGenerator(inputFile);
			
			File outputFile = new File("sm.c");
			cg.generateCode(outputFile);
		}
		else
		{
			System.out.println("Invalid commandline input");
		}
	}
}
