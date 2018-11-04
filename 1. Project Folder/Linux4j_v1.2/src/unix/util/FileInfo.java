package unix.util;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

public class FileInfo
{
	private File file;
	private String name;
	private String absolutePath;
	private String size;
	private Date lastModified;
	private Date lastAccessed;
	private String owner;
	private String group;
	private String filePermissions;
	
	public FileInfo(File file) {
		
		this.file = file;
		
		this.name = file.getName();
		
		this.absolutePath = file.getAbsolutePath();
		this.size = Long.toString(file.length());
		this.lastModified = new Date(file.lastModified());

		Path path = Paths.get(this.absolutePath);
		BasicFileAttributes attrs=null;
		try {
			attrs = Files.readAttributes(path, BasicFileAttributes.class);
			this.lastAccessed = new Date(attrs.lastAccessTime().toMillis());
		} catch (IOException e1) { this.lastAccessed=this.lastModified;}

		
		String username="SYSTEM";
		try {
		    UserPrincipal owner = Files.getOwner(path, LinkOption.NOFOLLOW_LINKS);
		    try { 
		    	username = owner.getName().split("\\\\")[1];
		    } catch (Exception e) {
		    	username = owner.getName();
		    }
		} catch (IOException e) {}
		this.owner = username;
		
		//Group set Static because it can't be retrieved in windows OS, We can get it using POSIX which is supported by Unix-OS
		if(this.owner.equals("SYSTEM")) this.group = this.owner;
		else this.group = "None";
		
		final boolean r = file.canRead();
		final boolean w = file.canWrite();
		final boolean x = file.canExecute();
		this.filePermissions = (file.isDirectory() ? "d" : "-") +
				//owner
				(r ? 'r' : '-') +
				(w ? 'w' : '-') +
				(x ? 'x' : '-') +
				//group
				(r ? '.' : '-') +
				(w ? '.' : '-') +
				(x ? '.' : '-') +
				//other
				(r ? '.' : '-') +
				(w ? '.' : '-') +
				(x ? '.' : '-');
	}
	
