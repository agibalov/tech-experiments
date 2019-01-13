file { 'testfile':
    path => '/home/loki2302/1',
    ensure => present,
    content => 'hello there
123'
}
