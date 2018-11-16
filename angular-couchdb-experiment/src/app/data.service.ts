import { Injectable } from '@angular/core';
import { AppCollections, AppDatabase } from './database';

import RxDB, { RxCollectionBase, RxReplicationState } from 'rxdb';
import { TASK_SCHEMA, TaskDocumentType } from './task-document';

import PouchDB from 'pouchdb-browser';
import { BehaviorSubject, Observable, ReplaySubject } from 'rxjs';

@Injectable()
export class DataService {
  private _db: AppDatabase;
  private _replicationState$ = new BehaviorSubject<RxReplicationState>(null);

  constructor(
    private readonly username: string,
    private readonly password: string) {
  }

  async initialize(): Promise<void> {
    if (this._db != null) {
      throw new Error('Already initialized');
    }

    RxDB.plugin(pouch => {
      pouch.adapter('idb', PouchDB.adapters.idb, true);
    });

    RxDB.plugin(pouch => {
      pouch.adapter('http', PouchDB.adapters.http, true);
    });

    const db = await RxDB.create<AppCollections>({
      name: 'dummy',
      adapter: 'idb',
      queryChangeDetection: true
    });

    await db.collection(TASK_SCHEMA);

    this._db = db;

    this.goOnline();
  }

  private get db(): AppDatabase {
    if (this._db == null) {
      throw new Error('Not initialized');
    }
    return this._db;
  }

  get tasks(): RxCollectionBase<TaskDocumentType> {
    return (this.db.collections as AppCollections).tasks;
  }

  get replicationState$(): Observable<RxReplicationState> {
    return this._replicationState$;
  }

  goOnline() {
    const replicationState = this.tasks.sync({
      remote: `http://${this.username}:${this.password}@localhost:4200/couchdb/tasks1`
    });
    this._replicationState$.next(replicationState);
  }

  async goOffline() {
    const replicationState = this._replicationState$.value;
    if (replicationState == null) {
      throw new Error('Already offline');
    }

    await replicationState.cancel();

    this._replicationState$.next(null);
  }
}
