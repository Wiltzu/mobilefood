package fi.nottingham.mobilefood.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
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
import org.mockserver.model.Parameter;

import com.typesafe.config.ConfigFactory;

import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.INetworkStatusService;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;

public class FoodServiceTest {

	private IFoodService foodService;
	private IFileSystemService fileSystemService;
	private INetworkStatusService networkStatusService;

	private static int port = 4731;
	private static String host = "localhost";

	private static MockServer mockServer;
	private static MockServerClient mockServerClient;
	private static boolean environmentIsTravis;

	@BeforeClass
	public static void beforeClass() {
		environmentIsTravis = ConfigFactory.load().getBoolean(
				"environment.is.travis");
		if (!environmentIsTravis) {
			System.out.println("Starting server...");
			mockServer = new MockServer();
			mockServer.start(port, 90);
		}
	}

	@AfterClass
	public static void afterClass() {
		if (mockServerClient != null) {
			System.out.println("Stopping server's listener client...");
			mockServerClient.stop();
		}
		if (mockServer != null) {
			System.out.println("Stopping server...");
			mockServer.stop();
		}
	}

	@Before
	public void setUp() throws FileNotFoundException, IOException,
			URISyntaxException {
		fileSystemService = mock(IFileSystemService.class);

		// by default device has Internet connection
		networkStatusService = mock(INetworkStatusService.class);
		when(networkStatusService.isConnectedToInternet()).thenReturn(true);

		String serviceLocation = getServiceLocation();

		foodService = new FoodServiceImpl(serviceLocation, fileSystemService,
				networkStatusService);

		if (!environmentIsTravis) {
			System.out.println("Start listening: " + serviceLocation);
			mockServerClient = new MockServerClient(host, port);
			mockServerClient
					.when(new HttpRequest()
							.withPath("/mobilerest/")
							.withMethod("GET")
							.withQueryStringParameter(
									new Parameter("week", "1")))
					.respond(
							new HttpResponse()
									.withBody(
											IOUtils.toString(getTestFileAsInputStream()))
									.withHeader(
											new Header("Content-Type",
													"application/json; charset=utf-8")));
			mockServerClient.when(
					new HttpRequest()
							.withPath("/mobilerest/")
							.withMethod("GET")
							.withQueryStringParameter(
									new Parameter("week", "2"))).respond(
					new HttpResponse().withBody("ERROR").withHeader(
							new Header("Content-Type",
									"text/plain; charset=utf-8")));

		}
	}

	private String getServiceLocation() {
		return String.format("http://%s:%s/mobilerest/", host, port);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getFoodsBy_dontReturnNull() throws FileNotFoundException,
			NoInternetConnectionException, FoodServiceException {
		int dayOfTheWeek = 0, weekNumber = 1;

		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);
		when(fileSystemService.openOutputFile(Mockito.anyString())).thenReturn(
				mock(OutputStream.class));

		assertNotNull(foodService.getFoodsBy(weekNumber, dayOfTheWeek));
	}

	@Test
	public void getFoodsBy_withCachedFileForCurrentWeek_returnsUnemptyList()
			throws FileNotFoundException, URISyntaxException,
			NoInternetConnectionException, FoodServiceException {
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
			throws IOException, NoInternetConnectionException,
			FoodServiceException {
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
	public void getFoodsBy_weekNumberUnderOne_resultsInAException()
			throws NoInternetConnectionException, FoodServiceException {
		int weekNumberUnderOne = 0;
		foodService.getFoodsBy(weekNumberUnderOne, 1);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = FoodServiceException.class)
	public void getFoodsBy_withoutServiceOn_throwsException()
			throws FileNotFoundException, NoInternetConnectionException,
			FoodServiceException {

		int dayOfTheWeek = 0, weekNumber = 1;

		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);

		foodService = new FoodServiceImpl(getServiceLocation() + "serviceNotOn", fileSystemService, networkStatusService);

		try {
			foodService.getFoodsBy(weekNumber, dayOfTheWeek);
		} catch (FoodServiceException e) {
			assertThat(e.getErrorCode(),
					Matchers.equalTo(FoodServiceException.SERVICE_DOWN));
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = FoodServiceException.class)
	public void getFoodsBy_withServiceOnButNoFileForWeek_throwsException()
			throws FileNotFoundException, NoInternetConnectionException,
			FoodServiceException {
		//"ERROR" String is returned for week 2
		int dayOfTheWeek = 0, weekNumber = 2;

		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);

		try {
			foodService.getFoodsBy(weekNumber, dayOfTheWeek);
		} catch (FoodServiceException e) {
			assertThat(e.getErrorCode(),
					Matchers.equalTo(FoodServiceException.NO_FOOD_FOR_WEEK));
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = NoInternetConnectionException.class)
	public void getFoodsBy_withoutInternetConnectionAndAlreadyDownloadedFoods_throwsException()
			throws FileNotFoundException, NoInternetConnectionException,
			FoodServiceException {
		int dayOfTheWeek = 0, weekNumber = 1;

		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);
		when(networkStatusService.isConnectedToInternet()).thenReturn(false);

		foodService.getFoodsBy(weekNumber, dayOfTheWeek);
	}
}
