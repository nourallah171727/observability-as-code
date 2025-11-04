## Grafana dashboard publisher
A java-based tool to programmatically create , update , delete and reuse panels/dashboards in a versionned and controlled way 
## Benefits
- Enables version control for dashboards, which Grafana's UI does not provide.
- Leverages Java's strong typing and compiler checks for safer dashboard definitions.
- Through OOP, developers can easily reuse and extend existing dashboards — more powerfully than with Jsonnet.
- Integrates smoothly with unit testing and build pipelines.
## Setup Overview
!!! Download Docker and JDK 17+ if not already  
ENVIRONMENT :
- please use the forked github repo as a running env: https://github.com/nourallah171727/demo-prometheus-and-grafana-alerts
- !!! the running environment repo (the forked one) and the one which contains java code (this one) are totally different repositories! you will be using both simultaneously.
- you can run the docker containers through : ```docker compose up``` on the forked repo
- this will create instances of grafana(port 3000) ,prometheus and loki (please modify the docker compose file on forked repo if any port is busy)
- you can add testdata to visualize them later through 
- 1: ```cd testdata ```
- 2: ```k6 run 1.cpu-usage.js ```on one terminal instance and
  ```k6 run 2.send-logs.js``` on another if you want

GENERATING DASHBOARDS:
- after you did the env setup you will have running instances 
- now we use our repo to generate dashboards to grafana UI!
- be sure to clone this repo and to be at the root of the project

- run ```./gradlew run``` on this repo ,which would interact via HTTP with the live grafana instance.
- please be sure to plug in env variables before executing ```./gradlew run```
example for zsh users:
``` export GRAFANA_URL="http://localhost:3000" ```
``` export GRAFANA_API_TOKEN="<the_api_token>" ```
```./gradlew run```
- (be sure to use the keywords that work for your shell e.g do not use export on windows)
- if you do not know how to create an API token:
  1) open UI and press grafana's logo on top left
  2) chooe Administration -> Users and access ->Sevice accounts
  3) add a service name with whatever name you want but ensure it has admin role
  4) press "add a service account token" and copy the token grafana gives you when you press generate
  5) please be sure to store the token somewhere , otherwise you might want to delete the token u created and recreate another
## Idea explanation
- the Idea is based on a Restful interaction with Grafana instance.
- in this demo , only dashboards under package "dashboards" in the /src which implement the DashboardDefinition interface would be considered.  
(I added some DummyDashboards for users to experiment with updating , adding and deleting)  
The most important function in the interface is the "Dashboard build();", that every Dashbboard must provide if it wants to be persisted to Grafana.
- CREATE:
  - if you want to create a Dashboard , just add a Dashboard class with a UID you want ,under the /dashboards package
  - be sure to implement the DashboardDefinition interface !
Example:
```java
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
```
UPDATE:  
if you want to update a dashboard please be sure to update existing classes , DO NOT ADD A CLASS WITH SAME UID!(only one of the dashboards with same UID would get randomly persisted)

DELETE:  
just delete the Dashboard class implementing DashboardDefinition from the dashboards package


REUSE:  
if you are a developer who wishes to reuse some already created class , I have a very elegant way of doing it for you!
example:
```java
public class ElegantAnotherCpuUsageDashboard extends CpuUsageDashboard{
    public Dashboard build(){
        Dashboard dashboard=super.build();
        dashboard.uid="another_raw_cpu_dashboard";
        dashboard.tags=List.of("elegant");
        return dashboard;
    }
}
```
no need to redefine every attribute , just modify the very few attributes you want to be modified!  
final:  
after doing all updates / creations / deletions you want , be sure to run the command ./gradlew run.  
Again do not forget to plugin the env varibales before!  
All what ./gradlew run does is iterate over all dashboard classes that should be considered , JSONifies them and interacts with grafana directly via HTTP

## proposition of a simple CI/CD pipeline:
- users would just create another branch
- update whatever dashboard classes they want
- run local tests
- do a pull request
- ON MERGE TO MAIN: other unit tests are run , classes are JSONified and deployed to grafana through the magical ./gradlew run

## Limitations

- This approach assumes UI-based edits in Grafana are disabled. Otherwise, the state may diverge from code.
- In production, direct pushes to `main` should be restricted to avoid unreviewed dashboard changes.
- A useful extension would be automated tests verifying whether dashboards were created/updated/deleted correctly via the Grafana API.
- a problem with directly inheriting from concrete classes in the "elegant" way I showed you , is that it can lead to violating the Liskov subtitution principle , since we can do CategoryADashboard extends CategoryBDashboard just because for NOW they share a lot of structure! but still I think it's a blazing fast way to reuse components. I am happy to discuss other approaches :)
