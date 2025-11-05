package dashboards.dummies;

import com.grafana.foundation.dashboard.Dashboard;
import dashboards.logs.LokiLogDashboard;

public class DummyDashboard2 extends LokiLogDashboard {
    public Dashboard build(){
        Dashboard dummy2=super.build();
        dummy2.uid="dummy2";
        return dummy2;
    }
}
