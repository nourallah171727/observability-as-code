## Grafana Dashboard Publisher  
A Java-based tool to programmatically create, update, delete, and reuse panels/dashboards in a versioned and controlled way.
## Benefits  
Enables version control for dashboards, which Grafana's UI does not provide.  
Leverages Java's strong typing and compiler checks for safer dashboard definitions.  
Through OOP, developers can easily reuse and extend existing dashboards — more powerfully than with Jsonnet.  
Integrates smoothly with unit testing and build pipelines.  
## Setup Overview  
requirements: Docker, JDK 17+, and k6 v1.3.0  
Some keywords change from one shell to another; I will try to point them out. All commands shown here are zsh/bash-based.  
- Please use the forked GitHub repo as the running environment: https://github.com/nourallah171727/demo-prometheus-and-grafana-alerts  
The running environment repo (the forked one) and the one containing the Java code (this one) are two different repositories! You will be using both simultaneously.
- Run the Docker containers using:  
```docker compose up```
(Execute this command inside the forked repo.)
This will create instances of:
Grafana (port 3000),Prometheus,Loki (Please modify the docker-compose.yml file in the forked repo if any port is busy.)
- You can add test data to visualize later by running:  
```cd testdata  ```
```k6 run 1.cpu-usage.js   # in one terminal```  
```k6 run 2.send-logs.js   # in another terminal```


Now, we use our repo to generate dashboards in the Grafana UI!(you can visited on localhost on port 3000)
- Clone this repository and navigate to its root:  
```git clone https://github.com/nourallah171727/observability-as-code.git```   
```cd observability-as-code```
- run the code of our repo . here is an example for zsh/bash users:
```export GRAFANA_URL="http://localhost:3000"```  
```export GRAFANA_API_TOKEN="<the_api_token>"``` 
```./gradlew run``` 
This command interacts via HTTP with the live Grafana instance.  
⚠️ Please check whether the command format works for your shell and be sure to set the environment variables before executing ./gradlew run.  
If you see generated dashboards under Grafana UI → Dashboards, the setup was successful! ✅

You don't know how to create a Grafana API Token ?:  
1) Open the Grafana UI and click the Grafana logo in the top left corner.  
2) Navigate to Administration → Users and Access → Service Accounts.  
3) Add a service account with any name you like, but ensure it has the Admin role.  
4) Press Add Service Account Token and copy the token Grafana provides when you press Generate.  
5) Store the token somewhere safe — you can reuse it for every execution. Otherwise, delete and recreate it when needed.

## Idea Explanation  
The idea is based on a RESTful interaction with a Grafana instance.  
In this demo, only dashboards under the package dashboards in /src that implement the DashboardDefinition interface are considered.  
(I added some dummy dashboards for users to experiment with updating, adding, and deleting.)  
The most important function in the interface is:  
```Dashboard build();```  
Every dashboard must implement this method to be persisted to Grafana.  
CREATE:  
To create a dashboard, just add a dashboard class with a UID of your choice under the /dashboards package.  
Be sure to implement the DashboardDefinition interface!  
Example:  
```java
    public class CpuUsageDashboard implements DashboardDefinition {
    public Dashboard build() {
        // 1️⃣ Define Prometheus data source
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

    public String getUID() {
        return "raw_cpu_usage_dash";
    }
}
```
UPDATE:  
If you want to update a dashboard, update the existing class.  
⚠️ Do not add another class with the same UID! (Only one of the dashboards with the same UID would get randomly persisted.)  
DELETE:  
To delete a dashboard, just delete the class implementing DashboardDefinition from the /dashboards package.  
REUSE:  
If you are a developer who wishes to reuse an already created class, there’s a very elegant way to do it!  
Example:
``` java
public class ElegantAnotherCpuUsageDashboard extends CpuUsageDashboard {
    public Dashboard build() {
        Dashboard dashboard = super.build();
        dashboard.uid = "another_raw_cpu_dashboard";
        dashboard.tags = List.of("elegant");
        return dashboard;
    }
}
```
No need to redefine every attribute — just modify the few attributes you want to change.  
FINAL:  
After performing all updates, creations, or deletions, run:  
```./gradlew run```  
⚠️ Don’t forget to set the environment variables before executing it!  

All that ./gradlew run does is:  
- Iterate over all dashboard classes that should be considered,  
- Convert them to JSON,  
- And interact with Grafana directly via HTTP.  
This is handled through DashboardPublisher, which performs the iteration, and GrafanaClient, which manages HTTP communication.  
## Proposition of a Simple CI/CD Pipeline
- Developers create a new branch diverging from main.
- Update whatever dashboard classes they want.
- Run local tests.
- Open a pull request.
- On merge to main:
- Unit tests run.
- Classes are JSONified.
- Dashboards are deployed to Grafana through ./gradlew run.
## Limitations
- This approach assumes UI-based edits in Grafana are disabled. Otherwise, the state may diverge from the code.  
- In production, direct pushes to main should be restricted to avoid unreviewed dashboard changes.  
- The gradlew run command should only be executed on a merge to main (unlike in this demo, where manual runs are allowed).  
- A useful extension would be automated tests verifying whether dashboards were created/updated/deleted correctly via the Grafana API.
- demo also lacks real authentication and authorization mechanisms , and works under assumption that grafana always returns 2xx code responses
- A potential issue with directly inheriting from concrete classes in the “elegant” way shown is that it can violate the Liskov Substitution Principle, e.g., CategoryADashboard extending CategoryBDashboard simply because they currently share structure.
However, it remains a blazing-fast way to reuse components. I’m happy to discuss other approaches. 😄
