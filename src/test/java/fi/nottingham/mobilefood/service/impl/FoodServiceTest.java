package fi.nottingham.mobilefood.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import org.fest.util.SystemProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.IFoodService;

public class FoodServiceTest {

	private IFoodService foodService;
	private IFileSystemService fileSystemService;
	
	@BeforeClass
	public static void beforeClass() {
		URL.setURLStreamHandlerFactory(new MockURLStreamHandler());
	}

	@Before
	public void setUp() {
		fileSystemService = mock(IFileSystemService.class);
		foodService = new FoodServiceImpl("http://localhost:4730/mobilerest/",
				fileSystemService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getFoodsBy_dontReturnNull() throws FileNotFoundException {
		int dayOfTheWeek = 0, weekNumber = 1;

		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);
		when(fileSystemService.openOutputFile(Mockito.anyString())).thenReturn(
				mock(OutputStream.class));

		assertNotNull(foodService.getFoodsBy(weekNumber, dayOfTheWeek));
	}

	@Test
	public void getFoodsBy_withFileForCurrentWeek_returnsUnemptyList()
			throws FileNotFoundException, URISyntaxException {
		int dayOfTheWeek = 0, weekNumber = 10;

		InputStream jsonFoodFile = getTestFileAsInputStream();

		when(fileSystemService.openInputFile(Mockito.anyString())).thenReturn(
				jsonFoodFile);

		assertFalse(foodService.getFoodsBy(weekNumber, dayOfTheWeek).isEmpty());
	}

	private static InputStream getTestFileAsInputStream()
			throws FileNotFoundException, URISyntaxException {
		int weekNumber = 10;
		
		return FoodServiceTest.class.getResourceAsStream(String.format("2014_w%s_unica.json", weekNumber));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getFoodsBy_createsFileIfFoodsAreLoadedFromService()
			throws IOException {
		int dayOfTheWeek = 0, weekNumber = 1;
		OutputStream fileOutputStreamMock = mock(OutputStream.class);

		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);
		when(fileSystemService.openOutputFile(Mockito.anyString())).thenReturn(
				fileOutputStreamMock);

		foodService.getFoodsBy(weekNumber, dayOfTheWeek);

		verify(fileSystemService).openOutputFile(Mockito.anyString());
		verify(fileOutputStreamMock).close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void getFoodsBy_weekNumberUnderOne_resultsInAException() {
		foodService.getFoodsBy(0, 1);
	}

	public static class MockURLStreamHandler extends URLStreamHandler implements
			URLStreamHandlerFactory {
		private MockHttpURLConnection mConnection;

		public MockHttpURLConnection getConnection() {
			return mConnection;
		}

		// *** URLStreamHandler

		@Override
		protected URLConnection openConnection(URL u) throws IOException {
			mConnection = new MockHttpURLConnection(u);
			return mConnection;
		}

		// *** URLStreamHandlerFactory

		@Override
		public URLStreamHandler createURLStreamHandler(String protocol) {
			return this;
		}

	}

	public static class MockHttpURLConnection extends HttpURLConnection {

		protected MockHttpURLConnection(URL url) {
			super(url);
		}

		// *** HttpURLConnection

		@Override
		public InputStream getInputStream() throws IOException {
			try {
				return getTestFileAsInputStream();
			} catch (URISyntaxException e) {
			}

			return null;
		}

		@Override
		public void connect() throws IOException {
		}

		@Override
		public void disconnect() {
		}

		@Override
		public boolean usingProxy() {
			return false;
		}

	}

}
