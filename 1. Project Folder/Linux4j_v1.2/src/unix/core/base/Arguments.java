package unix.core.base;

/*
 * Arguments
 * This interface is made to define a common interface to be implemented by Arguments of each COMMAND,
 * this way the CommandParser will only undergo a minor change when it comes to adding other commands
 */
public interface Arguments {
	//SpecificArguments that implements this interface should have an OutFile [null for default=console]
	public String getOutFile();

}
