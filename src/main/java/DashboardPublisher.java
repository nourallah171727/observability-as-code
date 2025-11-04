import grafanaclient.GrafanaClient;
import interfaces.DashboardDefinition;
import org.reflections.Reflections;

import java.util.Set;

public class DashboardPublisher {
    public static void main(String[] args) throws Exception{
        String grafanaUrl = System.getenv("GRAFANA_URL");
        String apiToken   = System.getenv("GRAFANA_API_TOKEN");


        GrafanaClient grafana = new GrafanaClient(grafanaUrl, apiToken);
        Reflections reflections = new Reflections("dashboards");
        Set<Class<? extends DashboardDefinition>> dashboardClasses =
                reflections.getSubTypesOf(DashboardDefinition.class);

        for (Class<? extends DashboardDefinition> clazz : dashboardClasses) {
            DashboardDefinition instance = clazz.getDeclaredConstructor().newInstance();
            grafana.upsertDashboard(instance.build());
        }
    }
}
