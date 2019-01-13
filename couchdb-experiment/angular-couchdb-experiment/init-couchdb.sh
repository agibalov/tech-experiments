# http://docs.couchdb.org/en/stable/install/setup.html#single-node-setup

username=admin
password=qwerty
curl -X PUT http://${username}:${password}@127.0.0.1:5984/_users
curl -X PUT http://${username}:${password}@127.0.0.1:5984/_replicator
curl -X PUT http://${username}:${password}@127.0.0.1:5984/_global_changes
