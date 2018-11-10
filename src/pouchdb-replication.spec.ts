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
        await destroyDatabaseIfExists(nano, 'dummy');
        await nano.db.create('dummy');
    });

    it('sync should work', async () => {
        const memDb = new PouchDB<Todo>('dummy1', { adapter: 'memory' });
        const httpDb = new PouchDB<Todo>(`http://${username}:${password}@localhost:5984/dummy`);

        {
            await memDb.put({
                _id: 'one',
                text: 'hi there!'
            });

            await PouchDB.sync(memDb, httpDb);
        }

        {
            const todoOne = await httpDb.get('one');
            expect(todoOne.text).equal('hi there!');

            await httpDb.put({
                _id: 'one',
                _rev: todoOne._rev,
                text: 'updated text'
            });

            await PouchDB.sync(memDb, httpDb);
        }

        {
            const todoOne = await memDb.get('one');
            expect(todoOne.text).equal('updated text');
        }
    });
});

interface Todo {
    _id?: string;
    _rev?: string;
    text: string;
}
