package planetpbfupdate;

import crosby.binary.file.*;
import java.io.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.net.*;
import java.util.zip.GZIPInputStream;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

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

			//String replicationBinaseUrl = pbfParser.getReplicationBaseUrl();
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

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

		// Buffer for uncompressed data
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

			String fullPath = baseUrl.toString() + "/" + fileRelativeDir + filename;
			/*
			System.out.println("Full path to current file: " +
				baseUrl.toString() + "/" + fileRelativeDir + filename );
			*/

			// 16 MB read buffer
            final int uncompressedDataSize = 16 * 1024 * 1024;
            byte[] uncompressedData = new byte[ uncompressedDataSize ];
			int bytesToProcess;

			try
			{
				DocumentBuilder docBuilder = factory.newDocumentBuilder();

				try
				{
					InputStream urlInputStream = new URL(fullPath).openStream();
					InputStream inputStream = new GZIPInputStream( urlInputStream );
					Document osmChange = docBuilder.parse( inputStream );

					readOsmChange( osmChange.getFirstChild() );
				}
				catch ( MalformedURLException e ) 
				{
					System.err.println("URL exception with " + fullPath);
				}
				catch ( IOException e )
				{
					System.err.println("IO Exception with " + fullPath);
				}
				catch ( SAXException e )
				{
					System.err.println("SAX exception with " + fullPath );
				}
			}
			catch ( ParserConfigurationException e ) 
			{
				System.err.println("Could not configure XML parser");
			}

			break;	
		}
	}

	protected static void readOsmChange(
		Node osmChange )
	{
		// Make sure we're at sane starting point
		if ( osmChange.getNodeName().equals("osmChange") == false )
		{
			System.err.println("Did not start reading osmchanges at correct level");
			return;
		}

		// Make sure we have a known version
		if ( osmChange.hasAttributes() == false )
		{
			System.err.println("OSM changes does not have attributes, cannot check version");
			return;
		}

		NamedNodeMap attributes = osmChange.getAttributes();
		Node osmChangeVersion = attributes.getNamedItem("version");

		if ( osmChangeVersion == null )
		{
			System.out.println("OSM changes does not have version attribute");
			return;
		}

		if ( osmChangeVersion.getTextContent().equals("0.6") == false )
		{
			System.out.println("Unknown osmChange version: " + osmChangeVersion.getTextContent() );
			return;
		}

		System.out.println("Confirmed we are parsing an osmChange v0.6 XML document");

		int siblings = 0;

		Node currNode = osmChange.getNextSibling();

		while ( currNode != null )
		{
			siblings++;
			currNode = currNode.getNextSibling();
		}

		System.out.println("Number of siblings to osmchange: " + siblings );
			
		
		System.out.println("Children of osmChange: " + osmChange.getChildNodes().getLength() );

		for ( 
			currNode = osmChange.getFirstChild();
			currNode != null; 
			currNode = currNode.getNextSibling() )
		{
			if ( (currNode instanceof Text) && (currNode.getNodeValue().trim().equals("") == true) )
			{
				continue;
			}
			
			final String currNodeName = currNode.getNodeName();
			if ( currNodeName.equals("modify") == true )
			{
				System.out.println("Found modify node");
			}
			else if ( currNodeName.equals("delete") == true )
			{
				System.out.println("Found delete node");
			}
			else if ( currNodeName.equals("create") == true )
			{
				System.out.println("Found create node");
			}
			else
			{
				System.out.println("Unexpected node name: " + currNodeName );

				// What can you tell us about current?
				if ( currNode.hasChildNodes() == true )
				{
					System.out.println("\tHas child nodes");
				}
			}
		}
	}
}
