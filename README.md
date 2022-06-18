# SkyBlockOrM

<h3>Example</h3>

```
ORM.connectionPool("DriverClassName", "databaseUrl", "name", "password", minPoolSize, maxPoolSize);

File.appendLine("CREATE TABLE Table (id BIGINT AUTO_INCREMENT PRIMARY KEY)", "tmp/sql/1.sql");
File.appendLine("ALTER TABLE Table ADD name VARCHAR(255)", "tmp/sql/1.sql");
ORM.applyDDL("tmp/sql"); 

File.appendLine("ALTER TABLE Table ADD salary INT", "tmp/sql/2.sql");
ORM.applyDDL("tmp/sql"); 

public class Table {
    @Id(autoIncremental=true)
    private Long id;
    private String name;
    private Integer salary;
    }
```
