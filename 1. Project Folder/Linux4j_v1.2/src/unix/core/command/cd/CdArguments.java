package unix.core.command.cd;

import java.io.File;
import java.util.ArrayList;

import unix.core.base.Arguments;

public class CdArguments implements Arguments
{
	private ArrayList<String> directories;
	
	public CdArguments(String currentDirectory, ArrayList<String> secondArgument) throws Exception 
	{
		super();
		this.directories = new ArrayList<String>();
		
		//Get Directories, additional options and OUT-file
		Boolean singleQuote=false;
		Boolean doubleQuote=false;
		String spacedDir="";
		for (String arg: secondArgument)
		{
			// Spaced Directory with SINGLE-QUOTE
			if(arg.charAt(0)=='\'' && !singleQuote) 
			{
				singleQuote=true;
				spacedDir="";
			}
			if(arg.charAt(arg.length()-1)=='\'' && singleQuote) 
			{
				spacedDir = spacedDir+" "+arg;
				singleQuote=false;
				this.directories.add(spacedDir.substring(2,spacedDir.length()-1));
				break;
			}
			if(singleQuote) 
			{
				spacedDir = spacedDir+" "+arg; 
				continue;
			}
			
			// Spaced Directory with DOUBLE-QUOTE
			if(arg.charAt(0)=='"' && !doubleQuote) 
			{
				doubleQuote=true;
				spacedDir="";
			}
			if(arg.charAt(arg.length()-1)=='\"' && doubleQuote) 
			{
				spacedDir = spacedDir+" "+arg; 
				doubleQuote=false;
				this.directories.add(spacedDir.substring(2,spacedDir.length()-1));
				break;
			}
			if(doubleQuote) 
			{
				spacedDir = spacedDir+" "+arg; 
				continue;
			}
			
			//Parent Directory [..]
			if (arg.length()>1 && arg.charAt(0) == '.' && arg.charAt(1) == '.')
			{
				this.directories.add(currentDirectory+"/"+arg);
				break;
			}
			
			//Home Directory [~]
			if (arg.length()>0 && (arg.charAt(0) == '~' || arg.charAt(0) == '\\'))
			{
				this.directories.add(System.getProperty("user.home"));
				break;
			}
			
			//Relative-Absolute Path
			if ( arg.charAt(0) == '/' || (arg.length()>2 && arg.charAt(1) == ':' && arg.charAt(2) == '\\'))
			{
				this.directories.add(new File(arg).getAbsolutePath());
				break;
			}
			else
			{
				this.directories.add(currentDirectory+"/"+arg);
				break;
			}
		}
	}
	
	@Override
	public String getOutFile() {return null;}

	public ArrayList<String> getDirectories() {
		return directories;
	}

	public void setDirectories(ArrayList<String> directories) {
		this.directories = directories;
	}
	
	
	
}
