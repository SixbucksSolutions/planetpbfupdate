package planetpbfupdate;

import crosby.binary.file.*;
import java.io.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.net.*;

public class PlanetPbfUpdate
{
   	public static void main(String[] args)
	{
		String filename;
		if ( args.length == 1 )
		{
			filename = args[0]; }
		else
		{
			System.err.println("No PBF file specified");
			return;
		}

		try
		{
			OsmPbfParser pbfParser = new OsmPbfParser( filename );
			System.out.println("Opened PBF: " + filename );
			Calendar pbfTimestamp = pbfParser.getPbfTimestamp();

			downloadOsmPlanetUpdates(pbfTimestamp);
		}
		catch ( FileNotFoundException e )
		{
			System.out.println("Could not open PBF file: " + filename );
		}
	}

	protected static void downloadOsmPlanetUpdates(
		Calendar pbfTimestamp )
	{
		SimpleDateFormat isoFormat =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		System.out.println( "File timestamp: " +
				isoFormat.format( pbfTimestamp.getTime()) );

		// Find timestamps of latest hourly/minute/day files
		getLatestUpdates();
	}

	protected static void getLatestUpdates()
	{
		URL updateFile;
		PlanetUpdateStateData minuteUpdates;

		try {
			updateFile = new URL(
				"http://planet.openstreetmap.org/replication/minute/state.txt");
			minuteUpdates = new PlanetUpdateStateData( updateFile );
    		System.out.println("Minute update sequence number: " +
            	minuteUpdates.getSequenceNumber() );

		}
		catch ( MalformedURLException e ) 
		{
			System.err.println("Bad URL for minutes"); 
		}
	}
}
