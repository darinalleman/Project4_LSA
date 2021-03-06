package FileSystemApp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * Server class
 * @author Andrew Corchado, Darin Alleman, Brad Olah
 */
class FileSystemImpl extends FileSystemPOA
{
	private ORB orb;
	private ArrayList<ArrayList<String>> allServers = new ArrayList<ArrayList<String>>();
	private ArrayList<File> files = new ArrayList<File>();
	FileSystem newFileSystem;
	File file;

	public void setORB(ORB orb_val)
	{
		orb = orb_val;
	}

	/**
	 * Add our server ports/hostnames to the ArrayList
	 */
	public void setServers()
	{
		ArrayList<String> server = new ArrayList<String>();
		server.add("1056");
		server.add("1057");
		server.add("clipper.ship.edu");

		allServers.add(server);
		server.clear();

		server.add("1056");
		server.add("1057");
		server.add("54.164.211.108");		//Virginia server

		allServers.add(server);
		server.clear();

		server.add("1056");
		server.add("1057");
		server.add("54.91.209.99");			//Japan server

		allServers.add(server);
		server.clear();

		server.add("1056");
		server.add("1057");
		server.add("52.23.242.80");			//Ireland server

		allServers.add(server);
		server.clear();
	}

	// implement shutdown() method - we probably won't call this
	public void shutdown()
	{
		orb.shutdown(false);
	}

//	@Override
//	public String readFile(String title)
//	{
//		try
//		{
//			Scanner s = new Scanner(new File(title));
//			StringBuffer contents = new StringBuffer("");
//			while (s.hasNext())
//			{
//				contents.append(s.nextLine() + "\n");
//			}
//
//			s.close();
//			return contents.toString();
//		} catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//		}
//		return null;
//	}

	/**
	 * Finding the file
	 */
	@Override
	public boolean findFile(String title)
	{
		boolean local = false;

		//check to see if we have already added the file to our ArrayList
		for(File file : files)
		{
			if(file.getName().equals(title + ".txt"))
			{
				return true;
			}
		}
		if(local = hasFile(title))	//check to see if the file is local
		{
			return !local;
		}
		if(local)
		{
			System.out.println("File not local. Asking other servers...");
			if (otherServerHasFile()) //check to see if other servers have the file
			{
				return true;
			}
		}
		return false;	//if we can't find the file at all
	}

	/**
	 * Check to see if we have the file on this server
	 */
	@Override
	public boolean hasFile(String title)
	{
		if(title == null)
		{
			return false;
		}
		else
		{
			file = new File(title + ".txt");
			if(file.isFile())
			{
				files.add(file);
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the record that the client wants to modify
	 */
	@Override
	public String getRecord(int num)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));

			for(int i = 0; i < num; i++)
			{
				br.readLine();
			}

			String record = br.readLine();
			System.out.println(record);
			br.close();
			return record;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method to check if other servers have the file we're looking for
	 */
	@Override
	public boolean otherServerHasFile()
	{
		for (ArrayList<String> oneServer : allServers)
		{
			String[] args = {"orbd", "-ORBInitialPort", oneServer.get(0), "-port", oneServer.get(1), "-ORBInitialHost", oneServer.get(2)};
			ORB orb = ORB.init(args, null);

			org.omg.CORBA.Object objRef = null;
			try
			{
				objRef = orb.resolve_initial_references("NameService");
			}
			catch (InvalidName e)
			{
				e.printStackTrace();
			}

			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			String name = "FileSystem";
			try
			{
				newFileSystem = FileSystemHelper.narrow(ncRef.resolve_str(name));
			}
			catch (NotFound | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName e)
			{
				e.printStackTrace();
			}
			if (newFileSystem.hasFile(file.getName()))
			{
				files.add(file);
				return true;
			}
		}
		return false;
	}

	/**
	 * Server with the file sends it back to the client
	 */
	@Override
	public String otherServerSendsFile(String title)
	{
		String fileText = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(title));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			while(br.readLine() != null)
			{
				fileText += br.readLine();
			}
			br.close();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		return fileText;
	}

	/**
	 * Writes the file out to the local copy
	 */
	@Override
	public void writeFile(String title)
	{
		String text = newFileSystem.otherServerSendsFile(title);
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(title));

			bw.write(text);
			bw.close();
			closeFile(title);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Remove/close the file when we are finished with it.
	 */
	@Override
	public void closeFile(String title)
	{
		File temp = new File(title + ".txt");
		files.remove(temp);

		for (ArrayList<String> oneServer : allServers)
		{
			String[] args = {"orbd", "-ORBInitialPort", oneServer.get(0), "-port", oneServer.get(1), "-ORBInitialHost", oneServer.get(2)};
			ORB orb = ORB.init(args, null);

			org.omg.CORBA.Object objRef = null;
			try
			{
				objRef = orb.resolve_initial_references("NameService");
			}
			catch (InvalidName e)
			{
				e.printStackTrace();
			}

			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			String name = "FileSystem";
			try
			{
				newFileSystem = FileSystemHelper.narrow(ncRef.resolve_str(name));
			}
			catch (NotFound | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName e)
			{
				e.printStackTrace();
			}
			if (newFileSystem.hasFile(file.getName()))
			{
				newFileSystem.deleteFile(file.getName());
			}
		}
	}

	/**
	 * Delete the file when we're finished
	 */
	@Override
	public void deleteFile(String title)
	{
		File file = new File(title + ".txt");
		file.delete();
	}

	@Override
	public void saveRecordToFile(String record, int recordNum, String title)
	{
		for (File f : files)
		{
			if (f.getName().equals(title + ".txt"))
			{
				try
				{
					File temp = new File(title + ".tmp");
					BufferedReader br = new BufferedReader(new FileReader(f));
					BufferedWriter bw = new BufferedWriter(new FileWriter(temp));

					//until we get to the record that we need to change
					for (int i = 0; i < recordNum; i++)
					{
						if (br.readLine() != null)
						{
							//simply write those lines to the temp file
							bw.write(br.readLine());
						}
					}

					//after that for loop we will be at the record we need to change
					bw.write(record);

					//read the line of the record we changed in the old file to skip over it
					br.readLine();

					//continue reading lines from the old file and copying them to the new file
					while (br.readLine() != null)
					{
						bw.write(br.readLine());
					}

					files.remove(f);								// remove the file when we're done
					f.delete();										// delete the file
					temp.renameTo(new File(title + ".txt"));		// rename temp to original name
					br.close();
					bw.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

	}
}

/**
 * This is the class that runs on the server
 * @author merlin
 *
 */
public class FileSystemServer
{

	/**
	 * @param args ignored
	 */
	public static void main(String args[])
	{
		try
		{
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			FileSystemImpl fileSystemImpl = new FileSystemImpl();
			fileSystemImpl.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(fileSystemImpl);
			FileSystem href = FileSystemHelper.narrow(ref);

			// get the root naming context
			// NameService invokes the name service
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExt which is part of the Interoperable
			// Naming Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// bind the Object Reference in Naming
			String name = "FileSystem";
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, href);

			System.out.println("FileSystemServer ready and waiting ...");

			// wait for invocations from clients
			orb.run();
		}

		catch (Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println("FileSystemServer Exiting ...");

	}
}
