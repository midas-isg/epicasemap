package interactors;

import gateways.database.SeriesDataUrlDao;

import java.util.List;

import models.entities.Series;
import models.entities.SeriesDataUrl;
import models.filters.Filter;
import models.filters.SeriesDataUrlFilter;

public class SeriesDataUrlRule extends CrudRule<SeriesDataUrl> {
	private SeriesDataUrlDao dao;

	public SeriesDataUrlRule(SeriesDataUrlDao dao) {
		this.dao = dao;
	}

	public List<SeriesDataUrl> query(Filter filter) {
		return dao.queryDao((SeriesDataUrlFilter) filter);
	}

	@Override
	protected SeriesDataUrlDao getDao() {
		return dao;
	}

	public void createNew(Series series, String url, String checksum) {
		SeriesDataUrl dataUrl = new SeriesDataUrl();
		dataUrl.setSeries(series);
		dataUrl.setUrl(url);
		dataUrl.setChecksum(checksum);
		create(dataUrl);
	}

	public List<SeriesDataUrl> query(long seriesId, String url, String checksum) {
		SeriesDataUrlFilter filter = buildSeriesDataUrlFilter(seriesId, url,
				checksum);
		return query(filter);

	}

	private SeriesDataUrlFilter buildSeriesDataUrlFilter(long seriesId,
			String url, String checksum) {
		SeriesDataUrlFilter filter = new SeriesDataUrlFilter();
		filter.setSeriesId(seriesId);
		filter.setUrl(url);
		filter.setChecksum(checksum);
		return filter;
	}

	public List<SeriesDataUrl> query(long seriesId) {
		return this.query(seriesId,null,null);
		
	}

}
