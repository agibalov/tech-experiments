# rds-mysql-data-import-experiment

Looking for the fastest way to populate the RDS Mysql database with lots of synthetic data for performance testing.

Approaches compared are:

* [JDBC batch inserts](jdbc-batch-insert-app)
* [Mysql stored procedure: insert in a loop](stored-procedure-app)
* Mysql "load data infile"

# How to deploy and run

* `envTag=dev branch=master ./tool.sh deploy` to deploy.
* `envTag=dev app=jdbc-batch-insert-app ./tool.sh run` to try the "JDBC batch insert" approach.
* `envTag=dev app=stored-procedure-app ./tool.sh run` to try the "Stored procedure" approach.
* `envTag=dev ./tool.sh undeploy` to undeploy.
