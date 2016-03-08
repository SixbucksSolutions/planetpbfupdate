package planetpbfupdate;

import crosby.binary.*;
import java.util.Map;

public class NewOrUpdatedPrimitive
{
    public enum PrimitiveType
    {
        NODE,
        WAY,
        RELATION
    }

    public NewOrUpdatedPrimitive(
        Osmformat.Node          baseNode,
        String                  username,
        Map<String, String>     nodeTags )
    {
        primitiveType = PrimitiveType.NODE;
        nodeData = baseNode;
        setUsername(username);
        setTags(nodeTags);
    }

    public NewOrUpdatedPrimitive(
        Osmformat.Way           baseWay,
        String                  username,
        Map<String, String>     tags )
    {
        primitiveType = PrimitiveType.WAY;
        wayData = baseWay;
        setUsername(username);
        setTags(tags);
    }

    public NewOrUpdatedPrimitive(
        Osmformat.Relation      baseRelation,
        String                  username,
        Map<String, String>     tags )
    {
        primitiveType = PrimitiveType.RELATION;
        relationData = baseRelation;
        setUsername(username);
        setTags(tags);
    }

    protected void setTags(
        Map<String, String> tags )
    {
        primitiveTags = tags;
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
        return primitiveTags;
    }

    protected PrimitiveType         primitiveType;
    protected String                username;
    protected Osmformat.Node        nodeData;
    protected Osmformat.Way         wayData;
    protected Osmformat.Relation    relationData;
    protected Map<String, String>   primitiveTags;

}
