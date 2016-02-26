import crosby.binary.*;
import crosby.binary.Osmformat.*;
import crosby.binary.file.*;

import java.util.List;
import java.io.*;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

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
			System.out.println("skipBlock called on " + message.getType());

			if ( message.getType().equals("OSMHeader") )
			{
				return false;
			}
			else
			{
				return true;
			}
		}

		public void handleBlock(FileBlock message)	{
			System.out.println("Got a fileblock");

			if ( message.getType().equals("OSMHeader") == true )
			{
				System.out.println("Got header in handleblock");

				try
				{
					Osmformat.HeaderBlock headerBlock = 
						Osmformat.HeaderBlock.parseFrom(message.getData());	

					if ( headerBlock.hasOsmosisReplicationTimestamp() == true )
					{
						long osmosisTimestamp = 
							headerBlock.getOsmosisReplicationTimestamp();

						/*
						System.out.println("File timestamp: " + 
							osmosisTimestamp );
						*/
						Calendar calendar = Calendar.getInstance();
						calendar.setTime( new Date(osmosisTimestamp * 1000) );
						calendar.add(Calendar.DATE, 1);

						SimpleDateFormat isoFormat = 
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

						System.out.println( "File timestamp: " + 
							isoFormat.format( calendar.getTime() ) );

					}
					else
					{
						System.out.println("No timestamp");
					}
				} 
				catch ( InvalidProtocolBufferException e )
				{
					System.out.println("Invalid protocol buffer");
				}
			}
		}

        public void complete() {
            System.out.println("Complete!");
        }
	}
	


}

