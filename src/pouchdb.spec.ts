import { expect } from 'chai';
import * as PouchDB from 'pouchdb';
import * as PouchDBAdapterMemory from 'pouchdb-adapter-memory';

describe('PouchDB', () => {
    before(() => {
        PouchDB.plugin(PouchDBAdapterMemory);
    });

    it('should work', async () => {
        const db = new PouchDB<Todo>('dummy', { adapter: 'memory' });
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
