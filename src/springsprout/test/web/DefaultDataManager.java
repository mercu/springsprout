package springsprout.test.web;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceUtils;

import springsprout.test.exception.DataManagerSettingException;
import springsprout.test.exception.TestDataDeleteException;
import springsprout.test.exception.TestDataInputException;
import springsprout.test.web.annotation.DataConfiguration;
import springsprout.test.web.enumeration.DataType;

public class DefaultDataManager implements DataManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	DataSource dataSource;
	IDataSet dataset;
	DatabaseConnection databaseConnection;

	public DefaultDataManager(DataConfiguration dc) {
		this((String) AnnotationUtils.getValue(dc, "location"),
				(DataType) AnnotationUtils.getValue(dc, "dataType"));
	}

	public DefaultDataManager(String location, DataType dataType) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"applicationContext.xml");
		dataSource = applicationContext.getBean("dataSource", DataSource.class);
		InputStream sourceStream;
		try {
			sourceStream = new ClassPathResource(location).getInputStream();
			dataset = makeDataSet(dataType, sourceStream);
			databaseConnection = new DatabaseConnection(DataSourceUtils
					.getConnection(dataSource));
		} catch (Exception e) {
			logger.debug("DATA MANAGER SETTING ERROR", e);
			throw new DataManagerSettingException("DATA MANAGER SETTING ERROR");
		}
	}

	private IDataSet makeDataSet(DataType dataType, InputStream sourceStream)
			throws DataSetException, IOException {
		if (dataType == DataType.XML)
			return new FlatXmlDataSet(sourceStream);
		else if (dataType == DataType.EXEL)
			return new XlsDataSet(sourceStream);
		else
			throw new IllegalArgumentException();
	}

	public void insertTestData() {
		try {
			DatabaseOperation operation = DatabaseOperation.CLEAN_INSERT;
			operation.execute(databaseConnection, dataset);
			dataSource.getConnection().commit();
			logger.debug("TEST DATA INPUT OK....");
		} catch (Exception e) {
			logger.debug("TEST DATA INPUT ERROR", e);
			try {
				dataSource.getConnection().rollback();
				logger.debug("TEST DATA INPUT ROLLBACK OK....", e);
			} catch (SQLException e1) {
				logger.debug("TEST DATA INPUT ROLLBACK FAILED!", e);
				throw new TestDataInputException(
						"TEST DATA INPUT ROLLBACK FAILED");
			}
			throw new TestDataInputException("TEST DATA INPUT ERROR");
		}
	}

	public void deleteTestData() {
		try {
			DatabaseOperation operation = DatabaseOperation.DELETE_ALL;
			operation.execute(databaseConnection, dataset);
			dataSource.getConnection().commit();
			logger.debug("TEST DATA DELETE OK....");
		} catch (Exception e) {
			logger.debug("TEST DATA DELETE ERROR", e);
			try {
				dataSource.getConnection().rollback();
				logger.debug("TEST DATA DELETE ROLLBACK OK....", e);
			} catch (SQLException e1) {
				logger.debug("TEST DATA DELETE ROLLBACK FAILED!", e);
				throw new TestDataInputException(
						"TEST DATA DELETE ROLLBACK FAILED");
			}
			throw new TestDataDeleteException("TEST DATA DELETE ERROR");
		}
	}
}
