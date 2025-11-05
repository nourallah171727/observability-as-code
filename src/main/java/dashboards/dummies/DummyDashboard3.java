package dashboards.dummies;

import com.grafana.foundation.dashboard.Dashboard;
import dashboards.logs.LokiLogDashboard;

public class DummyDashboard3 extends LokiLogDashboard {
    public Dashboard build(){
        Dashboard dummy3=super.build();
        dummy3.uid="dummy3";
        return dummy3;
    }
}
