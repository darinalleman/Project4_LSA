package FileSystemApp;


/**
* FileSystemApp/byteSeqHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from FileSystem.idl
* Monday, December 12, 2016 2:05:42 PM EST
*/

public final class byteSeqHolder implements org.omg.CORBA.portable.Streamable
{
  public byte value[] = null;

  public byteSeqHolder ()
  {
  }

  public byteSeqHolder (byte[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = FileSystemApp.byteSeqHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    FileSystemApp.byteSeqHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return FileSystemApp.byteSeqHelper.type ();
  }

}