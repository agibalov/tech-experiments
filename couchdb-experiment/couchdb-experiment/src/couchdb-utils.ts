import { ServerScope } from 'nano';

export async function destroyDatabaseIfExists(nano: ServerScope, name: string) {
    try {
        await nano.db.destroy(name);
    } catch(e) {
        if(e.statusCode === 404) {
            // ok
        } else {
            throw e;
        }
    }
}
