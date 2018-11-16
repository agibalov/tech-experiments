import { expect } from 'chai';
import * as Docker from 'dockerode';
import * as makeNano from 'nano';
import { ServerScope } from 'nano';
import { Container } from 'dockerode';
import { CouchDBContainerDescription, DockerService } from './docker-service';
import { destroyDatabaseIfExists } from './couchdb-utils';
import * as PouchDB from 'pouchdb';

describe('PouchDB HTTP adapter', () => {
    const docker = new Docker();
    const dockerService = new DockerService(docker);

    const username = 'admin';
    const password = 'qwerty';

    let container: Container;
    let nano: ServerScope;

    before(async () => {
        container = await dockerService.start(new CouchDBContainerDescription(username, password));
        nano = makeNano(`http://${username}:${password}@localhost:5984`);
    });

    after(async () => {
        await dockerService.stop(container);
    });

    beforeEach(async () => {
        await destroyDatabaseIfExists(nano, 'dummy');
        await nano.db.create('dummy');
    });

    it('should work', async () => {
        const db = new PouchDB<Todo>(`http://${username}:${password}@localhost:5984/dummy`);
        const putResponse = await db.put({
            _id: 'one',
            text: 'hi there!'
        });
        expect(putResponse.ok).equal(true);
        expect(putResponse.id).equal('one');
        expect(putResponse.rev).not.null;

        const getResponse = await db.get('one');
        expect(getResponse._id).equal('one');
        expect(getResponse._rev).not.null;
        expect(getResponse.text).equal('hi there!');
    });
});

interface Todo {
    _id?: string;
    _rev?: string;
    text: string;
}
