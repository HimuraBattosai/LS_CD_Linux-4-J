package unix.core.command.ls;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import unix.core.base.CommandParser;
import unix.util.FileInfo;

public class LsCommand 
{
	private String directory;
	private ArrayList<FileInfo> filesList ;
	
	//OptionsAttributes
	private Boolean noOwner;
	private Boolean noGroup;
	private Boolean noAuthor;
	
	private Boolean isoTime;
	private String fileIndicator;
	private String sizeScale;
	private int sizePower;
	private String quoting;
	private Boolean allocatedSize;
	private Boolean showAccessTime;
	
	private Boolean listSubDir;
	
	
	public LsCommand(String directory) throws Exception 
	{
		super();
		this.directory=directory;
		this.filesList = new ArrayList<FileInfo>();
		
		//Add DIRECTORY and PARENT
		FileInfo currentDir = new FileInfo(new File(directory));
		currentDir.setName(".");
        this.filesList.add(currentDir);
        this.filesList.add(new FileInfo(new File(directory+"/..")));
        
        //Add Directory's internal content
		File dir = new File(directory);
        String directoryFiles[] = dir.list();
        if(directoryFiles!=null)
	        for(String file: directoryFiles)
	        {
	            this.filesList.add(new FileInfo(new File(directory + "/" + file )));
	        }
        
        
        //OptionsAttributes
        this.noOwner = false;
        this.noGroup = false;
        this.noAuthor=true;
        
        this.isoTime = false;
        this.fileIndicator="none";
        this.sizeScale="B";
        this.sizePower=1024;
        this.quoting="shell";
        this.allocatedSize=false;
        this.showAccessTime=false;
        this.listSubDir=false;
	}

	/************************************************SEARCH-WILDCARD***************************************************************/
	public LsCommand searchPattern(String wildCard)
	{
		wildCard = wildCard.replaceAll("\\*", ".*");
		
		Iterator<FileInfo> iter = this.filesList.iterator();
		while(iter.hasNext())
		{
			FileInfo file = iter.next();
			if(!file.getName().matches(wildCard)) iter.remove();
		}
		return this;
	}
	/******************************************************************************************************************************/
	
	/****************************************************FILTERS*******************************************************************/
	/*
	 * -d, --directory            list directories themselves, not their contents
	 */
	public LsCommand directory()
	{
		ArrayList<FileInfo> newFilesList = new ArrayList<FileInfo>();
		newFilesList.add(new FileInfo(new File(directory)));
		this.filesList = newFilesList;
		
		return this;
	}
	
	/*
	 * -B, --ignore-backups       do not list implied entries ending with ~
	 */
	public LsCommand ignoreBackups()
	{
		Iterator<FileInfo> iter = this.filesList.iterator();
		while(iter.hasNext())
		{
			FileInfo file = iter.next();
			if(file.getName().charAt(file.getName().length()-1) == '~') iter.remove();
		}
		return this;
	}
	
	/*
	 * -A, --almost-all           do not list implied . and ..
	 */
	public LsCommand almostAll()
	{
		Iterator<FileInfo> iter = this.filesList.iterator();
		while(iter.hasNext())
		{
			FileInfo file = iter.next();
			if(file.getName().equals(".") || file.getName().equals("..")) iter.remove();
		}
		
		return this;
	}
	
	/*
	 * default listing			ignore entries starting with .
	 */
	public LsCommand defaultListing()
	{
		Iterator<FileInfo> iter = this.filesList.iterator();
		while(iter.hasNext())
		{
			FileInfo file = iter.next();
			if(file.getName().charAt(0) == '.') iter.remove();
		}
		return this;
	}
	
	/******************************************************************************************************************************/


	/****************************************************SORTERS*******************************************************************/
	/*
	 * -t                         sort by modification time, newest first
	 */
	public LsCommand sortByModificationTime()
	{
		Collections.sort(this.filesList, FileInfo.fileModificationTimeComparator);
		return this;
	}
	
