file { 'testfile':
    path => '/opt/1.txt',
    ensure => present,
    content => 'hello puppet
'
}