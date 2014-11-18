class { 'postgresql::server': 
  postgres_password => 'qwerty'
}

postgresql::server::db { 'testdb':
  user => 'postgres',
  password => postgresql_password('postgres', 'qwerty')
}
