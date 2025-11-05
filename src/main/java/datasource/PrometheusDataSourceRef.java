package datasource;

import com.grafana.foundation.dashboard.DataSourceRef;
//this is wrapped to avoid boilerplate
public class PrometheusDataSourceRef extends DataSourceRef {
    public PrometheusDataSourceRef() {
        super();
        this.type = "prometheus";
        this.uid = "DS_PROMETHEUS_UID";
    }
}
