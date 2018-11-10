import { expect } from 'chai';
import * as Docker from 'dockerode';
import * as makeNano from 'nano';
import { MaybeDocument, ServerScope } from 'nano';
import { Container } from 'dockerode';
import { CouchDBContainerDescription, DockerService } from './docker-service';
import { destroyDatabaseIfExists } from './couchdb-utils';

describe('CouchDB', () => {
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
        const db = await nano.db.use<Todo>('dummy');

        const todo = new Todo();
        todo.text = 'hello!';
        const documentInsertResponse = await db.insert(todo);
        expect(documentInsertResponse.ok).equal(true);
        expect(documentInsertResponse.id).not.null;
        expect(documentInsertResponse.rev).not.null;

        const documentGetResponse = await db.get(documentInsertResponse.id);
        expect(documentGetResponse._id).not.null;
        expect(documentGetResponse._rev).not.null;
        expect(documentGetResponse.text).equal('hello!');
    });
});

class Todo implements MaybeDocument {
    _id: string;
    _rev: string;
    text: string;
}
