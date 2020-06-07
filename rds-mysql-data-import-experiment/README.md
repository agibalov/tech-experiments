# rds-mysql-data-import-experiment

Looking for the fastest way to populate the RDS Mysql database with lots of synthetic data for performance testing.

Approaches compared are:

* JDBC batch inserts
* Mysql stored procedure: insert in a loop
* Mysql "load data infile"

# How to deploy and run

* `envTag=dev ./tool.sh deploy` to deploy.
* `envTag=dev ./tool.sh run` to run (create the database, apply migrations and run the app)
* `envTag=dev ./tool.sh undeploy` to undeploy.
