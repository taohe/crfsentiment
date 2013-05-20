package dependencytree;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: tao
 * Date: 5/11/13
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoadPriorPolarityFeatureTest {
    private LoadPriorPolarityFeature loadPriorPolarityFeature;
    @Before
    public void setUp() throws Exception {
        loadPriorPolarityFeature = new LoadPriorPolarityFeature();
    }
    @After
    public void tearDown() throws Exception {
        loadPriorPolarityFeature = null;
    }

    @Test
    public void simplePriorPolarMapTest() {
        try {
            loadPriorPolarityFeature.readPriPolarDict(true);
            Map<String, PriorPolarityValue> priorPolarityMap =
                    loadPriorPolarityFeature.getPriorPolarityMap();
            System.out.println("Contain word= " + "abhors?");
            assertTrue(priorPolarityMap.containsKey("abhors"));
            assertEquals(priorPolarityMap.get("abhors").priorPolarity, -2);

            System.out.println("Contain word= " + "zenith?");
            assertTrue(priorPolarityMap.containsKey("zenith"));
            assertEquals(priorPolarityMap.get("zenith").priorPolarity, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void simplePolarReversalSetTest() {
        try {
            loadPriorPolarityFeature.readPolarReversalDict(true);
            Set<String> polarityReversalSet = loadPriorPolarityFeature.getPolarityReversalSet();
            assertTrue(polarityReversalSet.contains("slow"));
            assertFalse(polarityReversalSet.contains("happy"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
