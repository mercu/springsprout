package springsprout.test.web;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import springsprout.test.web.annotation.WebTest;
import springsprout.test.web.annotation.WebTestConfiguration;

//@RunWith(WebTestRunner.class)
@WebTestConfiguration(testDataLocation = "/web/testData.xml", appName = "springsprout")
public class WebTestRunnerTest {

	WebTestRunnerStub runner;
	WarManager mockWarManager;
	DataManager mockDataManager;

	@Before
	public void setUp() throws InitializationError{
		runner = new WebTestRunnerStub(this.getClass());
		mockWarManager = mock(WarManager.class);
		mockDataManager = mock(DataManager.class);
		runner.setWarManager(mockWarManager);
		runner.setDataManager(mockDataManager);
	}

	@Test
	public void findWebTest() throws Exception {
		runner.computeTestMethods();
		assertTrue(runner.getTestMethodName().contains("webTestA"));
		assertTrue(runner.getTestMethodName().contains("webTestB"));
		assertFalse(runner.getTestMethodName().contains("testB"));
	}

	@Test
	public void runWebTestBestSinario() throws Exception {
		RunNotifier mockRN = mock(RunNotifier.class);
		runner.run(mockRN);

		verify(mockWarManager).packaging();
		verify(mockWarManager).deploy();
		verify(mockDataManager).insertTestData();
		//test
		verify(mockDataManager).deleteTestData();
		verify(mockWarManager).undeploy();
	}

	@Test
	public void testname() throws Exception {

	}

	@Test
	public void testB(){}

	@WebTest
	public void webTestA(){System.out.println("webTestA");}

	@WebTest
	public void webTestB(){System.out.println("webTestB");}

	@WebTest
	@Ignore
	public void webTestC(){System.out.println("webTestC");}

}
