package ut.uk.ac.ucl.crest.inftracker;

import org.junit.Test;
import uk.ac.ucl.crest.inftracker.api.MyPluginComponent;
import uk.ac.ucl.crest.inftracker.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}