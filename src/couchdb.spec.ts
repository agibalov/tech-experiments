import { expect } from 'chai';
import * as Docker from 'dockerode';
import * as makeNano from 'nano';
import { MaybeDocument, ServerScope } from 'nano';
import { Container } from 'dockerode';
import { DockerService } from './docker-service';

describe('CouchDB', () => {
    const docker = new Docker();
    const dockerService = new DockerService(docker);

    const username = 'admin';
    const password = 'qwerty';

    let container: Container;
    let nano: ServerScope;

    before(async () => {
        container = await dockerService.start({
            name: 'dummy-couchdb1',
            Image: 'couchdb:2.2.0',
            Tty: true,
            Env: [
                `COUCHDB_USER=${username}`,
                `COUCHDB_PASSWORD=${password}`
            ],
            ExposedPorts: {
                '5984/tcp': {}
            },
            HostConfig: {
                PortBindings: {
                    '5984/tcp': [
                        { HostPort: '5984' }
                    ]
                }
            }
        });
    });

    after(async () => {
        await dockerService.stop(container);
    });

    beforeEach(async () => {
        nano = makeNano(`http://${username}:${password}@localhost:5984`);
        for(let i = 0; i < 20; ++i) {
            try {
                await nano.db.list();
            } catch(e) {
                console.log(`${i} error`, e);
                await new Promise(resolve => setTimeout(resolve, 5000));
            }
        }

        try {
            await nano.db.destroy('dummy');
        } catch(e) {
            if(e.statusCode === 404) {
                // ok
            } else {
                throw e;
            }
        }
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
