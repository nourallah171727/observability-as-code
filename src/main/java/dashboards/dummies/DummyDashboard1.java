package dashboards.dummies;

import com.grafana.foundation.dashboard.Dashboard;
import dashboards.metrics.CpuUsageDashboard;

public class DummyDashboard1 extends CpuUsageDashboard {
    public Dashboard build(){
        Dashboard dummy1=super.build();
        dummy1.uid="dummy1";
        return dummy1;
    }
}
