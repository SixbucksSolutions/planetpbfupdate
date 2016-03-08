package planetpbfupdate;

import crosby.binary.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class ChangeManager
{
    ChangeManager()
    {
        modifiedPrimitives = new
        HashMap<NewOrUpdatedPrimitive.PrimitiveType, Map<Long, NewOrUpdatedPrimitive>>();

        modifiedPrimitives.put(NewOrUpdatedPrimitive.PrimitiveType.NODE,
                               new HashMap<Long, NewOrUpdatedPrimitive>());

        modifiedPrimitives.put(NewOrUpdatedPrimitive.PrimitiveType.WAY,
                               new HashMap<Long, NewOrUpdatedPrimitive>());

        modifiedPrimitives.put(NewOrUpdatedPrimitive.PrimitiveType.RELATION,
                               new HashMap<Long, NewOrUpdatedPrimitive>());

        primitivesSeen = new
        HashMap<NewOrUpdatedPrimitive.PrimitiveType, Set<Long>>();

        primitivesSeen.put(NewOrUpdatedPrimitive.PrimitiveType.NODE,
                           new HashSet<Long>() );

        primitivesSeen.put(NewOrUpdatedPrimitive.PrimitiveType.WAY,
                           new HashSet<Long>() );

        primitivesSeen.put(NewOrUpdatedPrimitive.PrimitiveType.RELATION,
                           new HashSet<Long>() );

    }

    public void insertModify(
        NewOrUpdatedPrimitive   modifiedPrimitive )
    {
        ;
    }

    protected Map < NewOrUpdatedPrimitive.PrimitiveType,
              Map<Long, NewOrUpdatedPrimitive >> modifiedPrimitives;

    protected Map<NewOrUpdatedPrimitive.PrimitiveType, Set<Long>>
    primitivesSeen;
}
