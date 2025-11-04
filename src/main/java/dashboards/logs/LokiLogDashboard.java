package dashboards.logs;

import com.grafana.foundation.dashboard.Dashboard;
import com.grafana.foundation.dashboard.DashboardBuilder;
import com.grafana.foundation.dashboard.DashboardDashboardTimeBuilder;
import com.grafana.foundation.dashboard.DataSourceRef;
import com.grafana.foundation.logs.LogsPanelBuilder;
import datasource.LokiDataSourceRef;
import interfaces.DashboardDefinition;
import query.LokiDataQueryBuilder;

import java.util.List;

public class LokiLogDashboard  implements DashboardDefinition {
    public Dashboard build(){
        // 1️⃣ Define Loki datasource
        DataSourceRef lokiRef = new LokiDataSourceRef();

        // 2️⃣ Build a Logs panel — displays actual log lines
        LogsPanelBuilder logsPanel = new LogsPanelBuilder()
                .title("Backend Error Logs")
                .datasource(lokiRef)
                .withTarget(
                        new LokiDataQueryBuilder()
                                .expr("{service_name=\"backend\", detected_level=\"error\"}")
                                .legendFormat("{{instance}}")
                );

        // 3️⃣ Build the dashboard
        Dashboard dashboard = new DashboardBuilder("Loki Raw Logs Dashboard")
                .uid("loki_raw_logs_dash")
                .tags(List.of("loki", "dashboards/logs", "backend"))
                .refresh("5s")
                .time(new DashboardDashboardTimeBuilder()
                        .from("now-15m")
                        .to("now"))
                .timezone("browser")
                .withPanel(logsPanel)
                .build();
        return dashboard;
    }
    public String getUID(){
        return "loki_raw_logs_dash";
    }
}
