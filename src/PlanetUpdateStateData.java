package planetpbfupdate;

import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.TimeZone;

import java.net.*;
import java.io.*;

public class PlanetUpdateStateData
{
	protected long 		sequenceNumber;
	protected Calendar	stateTimestamp; 
	protected String	stateTimestampString;

	public PlanetUpdateStateData(URL netLink)
	{
		try
		{
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(netLink.openStream()));

			String inputLine;
			while ( (inputLine = reader.readLine()) != null )
			{
				String[] keyValue = inputLine.split("=", 2);

				if ( keyValue[0].equals("sequenceNumber") == true )
				{
					sequenceNumber = Long.parseLong( keyValue[1], 10 );
				}
				else if ( keyValue[0].equals("timestamp") == true )
				{
					DateFormat format = new SimpleDateFormat( "yyyy-mm-dd'T'HH\\:mm\\:ss'Z'");
					stateTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					stateTimestamp.setTime( format.parse(keyValue[1]));
					stateTimestamp.add(Calendar.MONTH, 1);

       				SimpleDateFormat isoFormat =
            			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					stateTimestampString = isoFormat.format( stateTimestamp.getTime() );

					//System.out.println("       Raw date: " + keyValue[1] );
					//System.out.println(" Converted date: " + stateTimestampString );
				}
			}
			reader.close();
		} 
		catch ( IOException e )
		{
			System.err.println("IOException when creating state data object");
		}
		catch ( ParseException e )
		{	
			System.err.println("Parsing error when creating state data object");
		}
	}

	public long getSequenceNumber()
	{
		return sequenceNumber;
	}

	public Calendar getTimestamp()
	{
		return stateTimestamp;
	}

	public String getTimestampString()
	{
		return stateTimestampString;
	}
}