package grafanaclient;

import com.grafana.foundation.dashboard.Dashboard;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GrafanaClient {
    private final String grafanaUrl;
    private final String apiToken;
    private final HttpClient client = HttpClient.newHttpClient();

    public GrafanaClient(String grafanaUrl, String apiToken) {
        this.grafanaUrl = grafanaUrl;
        this.apiToken = apiToken;
    }

    public void upsertDashboard(Dashboard dashboard) throws Exception {
        String json = """
            {
              "dashboard": %s,
              "overwrite": true
            }
        """.formatted(dashboard.toJSON());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(grafanaUrl + "/api/dashboards/db"))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println("Failed to upsert " + dashboard.uid + ": " + response.body());
        } else {
            System.out.println("✅ Updated: " + dashboard.uid);
        }
    }

    public void deleteDashboard(String uid) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(grafanaUrl + "/api/dashboards/uid/" + uid))
                .header("Authorization", "Bearer " + apiToken)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            System.out.println("🗑️ Deleted dashboard: " + uid);
        } else {
            System.err.println("Failed to delete " + uid + ": " + response.body());
        }
    }
}