package FileSystemApp;

import org.omg.CosNaming.*;

import java.util.Scanner;

import org.omg.CORBA.*;

/**
 * A simple client that just gets a
 * @author Merlin
 *
 */
public class FileSystemClient
{
	static FileSystem fileSystemImpl;

	/**
	 * Just do each operation once
	 * @param args ignored
	 */


	public static void main(String args[])
	{
		try
		{
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// get the root naming context
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExt instead of NamingContext. This is
			// part of the Interoperable naming Service.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// resolve the Object Reference in Naming
			String name = "FileSystem";
			fileSystemImpl = FileSystemHelper.narrow(ncRef.resolve_str(name));

			String action = "";
			Scanner input = new Scanner(System.in);

			while(!action.equalsIgnoreCase("Quit"))
			{

			}
			input.close();

		} catch (Exception e)
		{
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}
	}

}
