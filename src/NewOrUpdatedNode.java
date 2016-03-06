package planetpbfupdate;

import crosby.binary.*;

public class NewOrUpdatedNode
{
    public NewOrUpdatedNode(
        Osmformat.Node  baseNode )
    {
        this(baseNode, new String());
    }

    public NewOrUpdatedNode(
        Osmformat.Node  baseNode,
        String          username )
    {
        nodeData = baseNode;
        this.username = username;
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

    protected String            username;
    protected Osmformat.Node    nodeData;


}