	/*
	 * -u                         sort by access time, newest first
	 */
	public LsCommand sortByAccessTime()
	{
		Collections.sort(this.filesList, FileInfo.fileAccessTimeComparator);
		return this;
	}
	
	/*
	 * -S                         sort by file size, largest first
	 */
	public LsCommand sortBySize()
	{
		Collections.sort(this.filesList, FileInfo.fileSizeComparator);
		return this;
	}
	
	/*
	 * -X                         sort alphabetically by entry extension
	 */
	public LsCommand sortByExtension()
	{
		Collections.sort(this.filesList, FileInfo.fileExtensionComparator);
		return this;
	}
	
	/*
	 * -r, --reverse              reverse order while sorting
	 */
	public LsCommand reverseSort()
	{
		Collections.reverse(this.filesList);
		return this;
	}
	
	/*
	 * --group-directories-first	group directories before files
	 */
	public LsCommand directoriesFirst()
	{
		Collections.sort(this.filesList, FileInfo.fileDirectoryComparator);
		return this;
	}
	/******************************************************************************************************************************/
	
	/****************************************************ELEMENTS to DISPLAY*******************************************************/
	/*
	 * -o                         like -l, but do not list group information
	 * -G, --no-group             in a long listing, don't print group names
	 */
	public LsCommand noGroup()
	{
		this.noGroup=true;
		return this;
	}
	
	/*
	 * -g                         like -l, but do not list owner
	 */
	public LsCommand noOwner()
	{
		this.noOwner=true;
		return this;
	}
	
	/*
	 * --author               with -l, print the author of each file
	 */
	public LsCommand showAuthor()
	{
		this.noAuthor=false;
		return this;
	}
	
	/*
	 * -s, --size                 print the allocated size of each file, in blocks
	 */
	public LsCommand allocatedSize()
	{
		this.allocatedSize=true;
		return this;
	}
	
	/*
	 * -u                         show access time
	 */
	public LsCommand showAccessTime()
	{
		this.showAccessTime=true;
		return this;
	}
	
	/*
	 * ls *							list subdir
	 */
	public LsCommand listSubDir()
	{
		this.listSubDir=true;
		return this;
	}
	/******************************************************************************************************************************/
	
	/******************************************************FORMAT to DISPLAY*******************************************************/
	/*
	 * --full-time            like -l --time-style=full-iso
	 */
	public LsCommand fullTime()
	{
		this.isoTime=true;
		return this;
	}
	
	/*
	 * --block-size=SIZE      scale sizes by SIZE before printing them
	 * -h, --human-readable       with -l and/or -s, print human readable sizes
	 * --si                   likewise, but use powers of 1000 not 1024
	 */
	public LsCommand sizeFormat(String sizeScale, int sizePower)
	{
		this.sizeScale=sizeScale;
		if(sizePower != 0) this.sizePower=sizePower;
		return this;
	}
	
	/*
	 * -F, --classify             append indicator (one of *@/=>|) to entries  
	 */
	public LsCommand fileIndicator(String indicator)
	{
		this.fileIndicator=indicator;
		return this;
	}
	
	/*
	 * -L, --dereference          show target file information for SymLinks
	 */
	public LsCommand dereference() throws IOException
	{
		for(FileInfo file: this.filesList)
		{
			if(file.isSymLink())
			{
				FileInfo targetFile = new FileInfo(new File(file.getSymLink()));
				this.filesList.set(this.filesList.indexOf(file), targetFile);
			}
		}
		return this;
	}
	
	/*
	 * -Q, --quote-name           enclose entry names in double quotes
     * --quoting-style=WORD   	use quoting style WORD for entry names
	 */
	public LsCommand quotingStyle(String quoting)
	{
		this.quoting=quoting;
		return this;
	}
	
	/******************************************************************************************************************************/
	
