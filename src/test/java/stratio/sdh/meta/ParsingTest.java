package stratio.sdh.meta;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;
import org.junit.BeforeClass;

import com.stratio.sdh.meta.statements.CreateIndexStatement;
import com.stratio.sdh.meta.statements.MetaStatement;
import com.stratio.sdh.meta.structures.ValueProperty;
import com.stratio.sdh.meta.utils.AntlrResult;
import com.stratio.sdh.meta.utils.MetaUtils;
import com.stratio.sdh.meta.utils.RecoverableParserException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * MetaParser tests that recognize the different options of each Statement.
 */
public class ParsingTest {

	/**
	 * Class logger.
	 */
	private static final Logger _logger = Logger.getLogger(ParsingTest.class.getName());
	
	/**
	 * Parse a input text and return the equivalent Statement.
	 * //@param inputText The input text.
	 * //@return A Statement or null if the process failed.
	 *
	private Statement parseStatement(String inputText){
            Statement result = null;
            ANTLRStringStream input = new ANTLRStringStream(inputText);
            MetaLexer lexer = new MetaLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MetaParser parser = new MetaParser(tokens);   
            try {
                result = parser.query();
            } catch (RecognitionException e) {
                _logger.error("Cannot parse statement", e);
            }
            return result;
	}*/
	
	@BeforeClass
	//TODO Define common logging properties
	public static void initLog(){
            ConsoleAppender console = new ConsoleAppender();
            //String PATTERN = "%d [%p|%c|%C{1}] %m%n";
            String PATTERN = "%d [%p|%c] %m%n";
            console.setLayout(new PatternLayout(PATTERN)); 
            console.setThreshold(Level.INFO);
            console.activateOptions();
            Logger.getRootLogger().addAppender(console);
	}

