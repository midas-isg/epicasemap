package interactors;

import static org.junit.Assert.assertTrue;
import integrations.app.App;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import play.libs.F.Callback0;

public class TestFileHandler {
	
	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}
	
	@Test
	public void create() {
		runWithTransaction(() -> testPersist());
	}
	
	
	public void testPersist(){
		DelimitedFile dataFile = createTestFileWithApolloIDformat();
		assertTrue(FileHandler.persist(dataFile));
				
	}
	

	private  DelimitedFile createTestFileWithApolloIDformat() {
		String fileContent = "time,apollo ID,value\n"
				+ "2015-01-01,1,1\n"
				+ "2015-01-01T10:15,2,2\n"
				+ "2015-02-01T10:15:12,3,3\n"
				+ "2015-02-01T10:15:12+05,4,4\n"
				+ "2015-02-01T10:15:12+05:00,5,5\n";
		
		File csvFile = new File("test/resources/test_input.txt");
		BufferedWriter  bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(csvFile));
			bw.write(fileContent);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Map<String,String[]> metaData = new HashMap<String,String[]>();
		metaData.put("title",new String[]{"serie1"});
		metaData.put("format",new String[]{"apolloIdFormat"});
		metaData.put("delimiter",new String[]{","});
		metaData.put("headers",new String[]{"time","apollo ID","value"});
		metaData.put("description",new String[]{"desc"});
		DelimitedFile dataFile = new DelimitedFile(csvFile, metaData);
		return dataFile;
	}

}
