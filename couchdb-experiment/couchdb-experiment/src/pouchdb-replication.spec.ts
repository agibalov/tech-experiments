import { expect } from 'chai';
import * as Docker from 'dockerode';
import * as makeNano from 'nano';
import { ServerScope } from 'nano';
import { Container } from 'dockerode';
import { CouchDBContainerDescription, DockerService } from './docker-service';
import { destroyDatabaseIfExists } from './couchdb-utils';
import * as PouchDB from 'pouchdb';
import * as PouchDBAdapterMemory from 'pouchdb-adapter-memory';

describe('PouchDB replication', () => {
    const docker = new Docker();
    const dockerService = new DockerService(docker);

    const username = 'admin';
    const password = 'qwerty';

    let container: Container;
    let nano: ServerScope;

    before(async () => {
        container = await dockerService.start(new CouchDBContainerDescription(username, password));
        nano = makeNano(`http://${username}:${password}@localhost:5984`);
        PouchDB.plugin(PouchDBAdapterMemory);
    });

    after(async () => {
        await dockerService.stop(container);
    });

    beforeEach(async () => {
        // http://docs.couchdb.org/en/stable/install/setup.html#single-node-setup
        for(let name of ['dummy', '_users', '_replicator', '_global_changes']) {
            await destroyDatabaseIfExists(nano, name);
            await nano.db.create(name);
        }
    });

    it('non-live sync should work', async () => {
        const localDb = new PouchDB<Todo>('dummy1', { adapter: 'memory' });
        const remoteDb = new PouchDB<Todo>(`http://${username}:${password}@localhost:5984/dummy`);

        {
            await localDb.put({
                _id: 'one',
                text: 'hi there!'
            });

            await PouchDB.sync(localDb, remoteDb);
        }

        {
            const todoOne = await remoteDb.get('one');
            expect(todoOne.text).equal('hi there!');

            await remoteDb.put({
                _id: 'one',
                _rev: todoOne._rev,
                text: 'updated text'
            });

            await PouchDB.sync(localDb, remoteDb);
        }

        {
            const todoOne = await localDb.get('one');
            expect(todoOne.text).equal('updated text');
        }
    });

    it('live sync should work', async () => {
        const localDb = new PouchDB<Todo>('dummy2', { adapter: 'memory' });
        const remoteDb = new PouchDB<Todo>(`http://${username}:${password}@localhost:5984/dummy`);

        const replication = PouchDB.sync(localDb, remoteDb, {
            live: true
        }).on('change', info => {
            console.log('CHANGE!', JSON.stringify(info, null, 2));
        }).on('paused', err => {
            console.log('PAUSED!', JSON.stringify(err, null, 2));
        }).on('active', () => {
            console.log('ACTIVE!');
        }).on('denied', err => {
            console.log('DENIED!', JSON.stringify(err, null, 2));
        }).on('complete', info => {
            console.log('COMPLETE!', JSON.stringify(info, null, 2));
        }).on('error', err => {
            console.log('ERROR!', JSON.stringify(err, null, 2));
        });
        try {
            {
                await localDb.put({
                    _id: 'one',
                    text: 'hi there!'
                });

                await new Promise(resolve => setTimeout(resolve, 1000));
            }

            {
                const todoOne = await remoteDb.get('one');
                expect(todoOne.text).equal('hi there!');

                await remoteDb.put({
                    _id: 'one',
                    _rev: todoOne._rev,
                    text: 'updated text'
                });

                await new Promise(resolve => setTimeout(resolve, 1000));
            }

            {
                const todoOne = await localDb.get('one');
                expect(todoOne.text).equal('updated text');
            }
        } finally {
            replication.cancel();
        }
    });
});

interface Todo {
    _id?: string;
    _rev?: string;
    text: string;
}
