import crosby.binary.*;
import crosby.binary.Osmformat.*;
import crosby.binary.file.*;

import java.util.List;
import java.io.*;

public class PlanetPbfUpdate
{
   	public static void main(String[] args)
	{
		//System.out.println("Booya");
		BlockReaderAdapter blockReader = new PbfParser();
		InputStream input;
		try
		{
			input = new FileInputStream("/home/tdo/tmp/pbf/monaco-latest.osm.pbf");
			new BlockInputStream(input, blockReader).process();
		} 
		catch ( FileNotFoundException e )
		{
			System.out.println("File not found: " + e);
		}
		catch ( IOException e )
		{
			System.out.println("IO error when processing data");
		}
	}

	public static class PbfParser implements BlockReaderAdapter
	{
		public boolean skipBlock(FileBlockPosition message) {
			return true;
		}

		public void handleBlock(FileBlock message)	{
			;
		}

        public void complete() {
            System.out.println("Complete!");
        }
	}
	


}

