import airflow
from airflow.models import DAG
from airflow.operators.python_operator import PythonOperator

dag = DAG(
    dag_id='experiment-one',
    default_args={
        'owner': 'Andrey',
        'start_date': airflow.utils.dates.days_ago(2), #?
    },
    schedule_interval=None,
)

def do_something(ds, dag_run, ti, **kwargs):
    print('kwargs', kwargs)
    print('ds', ds)

    p1 = dag_run.conf['p1']
    print('params! p1', p1)

    print('do_a says', ti.xcom_pull(task_ids='do_a')) # 'did a'
    print('do_b_says', ti.xcom_pull(task_ids='do_b')) # 'did b''
    print('xxx', ti.xcom_pull(key='xxx', task_ids=['do_a', 'do_b'])) # ('do_a set this', 'do_b set this')

    return f'Hello world! p1={p1}'

def do_a(ds, ti, **kwargs):
    ti.xcom_push(key='xxx', value='do_a set this')
    return 'did a'

def do_b(ds, ti, **kwargs):
    ti.xcom_push(key='xxx', value='do_b set this')
    return 'did b'

do_something_task = PythonOperator(
    task_id='do_something',
    provide_context=True,
    python_callable=do_something,
    dag=dag
)

do_a_task = PythonOperator(
    task_id='do_a',
    provide_context=True,
    python_callable=do_a,
    dag=dag
)

do_b_task = PythonOperator(
    task_id='do_b',
    provide_context=True,
    python_callable=do_b,
    dag=dag
)

do_something_task.set_upstream(do_a_task)
do_something_task.set_upstream(do_b_task)
