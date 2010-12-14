package abs.frontend.parser;

import org.junit.Test;

import abs.frontend.FrontendTest;
import static abs.ABSTest.Config.*;


public class IncompleteExpTests extends FrontendTest {
    
    @Test
    public void incompleteSyncAccess() {
        assertParseOk("{ x = x.; }", ALLOW_INCOMPLETE_EXPR); 
        assertParseOk("{ x.; }", ALLOW_INCOMPLETE_EXPR); 
    }

    @Test
    public void incompleteAsyncAccess() {
        assertParseOk("{ x = x!; }", ALLOW_INCOMPLETE_EXPR); 
        assertParseOk("{ x!; }", ALLOW_INCOMPLETE_EXPR); 
    }

    @Test
    public void incompleteNewExp() {
        assertParseOk("{ new ; }", ALLOW_INCOMPLETE_EXPR); 
        assertParseOk("{ new cog ; }", ALLOW_INCOMPLETE_EXPR); 
        assertParseOk("{ x = new ; }", ALLOW_INCOMPLETE_EXPR); 
        assertParseOk("{ x = new cog ; }", ALLOW_INCOMPLETE_EXPR); 
    }
    
}