	/******************************************************DISPLAY*****************************************************************/
	public void longView(CommandParser command, PrintWriter out) throws IOException, ParseException
	{
		
		//Normal view, no subdir Listing
		if(!this.listSubDir)
		{
			out.println( (new File(directory)).getName() + ":\n" );
			for(FileInfo file: this.filesList) this.detailedLine(file, out);
		}
		else
		{
			out.println( (new File(directory)).getName() + ":\n" );
			
			//List all non-directories and symlinks
			for(FileInfo file: this.filesList)
			{
				if(!file.isDirectory() || file.isSymLink()) this.detailedLine(file, out);
					
			}
			
			//List Subdir of all directories
			for(FileInfo file: this.filesList)
			{
				if(file.isDirectory() && !file.isSymLink())
				{
					LsCommandBuilder lsBuilder = new LsCommandBuilder();
					
					// Disable the subdir option or will get a cascading loop
					LsArguments lsArg = (LsArguments) command.getArguments();
					lsArg.setListAllSubdir(false);
					
					//Update directory to subDir
					ArrayList<String> subDir= new ArrayList<String>();
					subDir.add(file.getAbsolutePath());
					lsArg.setDirectories(subDir);
					
					command.setArguments(lsArg);
					
					try {
						lsBuilder.execute(command, out);
					} catch (Exception e) {}
				}	
			}
		}
		
	}
	
	public void singleColumnView(PrintWriter out) throws IOException, ParseException
	{
		for(FileInfo file: this.filesList)
		{
			if(this.allocatedSize)
			{
				//Minimum scale for allocatedSize is KB
				if(this.sizeScale.equals("B"))
					out.printf("%15.15s ",file.getFormatedSize("K", this.sizePower));
				else
					out.printf("%15.15s ",file.getFormatedSize(this.sizeScale, this.sizePower));
			}
				
			out.printf("%s",file.getFormatedName(this.quoting));
			out.printf("%s",file.getFileIndicator(this.fileIndicator));
			out.printf("%n");
		}
	}
	
	public void verticalView(String widthParam, PrintWriter out) throws IOException
	{
		String width;
		int filePerCol=(this.filesList.size()/5) + 1;
		int[] noLimitWidth = this.verticalColumnWidth();
		FileInfo file;
		for(int i=0; i<filePerCol; i++)
		{
			for(int j=0; j<5; j++)
			{
				if(i+filePerCol*j > this.filesList.size()-1) break;
				
				file=this.filesList.get(i+filePerCol*j);
				if(this.allocatedSize)
				{
					//Minimum scale for allocatedSize is KB
					if(this.sizeScale.equals("B"))
						out.printf("%15.15s ",file.getFormatedSize("K", this.sizePower));
					else
						out.printf("%15.15s ",file.getFormatedSize(this.sizeScale, this.sizePower));
				}
				
				if(widthParam.equals("0")) width = Integer.toString(noLimitWidth[j]);
				else width = widthParam;
				
				out.printf("%-"+width+"."+width+"s",
						file.getFormatedName(this.quoting)+file.getFileIndicator(this.fileIndicator));
			}

			out.printf("%n");
		}
		out.printf("%n");
	}
	
	public void horizontalView(String widthParam, PrintWriter out) throws IOException
	{
		int counter=0;
		int[] noLimitWidth = horizontalColumnWidth();
		String width;
		for(FileInfo file : this.filesList)
		{
			if(this.allocatedSize)
			{
				//Minimum scale for allocatedSize is KB
				if(this.sizeScale.equals("B"))
					out.printf("%15.15s ",file.getFormatedSize("K", this.sizePower));
				else
					out.printf("%15.15s ",file.getFormatedSize(this.sizeScale, this.sizePower));
			}
			
			if(widthParam.equals("0")) width = Integer.toString(noLimitWidth[counter%5]);
			else width = widthParam;
			
			out.printf("%-"+width+"."+width+"s",
					file.getFormatedName(this.quoting)+file.getFileIndicator(this.fileIndicator));
		
			if(counter%5==4) out.printf("%n");
			
			counter++;
		}
		out.printf("%n");
	}
	
