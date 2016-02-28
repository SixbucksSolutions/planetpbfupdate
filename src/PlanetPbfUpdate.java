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
			//long pbfSequenceNumber = pbfParser.getSequenceNumber();
			//System.out.println("PBF sequence number: " + pbfSequenceNumber );

			//String replicationBaseUrl = pbfParser.getReplicationBaseUrl();
			//System.out.println("Base URL: " + replicationBaseUrl );

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

		System.out.println( "         File timestamp: " +
				isoFormat.format( pbfTimestamp.getTime()) );

		// Find timestamps of latest hourly/minute/day files
		getLatestUpdates();
	}

	protected static void getLatestUpdates()
	{
		URL updateFile;
		PlanetUpdateStateData minuteUpdates;
		PlanetUpdateStateData hourUpdates;
		PlanetUpdateStateData dayUpdates;


		try {
			updateFile = new URL(
				"http://planet.openstreetmap.org/replication/minute/state.txt");
			minuteUpdates = new PlanetUpdateStateData( updateFile );
    		System.out.println("Minute update timestamp: " +
            	minuteUpdates.getTimestampString() );

            updateFile = new URL(
                "http://planet.openstreetmap.org/replication/hour/state.txt");
            hourUpdates = new PlanetUpdateStateData( updateFile );
            System.out.println("  Hour update timestamp: " +
                hourUpdates.getTimestampString() );

            updateFile = new URL(
                "http://planet.openstreetmap.org/replication/day/state.txt");
            dayUpdates = new PlanetUpdateStateData( updateFile );
            System.out.println("   Day update timestamp: " +
                dayUpdates.getTimestampString() );

			getMinuteUpdates(minuteUpdates, hourUpdates);
			//getHourUpdates();
			//getDayUpdates();
		}
		catch ( MalformedURLException e ) 
		{
			System.err.println("Bad URL for update state file"); 
		}
	}

	protected static void getMinuteUpdates(
		PlanetUpdateStateData minuteUpdates,
		PlanetUpdateStateData hourUpdates )
	{
		// Find out how many minutes worth of data are needed
		int numMinutes = minuteUpdates.getTimestamp().get(Calendar.MINUTE) - 
			hourUpdates.getTimestamp().get(Calendar.MINUTE);

		System.out.println( "Need to get " + numMinutes + " minute files between " +
			minuteUpdates.getTimestampString() + " and " + hourUpdates.getTimestampString() );

		System.out.println("Starting sequence number: " + minuteUpdates.getSequenceNumber() );

		long endingSequenceNumber = minuteUpdates.getSequenceNumber() - numMinutes;
		System.out.println("  Ending sequence number: " + endingSequenceNumber );

		try
		{
			getUpdates( new URL("http://planet.openstreetmap.org/replication/minute"), 
				minuteUpdates.getSequenceNumber(), endingSequenceNumber );
		}
		catch ( MalformedURLException e ) 
		{
			System.err.println("Invalid URL: " + e.toString() );
		}
	}

	protected static void getUpdates(
		URL 	baseUrl,
		long	startingSequenceNumber,
		long	endingSequenceNumber )
	{
		for ( 
			long currSequenceNumber = startingSequenceNumber;
			currSequenceNumber >= endingSequenceNumber;
			--currSequenceNumber )
		{

			// Build directory string right to left
			long directoryPortion = currSequenceNumber / 1000;
			String fileRelativeDir = new String("");
			while ( directoryPortion > 0 )
			{
				fileRelativeDir = String.format("%03d/", directoryPortion % 1000) + fileRelativeDir;
				directoryPortion /= 1000;
			}

            // Get filename (last three digits)
            String filename = String.format( "%03d.osc.gz", currSequenceNumber % 1000 );

			System.out.println("Full path to current file: " +
				baseUrl.toString() + "/" + fileRelativeDir + filename );
		}
	}
}
