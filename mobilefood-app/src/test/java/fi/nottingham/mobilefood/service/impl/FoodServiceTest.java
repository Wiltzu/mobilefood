package fi.nottingham.mobilefood.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

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
		networkStatusService = mock(INetworkStatusService.class);

		String serviceLocation = getServiceLocation();

		foodService = new FoodServiceImpl(serviceLocation, fileSystemService, networkStatusService);

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
	public void getFoodsBy_dontReturnNull() throws Exception{
		int dayOfTheWeek = 0, weekNumber = 1;

		when(networkStatusService.isConnectedToInternet()).thenReturn(true);
		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);
		when(fileSystemService.openOutputFile(Mockito.anyString())).thenReturn(
				mock(OutputStream.class));

		assertNotNull(foodService.getFoodsBy(weekNumber, dayOfTheWeek).get());
	}

	@Test
	public void getFoodsFromInternalStorageBy_withCachedFileForCurrentWeek_returnsUnemptyList()
			throws Exception {
		int dayOfTheWeek = 0, weekNumber = 10;

		InputStream jsonFoodFile = getTestFileAsInputStream();

		when(fileSystemService.openInputFile(Mockito.anyString())).thenReturn(
				jsonFoodFile);

		assertNotNull(foodService.getFoodsFromInternalStorageBy(weekNumber,
				dayOfTheWeek));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getFoodsFromInternalStorageBy_withoutCachedFileForWeek_returnsNull()
			throws Exception {
		int dayOfTheWeek = 0, weekNumber = 10;

		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);

		assertNull(foodService.getFoodsFromInternalStorageBy(weekNumber,
				dayOfTheWeek));
	}

	private static InputStream getTestFileAsInputStream()
			throws FileNotFoundException, URISyntaxException {
		int weekNumber = 10;

		return FoodServiceTest.class.getResourceAsStream(String.format(
				"2014_w%s_unica.json", weekNumber));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getFoodsBy_ifSuccesfulCallToService_createsFileAndClosesTheStream()
			throws Exception {
		int dayOfTheWeek = 0, weekNumber = 1;
		OutputStream fileOutputStreamMock = mock(OutputStream.class);
		when(networkStatusService.isConnectedToInternet()).thenReturn(true);

		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);
		when(fileSystemService.openOutputFile(Mockito.anyString())).thenReturn(
				fileOutputStreamMock);

		foodService.getFoodsBy(weekNumber, dayOfTheWeek).get();

		verify(fileSystemService).openOutputFile(Mockito.anyString());
		verify(fileOutputStreamMock).close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void getFoodsBy_weekNumberUnderOne_resultsInAException()
			throws FoodServiceException {
		int weekNumberUnderOne = 0;
		foodService.getFoodsBy(weekNumberUnderOne, 1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void getFoodsFromInternalStorage_weekNumberUnderOne_resultsInAException()
			throws FoodServiceException {
		int weekNumberUnderOne = 0;
		foodService.getFoodsFromInternalStorageBy(weekNumberUnderOne, 1);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = FoodServiceException.class)
	public void getFoodsBy_withoutServiceOn_throwsException()
			throws FileNotFoundException, NoInternetConnectionException,
			FoodServiceException {

		int dayOfTheWeek = 0, weekNumber = 1;
		when(networkStatusService.isConnectedToInternet()).thenReturn(true);
		
		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);

		foodService = new FoodServiceImpl(
				getServiceLocation() + "serviceNotOn", fileSystemService, networkStatusService);

		try {
			foodService.getFoodsBy(weekNumber, dayOfTheWeek).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			FoodServiceException foodServiceException = (FoodServiceException) e.getCause();
			assertThat(foodServiceException.getErrorCode(),
					Matchers.equalTo(FoodServiceException.SERVICE_DOWN));
			throw foodServiceException;
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = FoodServiceException.class)
	public void getFoodsBy_withServiceOnButItHasNoFileForWeek_throwsException()
			throws FileNotFoundException, NoInternetConnectionException,
			FoodServiceException {
		// "ERROR" String is returned for week 2
		int dayOfTheWeek = 0, weekNumber = 2;
		when(networkStatusService.isConnectedToInternet()).thenReturn(true);
		when(fileSystemService.openInputFile(Mockito.anyString())).thenThrow(
				FileNotFoundException.class);

		try {
			foodService.getFoodsBy(weekNumber, dayOfTheWeek).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			FoodServiceException foodServiceException = (FoodServiceException) e.getCause();
			assertThat(foodServiceException.getErrorCode(),
					Matchers.equalTo(FoodServiceException.NO_FOOD_FOR_WEEK));
			throw foodServiceException;
		}
	}
	
	@Test(expected = NoInternetConnectionException.class)
	public void getFoodsBy_withoutInternetConnection_throwsException() throws Throwable {
		int dayOfTheWeek = 0, weekNumber = 2;
		when(networkStatusService.isConnectedToInternet()).thenReturn(false);

		try {
			foodService.getFoodsBy(weekNumber, dayOfTheWeek).get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}
}
