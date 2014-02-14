package fi.nottingham.mobilefood.acceptance;

import java.text.SimpleDateFormat;
import java.util.Properties;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToPackagedName;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.jbehave.core.reporters.StoryReporterBuilder.Format.*;
import fi.nottingham.mobilefood.acceptance.steps.MainViewSteps;

@RunWith(RobolectricTestRunner.class)
public class MainViewTest extends JUnitStory {

	private final CrossReference xref = new CrossReference();

	public MainViewTest() {
		configuredEmbedder().embedderControls()
				.doGenerateViewAfterStories(true)
				.doIgnoreFailureInStories(true).doIgnoreFailureInView(true)
				.useStoryTimeoutInSecs(60);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Configuration configuration() {
		Class<? extends Embeddable> embeddableClass = this.getClass();
		Properties viewResources = new Properties();
		viewResources.put("decorateNonHtml", "true");
		// Start from default ParameterConverters instance
		ParameterConverters parameterConverters = new ParameterConverters();
		// factory to allow parameter conversion and loading from external
		// resources (used by StoryParser too)
		ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(
				new LocalizedKeywords(),
				new LoadFromClasspath(embeddableClass), parameterConverters);
		// add custom converters
		parameterConverters.addConverters(new DateConverter(
				new SimpleDateFormat("d.M.yyyy")), new ExamplesTableConverter(
				examplesTableFactory));

		return new MostUsefulConfiguration()
				.useStoryControls(
						new StoryControls().doDryRun(false)
								.doSkipScenariosAfterFailure(false))
				.useStoryLoader(new LoadFromClasspath(embeddableClass))
				.useStoryParser(new RegexStoryParser(examplesTableFactory))
				.useStoryPathResolver(new UnderscoredCamelCaseResolver())
				.useStoryReporterBuilder(
						new StoryReporterBuilder()
								.withCodeLocation(
										CodeLocations
												.codeLocationFromClass(embeddableClass))
								.withDefaultFormats()
								.withPathResolver(new ResolveToPackagedName())
								.withViewResources(viewResources)
								.withFormats(CONSOLE, TXT, HTML, XML)
								.withFailureTrace(true)
								.withFailureTraceCompression(true)
								.withCrossReference(xref))
				.useParameterConverters(parameterConverters)
				.useStepMonitor(xref.getStepMonitor());
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new MainViewSteps());
	}
}
