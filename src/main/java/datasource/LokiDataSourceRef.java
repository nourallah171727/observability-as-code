package datasource;

import com.grafana.foundation.dashboard.DataSourceRef;
//this is wrapped to avoid boilerplate
public class LokiDataSourceRef extends DataSourceRef {
    public LokiDataSourceRef() {
        super();
        this.type = "loki";
        this.uid = "P8E80F9AEF21F6940";
    }
}
