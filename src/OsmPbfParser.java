package planetpbfupdate;

import java.io.*;
import crosby.binary.file.*;
import crosby.binary.*;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.TimeZone;

public class OsmPbfParser implements BlockReaderAdapter
{
	protected BlockInputStream 	blockStream;
	protected Calendar 			pbfTimestamp;

	public OsmPbfParser(String filename) throws FileNotFoundException
	{
		InputStream input = new FileInputStream(filename);

		blockStream = new BlockInputStream(input, this);
	}

	public Calendar getPbfTimestamp()
	{
		try 
		{
			blockStream.process();
		}
		catch ( IOException e )
		{
			System.out.println("IO exception when processing blocks");
		}

		return pbfTimestamp;
	}

	

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
					TimeZone currTz = Calendar.getInstance().getTimeZone();
					long osmosisTimestamp = 
						headerBlock.getOsmosisReplicationTimestamp();
					int offsetFromUTC = currTz.getOffset(osmosisTimestamp);

					pbfTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					pbfTimestamp.setTime( new Date(osmosisTimestamp * 1000) );
					// Subtract offset to get UTC
					pbfTimestamp.add(Calendar.MILLISECOND, -offsetFromUTC);
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
	
