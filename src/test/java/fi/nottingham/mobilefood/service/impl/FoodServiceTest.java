package fi.nottingham.mobilefood.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.mockserver.MockServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.IFoodService;

public class FoodServiceTest {

	private IFoodService foodService;
	private IFileSystemService fileSystemService;
	private static int port = 4730;
	private static String host = "localhost";
	
	private static MockServer mockServer;
	private static MockServerClient mockServerClient;
	
	@BeforeClass
	public static void beforeClass() {
		mockServer = new MockServer();
		mockServer.start(port, 90);
	}
	
	@AfterClass
	public static void afterClass() {
		mockServer.stop();
	}

	@Before
	public void setUp() throws FileNotFoundException, IOException, URISyntaxException {
		fileSystemService = mock(IFileSystemService.class);
		foodService = new FoodServiceImpl(String.format(
				"http://%s:%s/mobilerest/", host, port), fileSystemService);
		
		mockServerClient = new MockServerClient(host, port);
		mockServerClient.when(new HttpRequest().withPath("/mobilerest/").withMethod(
				"GET"))
		.respond(
				new HttpResponse()
						.withBody(
								IOUtils.toString(getTestFileAsInputStream()))
						.withHeader(
								new Header("Content-Type",
										"application/json; charset=utf-8")));
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
	public void getFoodsBy_withCachedFileForCurrentWeek_returnsUnemptyList()
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

		return FoodServiceTest.class.getResourceAsStream(String.format(
				"2014_w%s_unica.json", weekNumber));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getFoodsBy_FoodsForWeekAreNotCached_callsServiceAndCreatesFile()
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void getFoodsBy_timeouts() throws IOException {
		//TODO: make this error handling test!!
		int dayOfTheWeek = 0, weekNumber = 1;
		OutputStream fileOutputStreamMock = mock(OutputStream.class);
		
		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);
		when(fileSystemService.openOutputFile(Mockito.anyString())).thenReturn(
				fileOutputStreamMock);
		mockServerClient.stop();
		
		foodService.getFoodsBy(weekNumber, dayOfTheWeek);
	}
}