	public void commaView(PrintWriter out) throws IOException
	{
		int counter=1;
		int blockSize;
		for(FileInfo file: this.filesList)
		{
			if(this.allocatedSize)
			{
				//Minimum scale for allocatedSize is KB
				if(this.sizeScale.equals("B"))
				{
					blockSize=file.getFormatedSize("K", this.sizePower).length();
					out.printf("%"+blockSize+"."+blockSize+"s ",file.getFormatedSize("K", this.sizePower));
				}
				else
				{
					blockSize=file.getFormatedSize(this.sizeScale, this.sizePower).length();
					out.printf("%"+blockSize+"."+blockSize+"s ",file.getFormatedSize(this.sizeScale, this.sizePower));
				}
			}
			
			out.printf("%s",file.getFormatedName(this.quoting));
			out.printf("%s",file.getFileIndicator(this.fileIndicator));
			
			if(counter<this.filesList.size()) out.printf(", ");
			
			counter++;
		}
		out.printf("%n");
	}
	/******************************************************************************************************************************/
	
	
	/******************************************************UTIL-FUNCTIONS**********************************************************/
	public int[] verticalColumnWidth()
	{
		int filePerCol=(this.filesList.size()/5) + 1;
		int[] width = {0,0,0,0,0};
		for(int i=0; i<filePerCol; i++)
		{
			for(int j=0; j<5; j++)
			{
				if(i+filePerCol*j > this.filesList.size()-1) break;
				if(this.filesList.get(i+filePerCol*j).getName().length() > width[j])
					width[j] = this.filesList.get(i+filePerCol*j).getName().length();
			}
		}
		
		width[0] +=3;
		width[1] +=3;
		width[2] +=3;
		width[3] +=3;
		width[4] +=3;
		
		return width;
	}
	
	public int[] horizontalColumnWidth()
	{
		int counter=0;
		int[] width = {0,0,0,0,0};
		for(FileInfo file : this.filesList)
		{
			if(file.getName().length() > width[counter%5]) width[counter%5] = file.getName().length();
			counter++;
		}
		
		width[0] +=3;
		width[1] +=3;
		width[2] +=3;
		width[3] +=3;
		width[4] +=3;
		
		return width;
	}

	public void detailedLine(FileInfo file, PrintWriter out) throws IOException, ParseException
	{
		if(this.allocatedSize)
		{
			//Minimum scale for allocatedSize is KB
			if(this.sizeScale.equals("B"))
				out.printf("%15.15s ",file.getFormatedSize("K", this.sizePower));
			else
				out.printf("%15.15s ",file.getFormatedSize(this.sizeScale, this.sizePower));
		}
		
		out.printf("%-12.12s ",file.getFilePermissions());
		
		if(!this.noOwner)
			out.printf("%-20.20s ",file.getOwner());
		
		if(!this.noGroup)
			out.printf("%-8.8s ",file.getGroup());
		
		if(!this.noAuthor)
			out.printf("%-20.20s ",file.getOwner());
		out.printf("%15.15s ",file.getFormatedSize(this.sizeScale, this.sizePower));
		
		if(this.showAccessTime)
			out.printf("%14.14s ",file.getLastAccessed());
		else
		{
			if(!this.isoTime)
				out.printf("%14.14s ",file.getLastModified());
			else
				out.printf("%20.20s ",file.getLastModifiedISO());
		}
		
		out.printf("%s",file.getFormatedName(this.quoting));
		out.printf("%s",file.getFileIndicator(this.fileIndicator));
		if(file.isSymLink())
			out.printf(" -> %s",file.getSymLink());
		
		out.printf("%n");
	}
	/******************************************************************************************************************************/
}
