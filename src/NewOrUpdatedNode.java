package planetpbfupdate;

import crosby.binary.*;
import java.util.Map;
import java.util.HashMap;

public class NewOrUpdatedNode
{
    public NewOrUpdatedNode(
        Osmformat.Node  baseNode )
    {
        this(baseNode, new String(), null);
    }

    public NewOrUpdatedNode(
        Osmformat.Node  baseNode,
        String          username )
    {
        this(baseNode, username, null);
    }

    public NewOrUpdatedNode(
        Osmformat.Node      baseNode,
        String              username,
        Map<String, String>  nodeTags )
    {
        nodeData = baseNode;
        this.username = username;

        if ( nodeTags != null )
        {
            this.nodeTags = nodeTags;
        }
        else
        {
            this.nodeTags = new HashMap<String, String>();
        }
    }

    public void setUsername(
        String editorUsername )
    {
        username = editorUsername;
    }

    public String getUsername()
    {
        return username;
    }

    public Map getTags()
    {
        return nodeTags;
    }

    protected String                username;
    protected Osmformat.Node        nodeData;
    protected Map<String, String>   nodeTags;

}
