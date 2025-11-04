import com.grafana.foundation.dashboard.Dashboard;
import grafanaclient.GrafanaClient;
import interfaces.DashboardDefinition;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardPublisher {
    public static void main(String[] args) throws Exception{
        String grafanaUrl = System.getenv("GRAFANA_URL");
        String apiToken   = System.getenv("GRAFANA_API_TOKEN");

        // 1️⃣ Discover dashboards in code
        GrafanaClient grafana = new GrafanaClient(grafanaUrl, apiToken);
        Reflections reflections = new Reflections("dashboards");
        Set<Class<? extends DashboardDefinition>> dashboardClasses =
                reflections.getSubTypesOf(DashboardDefinition.class);

        Set<String> codeUIDs = new HashSet<>();

        for (Class<? extends DashboardDefinition> clazz : dashboardClasses) {
            DashboardDefinition instance = clazz.getDeclaredConstructor().newInstance();
            Dashboard dashboard = instance.build();
            codeUIDs.add(dashboard.uid);
            grafana.upsertDashboard(dashboard);
        }

        // 2️⃣ Get all dashboards currently in Grafana
        List<String> grafanaUIDs = grafana.listDashboardUIDs();

        // 3️⃣ Delete dashboards not defined in code
        for (String uid : grafanaUIDs) {
            if (!codeUIDs.contains(uid)) {
                grafana.deleteDashboard(uid);
            }
        }
    }
}
