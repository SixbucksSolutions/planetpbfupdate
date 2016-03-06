package planetpbfupdate;

import crosby.binary.*;
import java.util.HashMap;

public class NewOrUpdatedNode
{
    public NewOrUpdatedNode(
        Osmformat.Node  baseNode )
    {
        this(baseNode, new String(), new HashMap());
    }

    public NewOrUpdatedNode(
        Osmformat.Node  baseNode,
        String          username )
    {
        this(baseNode, username, new HashMap() );
    }

    public NewOrUpdatedNode(
        Osmformat.Node  baseNode,
        String          username,
        HashMap         nodeTags )
    {
        nodeData = baseNode;
        this.username = username;
        this.nodeTags = nodeTags;
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

    public HashMap getTags()
    {
        return nodeTags;
    }

    protected String            username;
    protected Osmformat.Node    nodeData;
    protected HashMap           nodeTags;

}
