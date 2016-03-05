package planetpbfupdate;

import crosby.binary.file.*;
import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.net.*;
import java.util.zip.GZIPInputStream;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import crosby.binary.*;
import java.text.DateFormat;
import java.util.TimeZone;
import java.text.ParseException;

public class PlanetPbfUpdate
{
    protected final static String supportedOsmChangeVersion = "0.6";

    public static void main(String[] args)
    {
        String filename;

        if ( args.length == 1 )
        {
            filename = args[0];
        }
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


        try
        {
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
                            minuteUpdates.getTimestampString() + " and " +
                            hourUpdates.getTimestampString() );

        System.out.println("Starting sequence number: " +
                           minuteUpdates.getSequenceNumber() );

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
        URL     baseUrl,
        long    startingSequenceNumber,
        long    endingSequenceNumber )
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
                fileRelativeDir = String.format("%03d/",
                                                directoryPortion % 1000) + fileRelativeDir;
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
                    InputStream inputStream = new GZIPInputStream(
                        new URL(fullPath).openStream() );
                    //System.out.println("Opening " + fullPath);
                    readOsmChange( docBuilder.parse(inputStream).getFirstChild() );
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

        if ( osmChangeVersion.getTextContent().equals(supportedOsmChangeVersion) ==
                false )
        {
            System.out.println("Unknown osmChange version: " +
                               osmChangeVersion.getTextContent() );
            return;
        }

        System.out.println("Confirmed osmChange version (v" +
                           supportedOsmChangeVersion + ")");

        int siblings = 0;

        Node currNode;

        for (
            currNode = osmChange.getFirstChild();
            currNode != null;
            currNode = currNode.getNextSibling() )
        {
            if ( (currNode.getNodeType() == Node.TEXT_NODE) &&
                    (currNode.getNodeValue().trim().equals("") == true) )
            {
                continue;
            }

            processChanges(currNode);
        }
    }

    protected static void processChanges(
        Node currNode )
    {
        final String currNodeName = currNode.getNodeName();

        if ( currNodeName.equals("modify") == true )
        {
            System.out.println("Found modify node");

            processModifySection(currNode);
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
                //System.out.println("\tHas child nodes");
            }
        }

    }

    protected static void processModifySection(
        Node sectionNode )
    {
        for (
            Node currNode = sectionNode.getFirstChild();
            currNode != null;
            currNode = currNode.getNextSibling() )
        {
            // Skip text nodes
            if ( currNode.getNodeType() == Node.TEXT_NODE )
            {
                continue;
            }

            final String modifyType = currNode.getNodeName();

            if ( modifyType.equals("node") == true )
            {
                NewOrUpdatedNode parsedNode = parseNode(currNode);
            }
        }
    }

    public static NewOrUpdatedNode parseNode(
        Node currNode )
    {
        // Get attributes for the new node
        if ( currNode.hasAttributes() == false )
        {
            System.err.println("Modify node section with no attrs, invalid");
            return null;
        }

        // Create node builder
        Osmformat.Node.Builder nodeBuilder = Osmformat.Node.newBuilder();

        // Create info builder for node info
        Osmformat.Info.Builder infoBuilder = Osmformat.Info.newBuilder();

        // See what attrs we got
        NamedNodeMap nodeAttributes = currNode.getAttributes();

        String username = new String();

        for ( int i = 0; i < nodeAttributes.getLength(); ++i )
        {
            Node currAttr = nodeAttributes.item(i);
            //System.out.println("Attribute name: " + currAttr.getNodeName() );
            //System.out.println("Attribute value: "    + currAttr.getNodeValue() );

            final String nodeAttrName = currAttr.getNodeName();

            if ( nodeAttrName.equals("changeset") == true )
            {
                infoBuilder.setChangeset(Long.parseLong(currAttr.getNodeValue()));
            }
            else if ( nodeAttrName.equals("id") == true )
            {
                nodeBuilder.setId(Long.parseLong(currAttr.getNodeValue()));
            }
            else if ( nodeAttrName.equals("lat") == true )
            {
                //System.out.println("Node lat: " + currAttr.getNodeValue());
                nodeBuilder.setLat(convertToNanodegrees(Double.parseDouble(
                        currAttr.getNodeValue())));

                /*
                   System.out.println("converted lat of " + currAttr.getNodeValue() + " to " +
                   nodeBuilder.getLat() );
                 */
            }
            else if ( nodeAttrName.equals("lon") == true )
            {
                nodeBuilder.setLon(convertToNanodegrees(Double.parseDouble(
                        currAttr.getNodeValue())));
            }
            else if ( nodeAttrName.equals("timestamp") == true )
            {
                //System.out.println("Timestamp: " + currAttr.getNodeValue() );
                DateFormat format = new SimpleDateFormat( "yyyy-MM-dd'T'HH':'mm':'ss'Z'");
                Calendar nodeTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

                try
                {
                    nodeTimestamp.setTime( format.parse(currAttr.getNodeValue()) );
                }
                catch ( ParseException e )
                {
                    System.err.println("Invalid date parse on " + currAttr.getNodeValue() +
                                       ":" + e );
                }

                nodeTimestamp.add(Calendar.MONTH, 1);

                TimeZone tz = TimeZone.getDefault();
                Calendar cal = GregorianCalendar.getInstance(tz);
                int offsetFromUTC = tz.getOffset(cal.getTimeInMillis());

                nodeTimestamp.add(Calendar.MILLISECOND, offsetFromUTC);


                /*
                   System.out.println("Got " +
                   nodeTimestamp.get(Calendar.YEAR) + "-" +
                   nodeTimestamp.get(Calendar.MONTH) + "-" +
                   nodeTimestamp.get(Calendar.DATE) + " " +
                   nodeTimestamp.get(Calendar.HOUR_OF_DAY) + ":" +
                   nodeTimestamp.get(Calendar.MINUTE) + ":" +
                   nodeTimestamp.get(Calendar.SECOND) + " for " +
                   currAttr.getNodeValue() );
                 */

                infoBuilder.setTimestamp(nodeTimestamp.getTimeInMillis());
            }
            else if ( nodeAttrName.equals("uid") == true )
            {
                infoBuilder.setUid(Integer.parseInt(currAttr.getNodeValue()));
            }
            else if ( nodeAttrName.equals("user") == true )
            {
                username = currAttr.getNodeValue();
            }
            else if ( nodeAttrName.equals("version") == true )
            {
                infoBuilder.setVersion(Integer.parseInt(currAttr.getNodeValue()));
            }
            else
            {
                System.out.println("Invalid attribute: " + nodeAttrName);
            }
        }

        nodeBuilder.setInfo(infoBuilder);

        NewOrUpdatedNode returnNode = new NewOrUpdatedNode(nodeBuilder.build(),
                username);

        return returnNode;
    }

    protected static long convertToNanodegrees(
        double fullDegrees )
    {
        return (long)(fullDegrees * 10000000);
    }
}
