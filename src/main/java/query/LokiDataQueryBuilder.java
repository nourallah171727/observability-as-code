package query;

import com.grafana.foundation.loki.DataqueryBuilder;
//this is wrapped to avoid making mistakes easily:
//one can simply do the mistake of using a DataQueryBuilder from another package instead of the right one
public class LokiDataQueryBuilder extends DataqueryBuilder {}
