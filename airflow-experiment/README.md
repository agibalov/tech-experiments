# airflow-experiment

```
pipenv install
pipenv shell

export AIRFLOW_HOME=./airflow

# initialize the sqlite database
airflow initdb

# start the web server
airflow webserver -p 8080

# start the scheduler
airflow scheduler
```

Then go to http://localhost:8080 and enable the 'experiment-one' DAG. Then

```
airflow trigger_dag --conf '{"p1":"qwerty"}' experiment-one
```

Then go to http://localhost:8080 again and see what happens.
