# rds-mysql-data-import-experiment

Looking for the fastest way to populate the RDS Mysql database with lots of synthetic data for performance testing.

Approaches compared are:

* [JDBC batch inserts](jdbc-batch-insert-app)
* [Mysql stored procedure: insert in a loop](stored-procedure-app)
* [Mysql "load data infile"](load-data-infile-app)

# How to deploy and run

* `envTag=dev branch=master ./tool.sh deploy` to deploy. After it, go to the CodeBuild project and run it manually. In the very end of build log there will be a test run ID. Use this ID to get the results: `envTag=dev testRunId=123 ./tool.sh results` 
* `envTag=dev app=<APP> ./tool.sh run` to try one of the approaches:
  * `jdbc-batch-insert-app` for "JDBC batch inserts" approach
  * `stored-procedure-app` to try the "Stored procedure" approach.
  * `load-data-infile-app` to try the "Load data infile" approach.
  
  Optionally specify `APP_SCHOOLS`, `APP_CLASSES` and `APP_STUDENTS` (`APP_SCHOOLS=100 APP_CLASSES=200 envTag=dev ./tool.sh run`)
* `envTag=dev ./tool.sh undeploy` to undeploy.
