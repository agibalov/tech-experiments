import { RxDatabase } from 'rxdb';
import { TaskCollection } from './task-document';

export type AppCollections = {
  tasks: TaskCollection;
};

export type AppDatabase = RxDatabase<AppCollections>;