	public String getName()
	{
		return name;
	}
	public String getFormatedName(String quotingStyle) 
	{
		//For files name with a space use single-quotes
		if( (quotingStyle.equals("shell") || quotingStyle.equals("shell-escape")) && file.getName().indexOf(" ") > -1) 
			return "'"+file.getName()+"'";

		if(quotingStyle.equals("locale") || quotingStyle.equals("shell-always") || quotingStyle.equals("shell-escape-always")) 
			return "'"+file.getName()+"'";
		
		if(quotingStyle.equals("clocale") || quotingStyle.equals("c")) 
			return '"'+file.getName()+'"';
		
		if(quotingStyle.equals("escape"))
			return file.getName().replace(" ", "\\ ");
		
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public String getSize() {
		if (file.isDirectory()) return "0";
		return this.size;
	}
	
	public String getFormatedSize(String sizeScale, int sizePower) {
		//Directories show 0 in size
		if (file.isDirectory()) return "0";
		
		//Format size to Human Readable format
		ArrayList<String> sizes = new ArrayList<String>(Arrays.asList(new String [] {"B", "K", "M", "G", "T"}));
		
		double len = Double.valueOf(this.size);
		int order = 0;
		while ( (len >= sizePower || sizeScale!=null) 
				&& order < sizes.size()- 1 
				&& ( sizeScale==null || (sizeScale!=null && !sizeScale.equals(sizes.get(order)) ) ) )
		{
			//if(sizeScale.equals(sizes[order])) break;
		    len = len/sizePower;
		    order++;
		}
		if(sizes.get(order).equals("B")) return String.format("%.0f", len);
		else return String.format("%.1f%s", len, sizes.get(order));
}

	public void setSize(String size) {
		this.size = size;
	}

	public String getLastModified() {
		return new SimpleDateFormat("MMM dd HH:mm").format(this.lastModified);
	}
	public String getLastModifiedISO() throws ParseException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		return df.format(this.lastModified);
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	
	public String getLastAccessed() {
		return new SimpleDateFormat("MMM dd HH:mm").format(lastAccessed);
	}

	public void setLastAccessed(Date lastAccessed) {
		this.lastAccessed = lastAccessed;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getFilePermissions() {
		return filePermissions;
	}

	public void setFilePermissions(String filePermissions) {
		this.filePermissions = filePermissions;
	}

	public String getFileIndicator(String indicatorStyle) throws IOException
	{
		try
		{
			//Symbolic Link [shortcuts]
			if(indicatorStyle.equals("classify") || indicatorStyle.equals("file-type"))
				if( !file.getAbsolutePath().equals(file.toPath().toRealPath().toString()) ) return "@";
			

			//Directories
			if(indicatorStyle.equals("classify") || indicatorStyle.equals("file-type") || indicatorStyle.equals("slash"))
				if(file.isDirectory()) return "/";
			
			//Executables
			if(!indicatorStyle.equals("none") && !indicatorStyle.equals("file-type") && !indicatorStyle.equals("slash"))
				return "*";
		}
		catch(Exception e) {}
		
		return "";
		
		/*
		 * Other types are available, but java doesn't offer the tools to get them, 
		 * and most of them are more specific to UNIX-OS
		 */
	}
	
	public Boolean isSymLink() throws IOException
	{
		try
		{
			if(!file.getAbsolutePath().equals(file.toPath().toRealPath().toString())) return true;
		}
		catch(Exception e) {return false;}
		
		return false;
	}
	
	public String getSymLink() throws IOException
	{
		if(this.isSymLink()) return file.toPath().toRealPath().toString();
		return "";
	}
	
	public String getExtension()
	{
		if(this.file.isDirectory()) return "";
		else
		{
			String[] name = this.name.split("\\.");
			if(name.length>0) 
			{
				return name[name.length - 1];
			}
		}
		return "";
	}
	
	public Boolean isDirectory()
	{
		return this.file.isDirectory();
	}

	
	public static Comparator<FileInfo> fileModificationTimeComparator = new Comparator<FileInfo>() 
	{

		public int compare(FileInfo f1, FileInfo f2) 
		{
		   Date fileDate1 = null;
		   Date fileDate2 = null;
		   try {
				fileDate1 = new SimpleDateFormat("MMM dd HH:mm").parse(f1.getLastModified());
				fileDate2 = new SimpleDateFormat("MMM dd HH:mm").parse(f2.getLastModified());
		   } catch (ParseException e) {e.printStackTrace();}
		   

		   return fileDate2.compareTo(fileDate1);
	    }
	};
	
	public static Comparator<FileInfo> fileAccessTimeComparator = new Comparator<FileInfo>() 
	{

		public int compare(FileInfo f1, FileInfo f2) 
		{
		   Date fileDate1 = null;
		   Date fileDate2 = null;
		   try {
				fileDate1 = new SimpleDateFormat("MMM dd HH:mm").parse(f1.getLastAccessed());
				fileDate2 = new SimpleDateFormat("MMM dd HH:mm").parse(f2.getLastAccessed());
		   } catch (ParseException e) {e.printStackTrace();}
		   

		   return fileDate2.compareTo(fileDate1);
	    }
	};
	
	public static Comparator<FileInfo> fileSizeComparator = new Comparator<FileInfo>() 
	{

		public int compare(FileInfo f1, FileInfo f2) 
		{
		   int fileSize1 = Integer.parseInt(f1.getSize());
		   int fileSize2 = Integer.parseInt(f2.getSize());
		   

		   return fileSize2-fileSize1;
	    }
	};
	
	public static Comparator<FileInfo> fileDirectoryComparator = new Comparator<FileInfo>() 
	{

		public int compare(FileInfo f1, FileInfo f2) 
		{
			return f2.isDirectory().compareTo(f1.isDirectory());
	    }
	};
	
	public static Comparator<FileInfo> fileExtensionComparator = new Comparator<FileInfo>() 
	{
		public int compare(FileInfo f1, FileInfo f2) 
		{
		   return f1.getExtension().compareTo(f2.getExtension());
	    }
	};
}
