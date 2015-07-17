package interactors;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static suites.Helper.assertAreEqual;

import java.util.ArrayList;
import java.util.List;

import models.entities.Series;
import models.entities.Viz;
import models.view.VizInput;

import org.junit.Test;
import org.mockito.Mockito;

public class TestVizRule {
	private final VizRule sut = new VizRule(null);

	@Test
	public void toVizWithNull() throws Exception {
		assertThat(sut.toViz(null)).isNull();
	}

	@Test
	public void toVizWithNullSeriesIdsAllSeriesIsNull() throws Exception {
		final Viz actual = sut.toViz(new VizInput());
		assertThat(actual.getAllSeries()).isNull();
	}
	
	@Test
	public void toVizWithSeriesIds() throws Exception {
		SeriesRule mock = Mockito.mock(SeriesRule.class);
		sut.setSeriesRule(mock);
		List<Long> seriesIds = asList(1L, 111L);
		VizInput input = new VizInput();
		input.setSeriesIds(seriesIds);
		sut.toViz(input);
		for (Long id : seriesIds)
			verify(mock).read(id);
	}
	
	@Test
	public void fromVizWithNull() throws Exception {
		assertThat(sut.fromViz(null)).isNull();
	}

	@Test
	public void fromVizWithNullAllSeries() throws Exception {
		final Viz data = new Viz();
		data.setAllSeries(null);
		final VizInput actual = sut.fromViz(data);
		assertThat(actual.getSeriesIds()).isNull();
	}

	@Test
	public void fromVizWithAllSeries() throws Exception {
		Viz data = new Viz();
		List<Long> seriesIds = asList(1L, 111L);
		final List<Series> seriesList = makeSeriesList(seriesIds);
		data.setAllSeries(seriesList);
		final VizInput actual = sut.fromViz(data);
		assertAreEqual(actual.getSeriesIds(), seriesIds);
	}

	private List<Series> makeSeriesList(List<Long> seriesIds) {
		List<Series> list = new ArrayList<>();
		for (Long id : seriesIds)
			list.add(makeSeries(id));
		return list;
	}

	private Series makeSeries(final long id) {
		Series s = new Series();
		s.setId(id);
		return s;
	}
}
