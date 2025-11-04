package dashboards.metrics;

import com.grafana.foundation.dashboard.*;
import com.grafana.foundation.timeseries.TimeseriesPanelBuilder;
import datasource.PrometheusDataSourceRef;
import interfaces.DashboardDefinition;
import query.PrometheusDataQueryBuilder;

public class CpuUsageDashboard implements DashboardDefinition {
    public Dashboard build() {
        // 1️⃣ Define Prometheus datasource
        DataSourceRef prometheusRef = new PrometheusDataSourceRef();

        // 🔹 2. Create a time series panel for CPU usage
        TimeseriesPanelBuilder panel = new TimeseriesPanelBuilder()
                .title("CPU Usage")
                .datasource(prometheusRef)
                .unit("percent")
                .withTarget(
                        new PrometheusDataQueryBuilder()
                                .expr("cpu_usage")
                                .legendFormat("{{instance}}")
                );
        // 🔹 3. Build the dashboard
        Dashboard dashboard = new DashboardBuilder("CPU Usage Dashboard")
                .uid("raw_cpu_usage_dash")
                .tags(java.util.List.of("cpu", "prometheus"))
                .refresh("5s")
                .time(
                        new DashboardDashboardTimeBuilder()
                                .from("now-15m")
                                .to("now")
                )
                .timezone("browser")
                .withPanel(panel)
                .build();
        return dashboard;
    }
    public String getUID(){
        return "raw_cpu_usage_dash";
    }
}