	// CREATE KEYSPACE (IF NOT EXISTS)? <keyspace_name> WITH <properties> ';'
	@Test
	public void createKeyspace_basic() {
            String inputText = "CREATE KEYSPACE key_space1 "
                            + "WITH replication = replicationLevel AND durable_writes = false;";		
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse create keyspace - basic", st);
            //assertEquals("Cannot parse create keyspace - basic", inputText, st.toString()+";");
            //System.out.println(st.toString());
            //System.out.println(inputText);
            assertTrue("Cannot parse create keyspace - basic", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	@Test
	public void createKeyspace_ifNotExists() {
            String inputText = "CREATE KEYSPACE IF NOT EXISTS key_space1 "
                            + "WITH replication = replicationLevel AND durable_writes = false;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse create keyspace - If Not Exists", st);
            //assertEquals("Cannot parse create keyspace - basic", inputText, st.toString()+";");
            assertTrue("Cannot parse create keyspace - If Not Exists", inputText.equalsIgnoreCase(st.toString()+";"));
	}

	//TODO: Should we support it in this way?
	@Test
	public void createKeyspace_nestedOptions() {
            String inputText = "CREATE KEYSPACE IF NOT EXISTS key_space1 "
                            + "WITH replication = {class: NetworkTopologyStrategy, DC1: 1, DC2: 3} "
                            +"AND durable_writes = false;";
            Set<String> properties = new HashSet<>();
            properties.add("class: NetworkTopologyStrategy");
            properties.add("DC1: 1");
            properties.add("DC2: 3");
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            String propResultStr = st.toString().substring(st.toString().indexOf("{")+1, st.toString().indexOf("}"));
            //System.out.println(propResultStr);
            String[] str = propResultStr.split(",");
            Set<String> propertiesResult = new HashSet<>();
            for (String str1 : str) {
                propertiesResult.add(str1.trim());
            }                                
            assertNotNull("Cannot parse create keyspace - nestedOptions", st);                
            //assertEquals("Cannot parse create keyspace - nestedOptions", inputText, st.toString()+";");
            //System.out.println(st.toString().substring(0, st.toString().indexOf("{")));
            assertEquals("Cannot parse create keyspace - nestedOptions", 
                    "CREATE KEYSPACE IF NOT EXISTS key_space1 WITH replication = {", 
                    st.toString().substring(0, st.toString().indexOf("{")+1));
            //ystem.out.println(st.toString().substring(st.toString().indexOf("}"))+";");
            assertEquals("Cannot parse create keyspace - nestedOptions", 
                    "} AND durable_writes = false;", 
                    st.toString().substring(st.toString().indexOf("}"))+";");
            //assertEquals("Cannot parse create keyspace - nestedOptions", inputText, st.toString()+";");
            assertTrue("Cannot parse create keyspace - nestedOptions", propertiesResult.containsAll(properties));
            assertTrue("Cannot parse create keyspace - nestedOptions", properties.containsAll(propertiesResult));
	}
        
        @Test
	public void update_tablename() {
            String inputText = "UPDATE tablename USING prop1 = 342 SET ident1 = term1, ident2 = term2"
                    + " WHERE ident3 IN (term3, term4) IF field1 = 25 ;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //System.out.println(inputText);
            //System.out.println(st.toString()+" ;");
            assertNotNull("Cannot parse update tablename", st);
            //assertEquals("Cannot parse update tablename", inputText, st.toString()+";");
            assertTrue("Cannot parse update tablename", inputText.equalsIgnoreCase(st.toString()+" ;"));
	}
        
        @Test
	public void insert_into() {
            String inputText = "INSERT INTO mykeyspace.tablename (ident1, ident2) VALUES(term1, term2) "
                    + "IF NOT EXISTS USING COMPACT STORAGE AND prop1 = {innerTerm: result};";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //System.out.println(inputText);
            //System.out.println(st.toString()+";");
            assertNotNull("Cannot parse insert into", st);
            //assertEquals("Cannot parse insert into", inputText, st.toString()+";");
            assertTrue("Cannot parse insert into", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
        @Test
	public void truncate_table() {
            String inputText = "TRUNCATE usersTable;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse truncate table", st);
            //assertEquals("Cannot parse truncate table", inputText, st.toString()+";");
            assertTrue("Cannot parse truncate table", inputText.equalsIgnoreCase(st.toString()+";"));
	}
                
	//
	//CREATE INDEX
	
	// CREATE <type_index>? INDEX (IF NOT EXISTS)? <identifier>? ON <tablename> '(' <identifier> (',' <identifier>)* ')'
    // ( USING <string> )? WITH OPTIONS? (<maps> AND <maps>...) ';'
	//HASH → Usual inverted index, Hash index. (By default).
	//FULLTEXT → Full text index. 
	//CUSTOM → custom index. (new feature for release 2)

	@Test
	public void createIndex_hash_basic() {
            String inputText = "CREATE HASH INDEX index1 ON table1 (field1, field2);";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse basic hash index", st);
            //assertEquals("Cannot parse basic hash index", inputText, st.toString()+";");
            assertTrue("Cannot parse basic hash index", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	@Test
	public void createIndex_hash_ifNotExist() {
            String inputText = "CREATE HASH INDEX IF NOT EXISTS index1 ON table1 (field1, field2);";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse hash index with if not exists", st);
            //assertEquals("Cannot parse hash index with if not exists", inputText, st.toString()+";");
            assertTrue("Cannot parse hash index with if not exists", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	@Test
	public void createIndex_hash_using() {
            String inputText = "CREATE HASH INDEX index1 ON table1 (field1, field2) USING com.company.Index.class;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse hash index with using clause", st);
            //assertEquals("Cannot parse hash index with using clause", inputText, st.toString()+";");
            assertTrue("Cannot parse hash index with using clause", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	@Test
	public void createIndex_hash_options() {
            String inputText = "CREATE HASH INDEX index1 ON table1 (field1, field2) WITH OPTIONS opt1=val1 AND opt2=val2;";
            int numberOptions = 2;

            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse hash index with options clause", st);
            CreateIndexStatement cist = CreateIndexStatement.class.cast(st);
            //assertEquals("Cannot parse hash index with options clause - name", "index1", cist.getName());
            assertTrue("Cannot parse hash index with options clause - name", "index1".equalsIgnoreCase(cist.getName()));
            assertEquals("Cannot parse hash index with options clause - options size", numberOptions, cist.getOptions().size());
            HashMap<String, ValueProperty> options = cist.getOptions();

            for(int i = 1; i < numberOptions; i++){
                assertTrue("Cannot parse hash index with options clause - options opt"+i, options.containsKey("opt"+i));
                //assertEquals("Cannot parse hash index with options clause - options opt"+i, "val"+i, options.get("opt"+i).toString());
                assertTrue("Cannot parse hash index with options clause - options opt"+i, ("val"+i).equalsIgnoreCase(options.get("opt"+i).toString()));
            }

	}
	
	@Test
	public void createIndex_hash_all() {
            String inputText = "CREATE HASH INDEX IF NOT EXISTS index1 "
                            + "ON table1 (field1, field2) USING com.company.Index.class "
                            + "WITH OPTIONS opt1=val1 AND opt2=val2;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse hash index with options clause", st);
            CreateIndexStatement cist = CreateIndexStatement.class.cast(st);

            String retrieved = cist.toString().substring(0, cist.toString().indexOf("OPTIONS"));
            String expected = inputText.substring(0, inputText.indexOf("OPTIONS"));
            //assertEquals("Cannot parse hash index with using clause", expected, retrieved);
            assertTrue("Cannot parse hash index with using clause", expected.equalsIgnoreCase(retrieved));

            assertTrue("Cannot parse hash index with options clause - options size", cist.getOptions().size() > 0);
            HashMap<String, ValueProperty> options = cist.getOptions();

            assertTrue("Cannot parse hash index with options clause - options opt1", options.containsKey("opt1"));
            assertTrue("Cannot parse hash index with options clause - options opt1", options.containsKey("opt2"));
	}

	//DROP INDEX

	@Test
	public void dropIndex_basic() {
            String inputText = "DROP INDEX index_name;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse drop index basic", st);
            //assertEquals("Cannot parse drop index basic", inputText, st.toString()+";");
            assertTrue("Cannot parse drop index basic", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	@Test
	public void dropIndex_ifExists() {
            String inputText = "DROP INDEX IF EXISTS index_name;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse drop index with if exists clause", st);
            //assertEquals("Cannot parse drop index with if exists clause", inputText, st.toString()+";");
            assertTrue("Cannot parse drop index with if exists clause", inputText.equalsIgnoreCase(st.toString()+";"));
	}
        
        @Test
	public void select_statement() {
            String inputText = "SELECT ident1 AS name1, myfunction(innerIdent, anotherIdent) AS functionName "
                    + "FROM newks.newtb WITH WINDOW 5 ROWS INNER JOIN tablename ON field1=field2 WHERE ident1 LIKE whatever"
                    + " ORDER BY id1 ASC GROUP BY col1 LIMIT 50 DISABLE ANALYTICS;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse select", st);
            //assertEquals("Cannot parse select", inputText, st.toString());

            assertTrue("Cannot parse select", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	//ADD
	
	@Test
	public void add_basic() {
            String inputText = "ADD \"jar_name-v1.0.jar\";";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse add jar basic", st);
            //assertEquals("Cannot parse add jar basic", inputText, st.toString()+";");
            assertTrue("Cannot parse add jar basic", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	@Test
	public void add_relative() {
            String inputText = "ADD \"dir/jar_name-v1.0.jar\";";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse add jar with relative path", st);
            //assertEquals("Cannot parse add jar with relative path", inputText, st.toString()+";");
            assertTrue("Cannot parse add jar with relative path", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	@Test
	public void add_absolute() {
            String inputText = "ADD \"/dir/jar_name-v1.0.jar\";";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse add jar with absolute path", st);
            //assertEquals("Cannot parse add jar with absolute path", inputText, st.toString()+";");
            assertTrue("Cannot parse add jar with absolute path", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	//LIST
	
	@Test
	public void list_process() {
            String inputText = "LIST PROCESS;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse list process", st);
            //assertEquals("Cannot parse list process", inputText, st.toString()+";");
            assertTrue("Cannot parse list process", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	@Test
	public void list_udf() {
            String inputText = "LIST UDF;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse list udf", st);
            //assertEquals("Cannot parse list udf", inputText, st.toString()+";");
            assertTrue("Cannot parse list udf", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	@Test
	public void list_trigger() {
            String inputText = "LIST TRIGGER;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse list trigger", st);
            //assertEquals("Cannot parse list trigger", inputText, st.toString()+";");
            assertTrue("Cannot parse list trigger", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	//REMOVE UDF	
	@Test
	public void removeUDF() {
            String inputText = "REMOVE UDF \"jar.name\";";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse remove udf", st);
            //assertEquals("Cannot parse remove udf", inputText, st.toString()+";");
            assertTrue("Cannot parse remove udf", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	//DELETE ( <selection> ( ',' <selection> )* )?
        //FROM <tablename>
        //WHERE <where-clause>	
	@Test
	public void delete_where() {
            String inputText = "DELETE FROM table1 WHERE field1 = value1;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse delete - where", st);
            //assertEquals("Cannot parse delete - where", inputText, st.toString()+";");
            assertTrue("Cannot parse delete - where", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	@Test
	public void delete_selection() {
            String inputText = "DELETE (col1, col2) FROM table1 WHERE field1 = value1;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse delete - selection", st);
            //assertEquals("Cannot parse delete - selection", inputText, st.toString()+";");
            assertTrue("Cannot parse delete - selection", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
	@Test
	public void delete_full() {
            String inputText = "DELETE (col1, col2) FROM table1 WHERE field1 = value1 AND field2 = value2;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse delete - full", st);
            //assertEquals("Cannot parse delete - full", inputText, st.toString()+";");
            assertTrue("Cannot parse delete - full", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
        @Test
	public void set_basic() {
            String inputText = "SET OPTIONS ANALYTICS=true AND CONSISTENCY=LOCAL_ONE;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //System.out.println(inputText);
            //System.out.println(st.toString()+";");
            assertNotNull("Cannot parse set - basic", st);
            //assertEquals("Cannot parse set - basic", inputText, st.toString()+";");
            assertTrue("Cannot parse set - basic", inputText.equalsIgnoreCase(st.toString()+";"));
	}
        
	//UPDATE
	
	@Test
	public void update_basic() {
            String inputText = "UPDATE table1 SET field1 = value1 WHERE field3 = value3;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse update - basic", st);
            //assertEquals("Cannot parse update - basic", inputText, st.toString()+";");
            assertTrue("Cannot parse update - basic", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
        @Test
	public void explain_plan() {
            String inputText = "EXPLAIN PLAN FOR DROP INDEX indexName;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //System.out.println("EXPLAIN PLAN FOR DROP INDEX indexName;");
            //System.out.println(st.toString()+";");
            assertNotNull("Cannot parse explain plan", st);
            //assertEquals("Cannot parse explain plan", inputText, st.toString()+";");
            assertTrue("Cannot parse explain plan", inputText.equalsIgnoreCase(st.toString()+";"));
	}
        
        @Test
	public void drop_table() {
            String inputText = "DROP TABLE IF EXISTS lastTable;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //System.out.println(inputText);
            //System.out.println(st.toString()+";");
            assertNotNull("Cannot parse drop table", st);
            //assertEquals("Cannot parse drop table", inputText, st.toString()+";");
            assertTrue("Cannot parse drop table", inputText.equalsIgnoreCase(st.toString()+";"));
	}
        
	@Test
	public void update_full() {
            String inputText = "UPDATE table1 USING TTL = 400 SET field1 = value1,"
                            + " field2 = value2 WHERE field3 = value3 AND field4 = value4;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse update - full", st);
            //assertEquals("Cannot parse update - full", inputText, st.toString()+";");
            assertTrue("Cannot parse update - full", inputText.equalsIgnoreCase(st.toString()+";"));
	}
        
        @Test
	public void createTable_basic() {
            String inputText = "create table adsa(algo text primary key, algo2 int, algo3 bool);";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "Create table adsa(algo text PRIMARY KEY, algo2 int, algo3 bool)";
            assertNotNull("Cannot parse create table - basic", st);
            //assertEquals("Cannot parse create table - basic", comparativeText, st.toString());
            //System.out.println(st.toString());
            //System.out.println(inputText);
            assertTrue("Cannot parse create table - basic", inputText.equalsIgnoreCase(st.toString()+";"));
	}
        
        @Test
	public void createTable_basic_2() {
            String inputText = "create table adsa(algo text, algo2 int primary key, algo3 bool);";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "Create table adsa(algo text, algo2 int PRIMARY KEY, algo3 bool)";
            assertNotNull("Cannot parse create table - basic_2", st);
            //assertEquals("Cannot parse create table - basic_2", comparativeText, st.toString()+";");
            //assertEquals("Cannot parse create table - basic_2", inputText.equalsIgnoreCase(st.toString()+";"));
            //System.out.println(st.toString());
            //System.out.println(inputText);
            assertTrue("Cannot parse create table - basic_2", inputText.equalsIgnoreCase(st.toString()+";"));
	}
	
        @Test
	public void createTable_basic_3() {
            String inputText = "create table adsa(algo text, algo2 int, algo3 bool primary key);";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "Create table adsa(algo text, algo2 int, algo3 bool PRIMARY KEY)";
            assertNotNull("Cannot parse create table - basic_3", st);
            //assertEquals("Cannot parse create table - basic_3", comparativeText, st.toString()+";");
            //System.out.println(st.toString());
            //System.out.println(inputText);
            assertTrue("Cannot parse create table - basic_3", inputText.equalsIgnoreCase(st.toString()+";"));
	}
        
        @Test
	public void createTable_basic_4() {
            String inputText = "create table adsa(algo text, algo2 int, algo3 bool, primary key (algo));";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "Create table adsa(algo text, algo2 int, algo3 bool, PRIMARY KEY (algo))";
            assertNotNull("Cannot parse create table - basic_4", st);
            //assertEquals("Cannot parse create table - basic_4", comparativeText, st.toString()+";");
            //System.out.println(st.toString());
            //System.out.println(inputText);
            assertTrue("Cannot parse create table - basic_4", inputText.equalsIgnoreCase(st.toString()+";"));
	}
        
        @Test
        public void createTable_basic_5() {
            String inputText = "create table adsa(algo text, algo2 int, algo3 bool, primary key (algo, algo2));";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "Create table adsa(algo text, algo2 int, algo3 bool, PRIMARY KEY (algo, algo2))";
            assertNotNull("Cannot parse create table - basic_5", st);
            //assertEquals("Cannot parse create table - basic_5", comparativeText, st.toString()+";");
            //System.out.println(st.toString());
            //System.out.println(inputText);
            assertTrue("Cannot parse create table - basic_5", inputText.equalsIgnoreCase(st.toString()+";"));
	}
        
        @Test
        public void createTable_basic_6() {
            String inputText = "create table adsa(algo text, algo2 int, algo3 bool, primary key ((algo, algo2), algo3));";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "Create table adsa(algo text, algo2 int, algo3 bool, PRIMARY KEY ((algo, algo2), algo3))";
            assertNotNull("Cannot parse create table - basic_6", st);
            //assertEquals("Cannot parse create table - basic_6", comparativeText, st.toString()+";");
            //System.out.println(st.toString());
            //System.out.println(inputText);
            assertTrue("Cannot parse create table - basic_6", inputText.equalsIgnoreCase(st.toString()+";"));
       }
        
       @Test 
       public void createTable_basic_7() {
            String inputText = "create table adsa(algo text, algo2 int, algo3 bool, primary key ((algo, algo2), algo3)) with propiedad1=prop1 and propiedad2=2 and propiedad3=3.0;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //System.out.println(inputText);
            //System.out.println(st.toString()+";");
            assertNotNull("Cannot parse create table - basic_7", st);
            //assertEquals("Cannot parse create table - basic_7", inputText, st.toString());
            assertTrue("Cannot parse create table - basic_7", inputText.equalsIgnoreCase(st.toString()+";"));
       } 
       
       @Test
       public void alterKeyspace() {
            String inputText = "ALTER KEYSPACE mykeyspace WITH ident1 = value1 AND ident2 = 54;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "ALTER KEYSPACE mykeyspace WITH ident1 = value1 AND ident2 = 54";
            //assertTrue("Cannot parse alter keyspace", comparativeText.equalsIgnoreCase(st.toString()));
            assertNotNull("Cannot parse alter keyspace", st);
            //assertEquals("Cannot parse alter keyspace", comparativeText, st.toString());
            assertTrue("Cannot parse alter keyspace", inputText.equalsIgnoreCase(st.toString()+";"));		
       }
       
       @Test
       public void dropKeyspace() {
            String inputText = "drop keyspace IF EXISTS mykeyspace;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //System.out.println(inputText);
            //System.out.println(st.toString()+";");
            assertNotNull("Cannot parse drop keyspace", st);
            //assertEquals("Cannot parse drop keyspace", comparativeText, st.toString());
            assertTrue("Cannot parse drop keyspace", inputText.equalsIgnoreCase(st.toString()+";"));
       }
       
       @Test
       public void alterTable_basic() {
            String inputText = "alter table tabla1 alter column1 type int;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "Alter table tabla1 alter column1 type int";
            assertNotNull("Cannot parse alter table - basic", st);
            //assertEquals("Cannot parse alter table - basic", comparativeText, st.toString()+";");
            assertTrue("Cannot parse alter table - basic", inputText.equalsIgnoreCase(st.toString()+";"));
       } 
       
       @Test
       public void alterTable_basic_1() {
            String inputText = "alter table tabla1 add column1 int;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "Alter table tabla1 add column1 int";
            assertNotNull("Cannot parse alter table - basic_1", st);
            //assertEquals("Cannot parse alter table - basic_1", comparativeText, st.toString()+";");
            assertTrue("Cannot parse alter table - basic_1", inputText.equalsIgnoreCase(st.toString()+";"));
       } 
       
       @Test
       public void alterTable_basic_2() {
            String inputText = "alter table tabla1 drop column1;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "Alter table tabla1 drop column1";
            assertNotNull("Cannot parse alter table - basic_2", st);
            //assertEquals("Cannot parse alter table - basic_2", comparativeText, st.toString()+";");
            assertTrue("Cannot parse alter table - basic_2", inputText.equalsIgnoreCase(st.toString()+";"));
       }
       
       @Test
       public void alterTable_basic_3() {
            String inputText = "Alter table tabla1 with property1=value1 and property2=2 and property3=3.0;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            assertNotNull("Cannot parse alter table - basic_3", st);
            //assertEquals("Cannot parse alter table - basic_3", inputText, st.toString()+";");
            //System.out.println(inputText);
            //System.out.println(st.toString());
            assertTrue("Cannot parse alter table - basic_3", inputText.equalsIgnoreCase(st.toString()+";"));
       }
       
       @Test
       public void stopProcess() {
            String inputText = "STOP process process1;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "Stop process process1";
            assertNotNull("Cannot parse stop process", st);
            //assertEquals("Cannot parse stop process", comparativeText, st.toString()+";");
            assertTrue("Cannot parse stop process", inputText.equalsIgnoreCase(st.toString()+";"));
       }
       
       @Test
       public void createTrigger() {
            String inputText = "create trigger trigger1 on table_name USING triggerClassName;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "create trigger trigger1 on table_name USING triggerClassName";
            assertNotNull("Cannot parse create trigger", st);
            //assertEquals("Cannot parse create trigger", comparativeText, st.toString()+";");
            assertTrue("Cannot parse create trigger", inputText.equalsIgnoreCase(st.toString()+";"));
       }
       
       @Test
       public void dropTrigger() {
            String inputText = "drop trigger trigger1 on table_name;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            //String comparativeText= "Drop trigger trigger1 on table_name";
            assertNotNull("Cannot parse drop trigger", st);
            //assertEquals("Cannot parse drop trigger", comparativeText, st.toString()+";");
            assertTrue("Cannot parse drop trigger", inputText.equalsIgnoreCase(st.toString()+";"));
       }
       
       // TEST EXCEPTIONS
       @Rule
       public ExpectedException thrown = ExpectedException.none();       
       
       @Test
       public void update_for_invalid_assignment(){
            String inputText = "UPDATE table1 SET field1 = value1 WHERE field3: value3;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class);
            //thrown.expectMessage("Value of field3 should be null");
            System.out.println(st.toString());
       }
       
       @Test
       public void update_wrong_spelling(){
            String inputText = "UPDDATE table1 SET field1 = value1 WHERE field3: value3;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class);
            //thrown.expectMessage("Value of field3 should be null");
            System.out.println(st.toString());
       }
       
       @Test
       public void unknown_first_word_of_statement(){
            String inputText = "WINDOWS GO HOME;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class);
            //thrown.expectMessage("Value of field3 should be null");
            System.out.println(st.toString());
       }
       
       @Test
       public void wrong_plan_token(){
            String inputText = "EXPLAIN PLAANS FOR DROP INDEX indexName;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class);
            //thrown.expectMessage("Value of field3 should be null");
            System.out.println(st.toString());
       }
       
       @Test
       public void wrong_into_token(){
            String inputText = "INSERT INTI mykeyspace.tablename (ident1, ident2) VALUES(term1, term2)"
                    + " IF NOT EXISTS USING COMPACT STORAGE AND prop1 = {innerTerm: result};";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(RecoverableParserException.class);            
            //thrown.expectMessage("Value of field3 should be null");
            if(antlrResult.getFoundErrors().getNumberOfErrors()>0) throw new RecoverableParserException("Wrong 'INTO' token");
       }
       
       @Test
       public void create_table_wrong_column_definition(){
            String inputText = "CREATE TABLE adsa (algo text, primary key ([algo, algo2],algo3));";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class);
            //thrown.expectMessage("Value of field3 should be null");
            System.out.println(st.toString());
       }
       
       @Test
       public void create_table_wrong_values_token(){
            String inputText = "INSERT INTO mykeyspace.tablename (ident1, ident2) VALUED(term1, term2)"
                    + " IF NOT EXISTS USING COMPACT STORAGE AND prop1 = {innerTerm: result};";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class);
            //thrown.expectMessage("Value of field3 should be null");
            System.out.println(st.toString());
       }
       
       @Test
       public void create_keyspace_wrong_identifier(){
            String inputText = "CREATE KEYSPACE name.key_space1 WITH replication = replicationLevel;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class);
            //thrown.expectMessage("Value of field3 should be null");
            System.out.println(st.toString());
       }
       
       @Test
       public void truncate_wrong_identifier(){
            String inputText = "TRUNCATE companyKS..usersTable;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(RecoverableParserException.class);            
            //thrown.expectMessage("Value of field3 should be null");
            if(antlrResult.getFoundErrors().getNumberOfErrors()>0) throw new RecoverableParserException("Wrong identifier");
       }
       
       @Test
       public void alter_wrong_keyspace_token(){
            String inputText = "ALTER KEYSPACES mykeyspace WITH ident1 = value1;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class); 
            System.out.println(st.toString());
       }

       @Test
       public void drop_wrong_place_for_if_exists(){
            String inputText = "DROP KEYSPACE mykeyspace IF EXISTS;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class); 
            System.out.println(st.toString());
       }

       @Test
       public void set_wrong_boolean(){
            String inputText = "SET OPTIONS ANALYTICS=5;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class); 
            System.out.println(st.toString());
       }

       @Test
       public void alter_wrong_property_identifier(){
            String inputText = "ALTER TABLE tabla1 with 2property1=value1;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class); 
            System.out.println(st.toString());
       }

       @Test
       public void drop_not_missing(){
            String inputText = "DROP TABLE IF EXISTS _lastTable;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(RecoverableParserException.class); 
            if(antlrResult.getFoundErrors().getNumberOfErrors()>0) throw new RecoverableParserException("Drop missing word 'NOT'");
       }

       @Test
       public void create_index_wrong_option_assignment(){
            String inputText = "CREATE HASH INDEX index1 ON table1 (field1, field2) WITH OPTIONS opt1:val1;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(RecoverableParserException.class); 
            if(antlrResult.getFoundErrors().getNumberOfErrors()>0) throw new RecoverableParserException("Create index with wrong assignment symbol in options");
       }

       @Test
       public void drop_index_wrong_not_word(){
            String inputText = "DROP INDEX IF NOT EXISTS index_name;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(RecoverableParserException.class); 
            if(antlrResult.getFoundErrors().getNumberOfErrors()>0) throw new RecoverableParserException("Drop index with not expected word 'NOT'");
       }

       @Test
       public void delete_wrong_property_assignment(){
            String inputText = "DELETE (col1 AND col2) FROM table1 WHERE field1: value1;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(RecoverableParserException.class); 
            if(antlrResult.getFoundErrors().getNumberOfErrors()>0) throw new RecoverableParserException("Delete with wrong assignmet symbol for where clause");
       }

       @Test
       public void select_wrong_like_word(){
            String inputText = "SELECT ident1, myfunction(innerIdent, anotherIdent) LIKE ident1 FROM newks.newtb;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class); 
            System.out.println(st.toString());
       }

       @Test
       public void add_ending_quote_missing(){
            String inputText = "ADD \"/dir/jar_name-v1.0.jar;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(RecoverableParserException.class); 
            if(antlrResult.getFoundErrors().getNumberOfErrors()>0) throw new RecoverableParserException("Add missing ending quote symbol");
       }

       @Test
       public void list_reserved_word_use(){
            String inputText = "LIST PROCESS LAST;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class);
            System.out.println(st.toString());
       }

       @Test
       public void remove_udf_not_expected_word() {
            String inputText = "REMOVE UDF \"jar.name\" NOW;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class); 
            System.out.println(st.toString());
       }

       @Test
       public void stop_wrong_process_identifier(){
            String inputText = "STOP process kstest.process1;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class); 
            System.out.println(st.toString());
       }

       @Test
       public void create_trigger_wrong_as_word_use(){
            String inputText = "create trigger trigger1 on table_name USING triggerClassName AS ident1;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class); 
            System.out.println(st.toString());
       }

       @Test
       public void drop_trigger_wrong_assignment(){
            String inputText = "drop trigger trigger1 on table_name = 20;";
            AntlrResult antlrResult = MetaUtils.parseStatement(inputText, _logger);
            MetaStatement st = antlrResult.getStatement();
            thrown.expect(NullPointerException.class); 
            System.out.println(st.toString());
       }
       
}
