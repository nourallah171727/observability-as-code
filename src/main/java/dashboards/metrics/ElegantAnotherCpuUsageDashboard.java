package dashboards.metrics;

import com.grafana.foundation.dashboard.Dashboard;

import java.util.List;

public class ElegantAnotherCpuUsageDashboard extends CpuUsageDashboard{
    public Dashboard build(){
        Dashboard dashboard=super.build();
        dashboard.uid="another_raw_cpu_dashboard";
        dashboard.tags=List.of("elegant");
        return dashboard;
    }
}
