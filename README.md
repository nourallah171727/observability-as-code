#Grafana dashboard publisher
A java-based tool to programmatically create , update , delete and reuse panels/dashboards in a versionned and controlled way 
## Benefits
- Enables version control for dashboards, which Grafana's UI does not provide.
- Leverages Java's strong typing and compiler checks for safer dashboard definitions.
- Through OOP, developers can easily reuse and extend existing dashboards — more powerfully than with Jsonnet.
- Integrates smoothly with unit testing and build pipelines.
#Setup
please use this github repo that I forked as a running env: https://github.com/nourallah171727/demo-prometheus-and-grafana-alerts
you can run the docker containers through : docker compose up
this will create instances of grafana ,prometheus and loki
you can add testdata to already given dashboards in this repo through going in /testdata dir: cd testdata
and then running either :
k6 run 1.cpu-usage.js
or
k6 run 2.send-logs.js
after you did the setup and have running instances , the ./gradlew run on this repo would interact via HTTP with the live grafana instance.
please be sure to plug in env variables before executing ./gradlew run
"export GRAFANA_URL="http://localhost:3000"
export GRAFANA_API_TOKEN=<the_api_token>
./gradlew run"
#Idea explanation
the Idea is based on a Restful interaction with Grafana instance
in this demo , only dashboards under package "dashboards" in the /src which implement the DashboardDefinition interface would be considered.
(I added some DummyDashboards for users to experiment with updating , adding and deleting)
The most important function in the interface is the "Dashboard build();" function that every Dashbboard must provide if it wants to be persisted to Grafana.
Create:
if you wanna create a Dashboard , just add a Dashboard class with a UID you want .
Example:
'''java
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
}'''
update:
if you want to update a dashboard please be sure to update existing classes , DO NOT ADD A CLASS WITH SAME UID!(only one of the dashboards with same UID would get persisted)

delete: just delete the Dashboard class impelemnting DashboardDefinition from the dashboards package


reuse:
if you are a developer who whishes to reuse some already created class , I have a very elegant way of doing it for you!
example:
'''java
public class ElegantAnotherCpuUsageDashboard extends CpuUsageDashboard{
    public Dashboard build(){
        Dashboard dashboard=super.build();
        dashboard.uid="another_raw_cpu_dashboard";
        dashboard.tags=List.of("elegant");
        return dashboard;
    }
}
'''
final:
after doing all updates / creations / deletions you want , be sure to run the command ./gradlew run
All what ./gradlew run does is iterate over all dashboard classes that should be considered , JSONifies them and interacts with grafana directly via HTTP

#proposition of a simple CI/CD pipeline:
users would just create another branch
update whatever dashboard classes they want
run local tests
does a pull request
ON MERGE TO MAIN: other unit tests are run , classes are JSONified and deployed to grafana through the magical ./gradlew run

## Limitations

- This approach assumes UI-based edits in Grafana are disabled. Otherwise, the state may diverge from code.
- In production, direct pushes to `main` should be restricted to avoid unreviewed dashboard changes.
- A useful extension would be automated tests verifying whether dashboards were created/updated/deleted correctly via the Grafana API.
